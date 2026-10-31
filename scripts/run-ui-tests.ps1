<#
.SYNOPSIS
Runs the Markdown UI plan in isolated processes and stops at the first mismatch.
.DESCRIPTION
Build with Java 25 and gradlew.bat check shadowJar before running this script.
Expected output comes from the plan. Storage groups support restart scenarios.
#>
param(
    [string]$PlanPath = "test/ui-test-plan.md",
    [string]$JarPath = "build/libs/bob.jar",
    [string]$CasePrefix = "UI-",
    #@@author DaynaG3
    [string]$TranscriptPath = 'build/ui-plan/transcript.txt',
    #@@author
    [switch]$EnableAssertions,
    [ValidateRange(1, 60)]
    [int]$TimeoutSeconds = 10
)

# Run from the repository root. Each case owns a fresh process, and every read is bounded.
$ErrorActionPreference = "Stop"
#@@author DaynaG3
if ($TranscriptPath.Length -gt 0) {
    $transcriptDirectory = Split-Path -Parent $TranscriptPath
    if ($transcriptDirectory.Length -gt 0) {
        New-Item -ItemType Directory -Path $transcriptDirectory -Force | Out-Null
    }
    Start-Transcript -Path $TranscriptPath -Force | Out-Null
}
#@@author
$planText = Get-Content -LiteralPath $PlanPath -Raw -Encoding UTF8
$planText = $planText -replace '(?s)<!--.*?-->', ''
$planText = $planText.Replace("`r`n", "`n").Replace("`r", "`n")
$culture = [System.Globalization.CultureInfo]::GetCultureInfo("en-US")
$today = [DateTime]::Today
$replacements = @{
    "{{TODAY}}" = $today.ToString("d MMMM yyyy", $culture)
    "{{YESTERDAY}}" = $today.AddDays(-1).ToString("d MMMM yyyy", $culture)
    "{{TOMORROW}}" = $today.AddDays(1).ToString("d MMMM yyyy", $culture)
}
foreach ($placeholder in $replacements.Keys) {
    $planText = $planText.Replace($placeholder, $replacements[$placeholder])
}

function Get-Fixture([string]$marker) {
    $pattern = [regex]::Escape($marker) + '\s*```text\n(.*?)^```'
    $match = [regex]::Match($planText, $pattern, 'Singleline, Multiline')
    if (-not $match.Success) {
        throw "Missing fixture: $marker"
    }
    return ($match.Groups[1].Value -replace '\n$', '').Replace([char]0x2420, ' ')
}

function Remove-Ansi([string]$value) {
    return $value -replace '\x1B\[[0-?]*[ -/]*[@-~]', ''
}

function Read-AppLine($process) {
    $pendingLine = $process.StandardOutput.ReadLineAsync()
    if (-not $pendingLine.Wait($TimeoutSeconds * 1000)) {
        throw "Timed out after $TimeoutSeconds seconds waiting for application output."
    }
    $line = $pendingLine.GetAwaiter().GetResult()
    if ($null -eq $line) {
        throw "Application exited before producing the expected output."
    }
    $line = Remove-Ansi $line
    # Make trailing spaces visible in the transcript without changing the compared value.
    $display = [regex]::Replace($line, ' +$', { param($match) ([string][char]0x2420) * $match.Length })
    Write-Host "[app] $display"
    return $line
}

function Compare-Response($process, [string]$expected, [bool]$hasTerminator) {
    $actualLines = [System.Collections.Generic.List[string]]::new()
    $expectedLines = @()
    if ($expected.Length -gt 0) {
        $expectedLines = @($expected.Split("`n"))
    }
    if ($hasTerminator) {
        $expectedLines += ''
    }
    $expectedResponse = [string]::Join("`n", $expectedLines)
    foreach ($expectedLine in $expectedLines) {
        $readFailure = $null
        try {
            $actualLine = Read-AppLine $process
            $actualLines.Add($actualLine)
        } catch {
            $readFailure = $_.Exception.Message
        }
        if ($readFailure -or $actualLine -cne $expectedLine) {
            Write-Host '[expected]'
            Write-Host '```text'
            Write-Host $expectedResponse
            Write-Host '```'
            Write-Host '[actual captured before stopping]'
            Write-Host '```text'
            Write-Host ([string]::Join("`n", $actualLines))
            Write-Host '```'
            if ($readFailure) {
                throw $readFailure
            }
            throw 'Exact output comparison failed.'
        }
    }
}

