<#
.SYNOPSIS
Runs the Markdown UI plan with a fresh Bob process per case and stops at the first mismatch.
.DESCRIPTION
Build with Java 25 and gradlew.bat check shadowJar before running this script.
Expected output comes from the plan, not from a previous run of the application.
#>
param(
    [string] $CasePrefix = 'UI-',
    [int] $TimeoutSeconds = 10
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$planPath = Join-Path $root 'test/ui-test-plan.md'
$outputDirectory = Join-Path $root 'build/ui-plan'
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
$transcript = [System.Collections.Generic.List[string]]::new()
$process = $null
$stderrTask = $null
$passedCases = 0
$passedSteps = 0
$today = [DateTime]::Today
$culture = [System.Globalization.CultureInfo]::GetCultureInfo('en-US')
$plan = (Get-Content -LiteralPath $planPath -Raw -Encoding utf8).Replace("`r`n", "`n").Replace("`r", "`n")
$plan = [regex]::Replace($plan, '(?s)<!--.*?-->', '')
foreach ($entry in @{ TODAY = 0; YESTERDAY = -1; TOMORROW = 1 }.GetEnumerator()) {
    $date = $today.AddDays($entry.Value).ToString('d MMMM yyyy', $culture)
    $plan = $plan.Replace('{{' + $entry.Key + '}}', $date)
    $transcript.Add('[date] ' + $entry.Key + '=' + $date)
}
$plan = $plan.Replace([string][char]0x2420, ' ')

function Get-FencedBlock([string] $text, [string] $label) {
    $pattern = '(?s)' + $label + '\s*```text\n(.*?)\n?```'
    $match = [regex]::Match($text, $pattern)
    if (-not $match.Success) { throw "Missing or malformed block: $label" }
    return $match.Groups[1].Value.TrimEnd([char]10)
}

function Compare-Output([string] $expected) {
    if ($expected.Length -eq 0) { return }
    foreach ($line in ($expected -split "`n")) {
        $read = $process.StandardOutput.ReadLineAsync()
        if (-not $read.Wait($TimeoutSeconds * 1000)) { throw 'Timed out waiting for output' }
        $actual = $read.Result
        if ($null -eq $actual) { throw "Unexpected EOF; expected <$line>" }
        $actual = [regex]::Replace($actual, '\x1B\[[0-?]*[ -/]*[@-~]', '')
        $transcript.Add('[app] ' + $actual)
        if ($actual -cne $line) { throw "Expected <$line>; actual <$actual>" }
    }
}

function Finish-Process {
    $process.StandardInput.Close()
    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) { throw 'Application did not exit' }
    $extra = $process.StandardOutput.ReadToEnd()
    if ($extra.Length -ne 0) { throw "Unexpected trailing stdout: $extra" }
    $diagnostics = $stderrTask.Result
    if ($diagnostics) { $transcript.Add('[stderr] ' + $diagnostics.TrimEnd()) }
    if ($process.ExitCode -ne 0 -or $diagnostics -match 'SEVERE:|Exception in thread') {
        throw "Application failure; exit code $($process.ExitCode)"
    }
    $transcript.Add('[exit] ' + $process.ExitCode)
}

Push-Location $root
try {
    $version = (& java --version 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0 -or $version -notmatch '(?:java|openjdk) 25[.\s]') {
        throw "Java 25 is required. Found: $version"
    }
    $transcript.Add('[java] ' + $version.TrimEnd())
    if (-not (Test-Path 'build/libs/bob.jar')) { throw 'Build bob.jar first with gradlew.bat check shadowJar' }
    $startup = Get-FencedBlock $plan '- Startup prompt:'
    $greeting = Get-FencedBlock $plan '- Expected greeting:'
    $cases = [regex]::Matches($plan, '(?ms)^### (UI-[^:]+):[^\n]*\n(.*?)(?=^### |\z)')
    foreach ($case in $cases) {
        $caseId = $case.Groups[1].Value
        if (-not $caseId.StartsWith($CasePrefix, [StringComparison]::Ordinal)) { continue }
        $transcript.Add('[case] ' + $caseId)
        $process = [System.Diagnostics.Process]::new()
        $process.StartInfo.FileName = 'java'
        $process.StartInfo.WorkingDirectory = $root
        $process.StartInfo.Arguments = '-ea -jar build/libs/bob.jar'
        $process.StartInfo.UseShellExecute = $false
        $process.StartInfo.CreateNoWindow = $true
        $process.StartInfo.RedirectStandardInput = $true
        $process.StartInfo.RedirectStandardOutput = $true
        $process.StartInfo.RedirectStandardError = $true
        $null = $process.Start()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        Compare-Output $startup
        $transcript.Add('[input] Alex')
        $process.StandardInput.WriteLine('Alex')
        Compare-Output $greeting
        $steps = [regex]::Matches($case.Groups[2].Value,
                '(?ms)^#### Step ([^\n]+)\n(.*?)(?=^#### Step |\z)')
        if ($steps.Count -eq 0) { throw "No steps found for $caseId" }
        $ended = $false
        foreach ($step in $steps) {
            if ($ended) { throw "Steps occur after termination in $caseId" }
            $inputText = Get-FencedBlock $step.Groups[2].Value '\*\*Input\*\*'
            $expected = Get-FencedBlock $step.Groups[2].Value '\*\*Expected output(?: \(no output\))?\*\*'
            if ($inputText.Contains("`n")) { throw 'Each step must contain exactly one input command' }
            $transcript.Add('[input] ' + $inputText)
            if ($inputText -eq 'Close standard input (EOF)') {
                $process.StandardInput.Close()
                Compare-Output $expected
                Finish-Process
                $ended = $true
            } else {
                $process.StandardInput.WriteLine($inputText)
                if ($inputText.Trim() -ieq 'Bye') {
                    Compare-Output $expected
                    Finish-Process
                    $ended = $true
                } else {
                    Compare-Output ($expected + "`n")
                }
            }
            $passedSteps++
        }
        if (-not $ended) {
            $transcript.Add('[cleanup input] Bye')
            $process.StandardInput.WriteLine('Bye')
            Finish-Process
        }
        $process.Dispose()
        $process = $null
        $passedCases++
    }
    if ($passedCases -eq 0) { throw "No cases matched prefix $CasePrefix" }
    $transcript.Add("[result] PASS: $passedCases cases, $passedSteps steps")
    Write-Output $transcript[-1]
} catch {
    $transcript.Add('[result] FAIL: ' + $_.Exception.Message)
    $transcript.Add('[result] Later steps and cases were not run.')
    if ($null -ne $process -and -not $process.HasExited) { $process.Kill() }
    throw
} finally {
    $transcript | Set-Content -Encoding utf8 (Join-Path $outputDirectory 'transcript.txt')
    if ($null -ne $process) { $process.Dispose() }
    Pop-Location
}
