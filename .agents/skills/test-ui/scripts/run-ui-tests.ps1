param(
    [string]$PlanPath = "test/ui-test-plan.md",
    [string]$JarPath = "build/libs/duke.jar",
    [string]$CasePrefix = "UI-"
)

$ErrorActionPreference = "Stop"
$planLines = Get-Content -LiteralPath $PlanPath -Encoding UTF8
$culture = [System.Globalization.CultureInfo]::GetCultureInfo("en-US")
$today = [DateTime]::Today
$replacements = @{
    "{{TODAY}}" = $today.ToString("d MMMM yyyy", $culture)
    "{{YESTERDAY}}" = $today.AddDays(-1).ToString("d MMMM yyyy", $culture)
    "{{TOMORROW}}" = $today.AddDays(1).ToString("d MMMM yyyy", $culture)
}

function Resolve-Placeholders([string]$text) {
    foreach ($placeholder in $replacements.Keys) {
        $text = $text.Replace($placeholder, $replacements[$placeholder])
    }
    return $text
}

function Get-FencedBlockAfter([string]$marker) {
    $markerIndex = [Array]::IndexOf($planLines, $marker)
    if ($markerIndex -lt 0) {
        throw "Marker not found: $marker"
    }

    $fenceIndex = $markerIndex + 1
    while ($fenceIndex -lt $planLines.Count -and $planLines[$fenceIndex] -ne '```text') {
        $fenceIndex++
    }

    $content = [System.Collections.Generic.List[string]]::new()
    for ($lineIndex = $fenceIndex + 1; $lineIndex -lt $planLines.Count; $lineIndex++) {
        if ($planLines[$lineIndex] -eq '```') {
            return $content.ToArray()
        }
        $content.Add($planLines[$lineIndex])
    }
    throw "Unclosed fenced block after: $marker"
}

function Stop-WithMismatch($process, [string]$message) {
    if (-not $process.HasExited) {
        $process.Kill()
        $process.WaitForExit()
    }
    throw $message
}

$startupExpected = @(Get-FencedBlockAfter '- Startup prompt:' | ForEach-Object {
    Resolve-Placeholders ($_.Replace([char]0x2420, ' '))
})
$greetingExpected = @(Get-FencedBlockAfter '- Expected greeting:' | ForEach-Object {
    Resolve-Placeholders $_
})

$cases = [System.Collections.Generic.List[object]]::new()
$currentCase = $null
for ($lineIndex = 0; $lineIndex -lt $planLines.Count; $lineIndex++) {
    $line = $planLines[$lineIndex]
    if ($line -eq '<!--') {
        break
    }
    if ($line -match '^### (UI-[A-Z]+(?:-[A-Z]+)*-\d+):') {
        $currentCase = [pscustomobject]@{
            Id = $Matches[1]
            Steps = [System.Collections.Generic.List[object]]::new()
        }
        $cases.Add($currentCase)
        continue
    }
    if ($null -eq $currentCase -or $line -notmatch '^#### Step (\d+)$') {
        continue
    }

    $stepNumber = [int]$Matches[1]
    $cursor = $lineIndex
    while ($planLines[$cursor] -ne '**Input**') {
        $cursor++
    }
    while ($planLines[$cursor] -ne '```text') {
        $cursor++
    }

    $inputLines = [System.Collections.Generic.List[string]]::new()
    $cursor++
    while ($planLines[$cursor] -ne '```') {
        $inputLines.Add((Resolve-Placeholders $planLines[$cursor]))
        $cursor++
    }
    while ($planLines[$cursor] -ne '**Expected output**') {
        $cursor++
    }
    while ($planLines[$cursor] -ne '```text') {
        $cursor++
    }

    $expectedLines = [System.Collections.Generic.List[string]]::new()
    $cursor++
    while ($planLines[$cursor] -ne '```') {
        $expectedLines.Add((Resolve-Placeholders $planLines[$cursor]))
        $cursor++
    }
    $currentCase.Steps.Add([pscustomobject]@{
        Number = $stepNumber
        Input = [string]::Join("`n", $inputLines)
        Expected = $expectedLines.ToArray()
    })
    $lineIndex = $cursor
}

$selectedCases = @($cases | Where-Object { $_.Id.StartsWith($CasePrefix) })
if ($selectedCases.Count -eq 0) {
    throw "No test cases match prefix: $CasePrefix"
}

[Console]::WriteLine("[date] TODAY=$($replacements['{{TODAY}}'])")
[Console]::WriteLine("[date] YESTERDAY=$($replacements['{{YESTERDAY}}'])")
[Console]::WriteLine("[date] TOMORROW=$($replacements['{{TOMORROW}}'])")

$passedCases = 0
$passedCommands = 0
foreach ($case in $selectedCases) {
    [Console]::WriteLine("[case] $($case.Id)")
    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = "java"
    $startInfo.Arguments = "-jar `"$JarPath`""
    $startInfo.WorkingDirectory = (Get-Location).Path
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    if (-not $process.Start()) {
        throw "Failed to start $($case.Id)"
    }

    foreach ($expectedLine in $startupExpected) {
        $actualLine = $process.StandardOutput.ReadLine()
        [Console]::WriteLine("[app] $actualLine")
        if ($actualLine -cne $expectedLine) {
            Stop-WithMismatch $process "$($case.Id) startup mismatch. Expected <$expectedLine>, actual <$actualLine>"
        }
    }

    [Console]::WriteLine('[input] Alex')
    $process.StandardInput.WriteLine('Alex')
    $process.StandardInput.Flush()
    foreach ($expectedLine in $greetingExpected) {
        $actualLine = $process.StandardOutput.ReadLine()
        [Console]::WriteLine("[app] $actualLine")
        if ($actualLine -cne $expectedLine) {
            Stop-WithMismatch $process "$($case.Id) greeting mismatch. Expected <$expectedLine>, actual <$actualLine>"
        }
    }

    foreach ($step in $case.Steps) {
        [Console]::WriteLine("[input] $($step.Input)")
        $process.StandardInput.WriteLine($step.Input)
        $process.StandardInput.Flush()
        foreach ($expectedLine in $step.Expected) {
            $actualLine = $process.StandardOutput.ReadLine()
            [Console]::WriteLine("[app] $actualLine")
            if ($actualLine -cne $expectedLine) {
                Stop-WithMismatch $process "$($case.Id) step $($step.Number) mismatch. Expected <$expectedLine>, actual <$actualLine>"
            }
        }

        $terminator = $process.StandardOutput.ReadLine()
        [Console]::WriteLine('[app]')
        if ($null -eq $terminator -or $terminator.Length -ne 0) {
            Stop-WithMismatch $process "$($case.Id) step $($step.Number) missing blank response terminator. Actual <$terminator>"
        }
        $passedCommands++
    }

    [Console]::WriteLine('[cleanup] Bye')
    $process.StandardInput.WriteLine('Bye')
    $process.StandardInput.Flush()
    $process.StandardInput.Close()
    $process.WaitForExit()
    $errorOutput = $process.StandardError.ReadToEnd()
    [Console]::WriteLine("[process] Exited with code $($process.ExitCode)")
    if ($process.ExitCode -ne 0 -or -not [string]::IsNullOrEmpty($errorOutput)) {
        throw "$($case.Id) cleanup failed with code $($process.ExitCode): $errorOutput"
    }
    $passedCases++
}

[Console]::WriteLine("[result] PASS: $passedCases test cases and $passedCommands commands")