#@@author DaynaG3
function Finish-App($process, $stderrTask, [int]$expectedExitCode = 0) {
    #@@author
    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        throw "Application did not exit within $TimeoutSeconds seconds."
    }
    $remaining = $process.StandardOutput.ReadToEnd()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    foreach ($line in ($stderr -split '\r?\n')) {
        if ($line.Length -gt 0) {
            Write-Host "[stderr] $line"
        }
    }
    Write-Host "[process] Exited with code $($process.ExitCode)"
    #@@author DaynaG3
    if ($process.ExitCode -ne $expectedExitCode -or $stderr -match '(?m)^SEVERE:|Exception in thread|java\.lang\.\w*Error') {
        #@@author
        throw 'Application reported a crash or an unexpected internal failure.'
    }
    if ($remaining.Length -gt 0) {
        Write-Host "[app] $remaining"
        throw 'Application produced unexpected output after its final response.'
    }
}

#@@author DaynaG3
function Get-CaseSetting([string]$body, [string]$name, [string]$defaultValue = '') {
    $pattern = '(?m)^\*\*' + [regex]::Escape($name) + ':\*\* `([^`]+)`$'
    $setting = [regex]::Match($body, $pattern)
    if ($setting.Success) { return $setting.Groups[1].Value }
    return $defaultValue
}

function Get-StorageFingerprint([string]$workingDirectory) {
    $dataDirectory = Join-Path $workingDirectory 'data'
    if (-not (Test-Path -LiteralPath $dataDirectory)) { return '<missing>' }
    $entries = @(Get-ChildItem -LiteralPath $dataDirectory -Recurse -File | Sort-Object FullName | ForEach-Object {
        $_.FullName.Substring($dataDirectory.Length) + ':' + (Get-FileHash -LiteralPath $_.FullName).Hash
    })
    return [string]::Join("`n", $entries)
}

function Set-StorageFixture([string]$workingDirectory, [string]$fixture) {
    if ($fixture.Length -eq 0) { return }
    $dataDirectory = Join-Path $workingDirectory 'data'
    New-Item -ItemType Directory -Path $dataDirectory -Force | Out-Null
    $exampleDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) 'docs/examples'
    $main = Join-Path $dataDirectory 'bob.json'
    $backup = Join-Path $dataDirectory 'bob.json.bak'
    switch ($fixture) {
        'populated' { Copy-Item -LiteralPath (Join-Path $exampleDirectory 'bob-v1.json') -Destination $main }
        'recovery' {
            [IO.File]::WriteAllText($main, 'broken main')
            Copy-Item -LiteralPath (Join-Path $exampleDirectory 'bob-v1.json') -Destination $backup
        }
        'backup-only' { Copy-Item -LiteralPath (Join-Path $exampleDirectory 'bob-v1.json') -Destination $backup }
        'corrupt' { [IO.File]::WriteAllText($main, 'broken main') }
        'newer' {
            [IO.File]::WriteAllText($main, '{"schemaVersion":2,"newFormat":true}')
            Copy-Item -LiteralPath (Join-Path $exampleDirectory 'bob-v1-empty.json') -Destination $backup
        }
        'blocked-backup' {
            Copy-Item -LiteralPath (Join-Path $exampleDirectory 'bob-v1-empty.json') -Destination $main
            New-Item -ItemType Directory -Path $backup | Out-Null
            [IO.File]::WriteAllText((Join-Path $backup 'blocker.txt'), 'Prevents replacing the backup directory')
        }
        default { throw "Unknown storage fixture: $fixture" }
    }
}
#@@author

$javaVersion = & java --version 2>&1
if ($LASTEXITCODE -ne 0 -or $javaVersion[0] -notmatch '^(?:openjdk|java) 25(?:[.\s]|$)') {
    throw "Java 25 is required. Found: $javaVersion"
}
if (-not (Test-Path -LiteralPath $JarPath -PathType Leaf)) {
    throw "JAR not found: $JarPath. Build it with gradlew.bat shadowJar first."
}
Write-Host "[java] $($javaVersion[0])"
Write-Host "[date] TODAY=$($replacements['{{TODAY}}'])"
Write-Host "[date] YESTERDAY=$($replacements['{{YESTERDAY}}'])"
Write-Host "[date] TOMORROW=$($replacements['{{TOMORROW}}'])"
Write-Host '[transcript] Trailing spaces are shown as U+2420.'

$startup = Get-Fixture '- Startup prompt:'
$greeting = Get-Fixture '- Expected greeting:'
$testUserMatch = [regex]::Match($planText, '(?m)^- Test-user input: `([^`]+)`$')
if (-not $testUserMatch.Success) {
    throw 'Missing test-user input.'
}
$testUser = $testUserMatch.Groups[1].Value
$casePattern = '(?ms)^### (UI-[A-Z]+(?:-[A-Z]+)*-\d+): ([^\n]+)\n(.*?)(?=^### |\z)'
$stepPattern = '(?ms)^#### Step (\d+)\n\s*\*\*Input\*\*\s*```text\n(.*?)^```' +
        '\s*\*\*Expected output(?: \(no output\))?\*\*\s*```text\n(.*?)^```'
$cases = @([regex]::Matches($planText, $casePattern) | Where-Object {
    $_.Groups[1].Value.StartsWith($CasePrefix)
})
if ($cases.Count -eq 0) {
    throw "No test cases match prefix: $CasePrefix"
}

$passedCases = 0
$passedSteps = 0
$passedCommands = 0
#@@author DaynaG3
$runRoot = Join-Path (Split-Path -Parent $PSScriptRoot) ('build/ui-tests-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runRoot | Out-Null
#@@author
foreach ($case in $cases) {
    $caseId = $case.Groups[1].Value
    #@@author DaynaG3
    $caseBody = $case.Groups[3].Value
    $storageGroup = Get-CaseSetting $caseBody 'Storage group' $caseId
    if ($storageGroup -notmatch '^[A-Za-z0-9-]+$') { throw 'Invalid storage group name.' }
    $workingDirectory = Join-Path $runRoot $storageGroup
    New-Item -ItemType Directory -Path $workingDirectory -Force | Out-Null
    Set-StorageFixture $workingDirectory (Get-CaseSetting $caseBody 'Storage fixture')
    $caseBody = $caseBody.Replace('{{STORAGE_PATH}}', (Join-Path $workingDirectory 'data/bob.json'))
    $expectedExitCode = [int](Get-CaseSetting $caseBody 'Exit code' '0')
    $fingerprint = Get-StorageFingerprint $workingDirectory
    $steps = [regex]::Matches($caseBody, $stepPattern)
    #@@author
    $stepHeadingCount = [regex]::Matches($case.Groups[3].Value, '(?m)^#### Step ').Count
    if ($steps.Count -eq 0 -or $steps.Count -ne $stepHeadingCount) {
        throw "Malformed or missing steps in $caseId."
    }
    Write-Host "[case] ${caseId}: $($case.Groups[2].Value)"
    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = 'java'
    $startInfo.Arguments = '-jar "' + (Resolve-Path -LiteralPath $JarPath).Path + '"'
    if ($EnableAssertions) {
        $startInfo.Arguments = '-ea ' + $startInfo.Arguments
    }
    #@@author DaynaG3
    $startInfo.WorkingDirectory = $workingDirectory
    #@@author
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $stepLabel = 'startup'
    $started = $false
    try {
        if (-not $process.Start()) {
            throw 'Unable to start the application.'
        }
        $started = $true
        # Drain diagnostic logs concurrently so a full stderr buffer cannot block stdout.
        $stderrTask = $process.StandardError.ReadToEndAsync()
        #@@author DaynaG3
        if ((Get-CaseSetting $caseBody 'Startup failure') -eq 'true') {
            if ($steps.Count -ne 1 -or $steps[0].Groups[2].Value.Trim() -cne 'Start Bob') {
                throw 'A startup-failure case must have exactly one Start Bob step.'
            }
            Compare-Response $process ($steps[0].Groups[3].Value -replace '\n$', '') $false
            $process.StandardInput.Close()
            Finish-App $process $stderrTask $expectedExitCode
            if ((Get-StorageFingerprint $workingDirectory) -cne $fingerprint) {
                throw 'Failed startup changed saved files.'
            }
            $passedSteps++
            $passedCases++
            Write-Host "[result] ${caseId} step 1: PASS"
            Write-Host "[result] ${caseId}: PASS (startup failure preserved saved files)"
            continue
        }
        #@@author
        Compare-Response $process $startup $false
        Write-Host "[input] $testUser"
        $process.StandardInput.WriteLine($testUser)
        $process.StandardInput.Flush()
        Compare-Response $process $greeting $false
        #@@author DaynaG3
        $warning = [regex]::Match($caseBody, '(?ms)^\*\*Startup warning\*\*\s*```text\n(.*?)^```')
        if ($warning.Success) {
            Compare-Response $process ($warning.Groups[1].Value -replace '\n$', '') $false
        }
        #@@author
        $ended = $false
        foreach ($step in $steps) {
            $number = $step.Groups[1].Value
            #@@author DaynaG3
            if ((Get-CaseSetting $caseBody 'Repair storage before step') -eq $number) {
                # Remove only the two known fixture paths; never recursively delete a computed directory.
                $blockedBackup = Join-Path $workingDirectory 'data/bob.json.bak'
                Remove-Item -LiteralPath (Join-Path $blockedBackup 'blocker.txt')
                Remove-Item -LiteralPath $blockedBackup
                Write-Host '[setup] Removed the backup blocker so the next retry can save.'
            }
            #@@author
            $inputText = $step.Groups[2].Value -replace '\n$', ''
            $inputText = $inputText.Replace([char]0x2420, ' ')
            $expected = $step.Groups[3].Value -replace '\n$', ''
            $stepLabel = "step $number ($inputText)"
            if ($ended) {
                throw 'A step follows a command that exits the application.'
            }
            Write-Host "[input] $inputText"
            if ($inputText -ceq 'Close standard input (EOF)') {
                $process.StandardInput.Close()
                #@@author DaynaG3
                Compare-Response $process $expected $false
                Finish-App $process $stderrTask $expectedExitCode
                #@@author
                $ended = $true
                #@@author DaynaG3
            } elseif (($inputText.Trim() -ieq 'Bye' -and $expected.Length -eq 0) -or
                    $inputText -ceq (Get-CaseSetting $caseBody 'Exit input' '<none>')) {
                #@@author
                $process.StandardInput.WriteLine($inputText)
                $process.StandardInput.Close()
                #@@author DaynaG3
                Compare-Response $process $expected $false
                Finish-App $process $stderrTask $expectedExitCode
                #@@author
                $ended = $true
                $passedCommands++
            } else {
                if ($inputText.Contains("`n")) {
                    throw 'Each step must contain exactly one command.'
                }
                $process.StandardInput.WriteLine($inputText)
                $process.StandardInput.Flush()
                Compare-Response $process $expected $true
                $passedCommands++
            }
            $passedSteps++
            Write-Host "[result] ${caseId} step ${number}: PASS"
        }
        if (-not $ended) {
            $stepLabel = 'cleanup'
            Write-Host '[cleanup] Bye'
            $process.StandardInput.WriteLine('Bye')
            $process.StandardInput.Close()
            Finish-App $process $stderrTask
        }
        #@@author DaynaG3
        if ((Get-CaseSetting $caseBody 'Disk unchanged') -eq 'true' -and
                (Get-StorageFingerprint $workingDirectory) -cne $fingerprint) {
            throw 'A read-only case changed saved files.'
        }
        #@@author
        $passedCases++
        Write-Host "[result] ${caseId}: PASS ($($steps.Count) steps)"
    } catch {
        Write-Host "[result] ${caseId} ${stepLabel}: FAIL - $($_.Exception.Message)"
        Write-Host '[result] Later steps and cases were not run.'
        throw
    } finally {
        if ($started -and -not $process.HasExited) {
            $process.Kill()
            $process.WaitForExit()
            Write-Host "[process] Terminated with code $($process.ExitCode)"
        }
        $process.Dispose()
    }
}
Write-Host "[result] PASS: $passedCases test cases, $passedSteps steps, $passedCommands commands"
#@@author DaynaG3
if ($TranscriptPath.Length -gt 0) {
    Stop-Transcript | Out-Null
}
#@@author
