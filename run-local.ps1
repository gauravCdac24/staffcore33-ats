#Requires -Version 5.1
<#
.SYNOPSIS
  Start Staffcore33 ATS locally on Windows.

.DESCRIPTION
  - Auto-installs missing JDK 17+, Maven, Node.js/npm (and PostgreSQL when possible)
  - Ensures local PostgreSQL DB (staffcore33_ats) is available
  - Stores uploaded files under project\device\
  - Frees ports 3000 (frontend) and 8080 (backend) if in use
  - Installs frontend npm packages when missing
  - Starts Spring Boot API + Next.js frontend

.EXAMPLE
  .\run-local.ps1
  .\run-local.ps1 -DbUser postgres -DbPassword root
  .\run-local.ps1 -SkipToolInstall
#>

[CmdletBinding()]
param(
    [string]$DbUser = $(if ($env:DB_USER) { $env:DB_USER } else { "postgres" }),
    [string]$DbPassword = $(if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "root" }),
    [int]$BackendPort = 8080,
    [int]$FrontendPort = 3000,
    [switch]$SkipNpmInstall,
    [switch]$SkipToolInstall,
    [switch]$SkipPostgresInstall
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = Join-Path $Root "backend"
$FrontendDir = Join-Path $Root "frontend"
$DeviceDir = Join-Path $Root "device"
$LogsDir = Join-Path $Root "logs"
$ToolsDir = Join-Path $Root ".tools"
$script:ExtraPathDirs = New-Object System.Collections.Generic.List[string]

# Portable tool versions (fallback downloads)
$MavenVersion = "3.9.9"
$NodeVersion = "20.18.1"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Write-Ok {
    param([string]$Message)
    Write-Host "    $Message" -ForegroundColor Green
}

function Write-WarnMsg {
    param([string]$Message)
    Write-Host "    $Message" -ForegroundColor Yellow
}

function Write-Fail {
    param([string]$Message)
    Write-Host "    ERROR: $Message" -ForegroundColor Red
}

function Test-CommandExists {
    param([string]$Name)
    return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

function Add-ExtraPath {
    param([string]$Dir)
    if ([string]::IsNullOrWhiteSpace($Dir)) { return }
    if (-not (Test-Path -LiteralPath $Dir)) { return }
    if (-not ($script:ExtraPathDirs -contains $Dir)) {
        [void]$script:ExtraPathDirs.Add($Dir)
    }
}

function Update-SessionPath {
    $machine = [Environment]::GetEnvironmentVariable("Path", "Machine")
    $user = [Environment]::GetEnvironmentVariable("Path", "User")
    $parts = @()
    if ($script:ExtraPathDirs.Count -gt 0) {
        $parts += $script:ExtraPathDirs.ToArray()
    }
    if ($machine) { $parts += $machine }
    if ($user) { $parts += $user }
    $env:Path = ($parts -join ";")

    # Discover common install locations that winget/choco may not put on PATH yet
    $discover = @(
        "$env:ProgramFiles\Eclipse Adoptium\jdk-17*\bin",
        "$env:ProgramFiles\Eclipse Adoptium\jdk-21*\bin",
        "$env:ProgramFiles\Microsoft\jdk-17*\bin",
        "$env:ProgramFiles\Microsoft\jdk-21*\bin",
        "$env:ProgramFiles\Java\jdk-17*\bin",
        "$env:ProgramFiles\Java\jdk-21*\bin",
        "$env:LocalAppData\Programs\Eclipse Adoptium\jdk-17*\bin",
        "$env:LocalAppData\Programs\Eclipse Adoptium\jdk-21*\bin",
        "$env:ProgramFiles\Apache\maven\bin",
        "$env:ProgramFiles\Maven\bin",
        "$env:ProgramData\chocolatey\lib\maven\apache-maven-*\bin",
        "$env:ProgramFiles\nodejs",
        "$env:ProgramFiles (x86)\nodejs",
        "$env:LocalAppData\Programs\node",
        "$env:ProgramFiles\PostgreSQL\17\bin",
        "$env:ProgramFiles\PostgreSQL\16\bin",
        "$env:ProgramFiles\PostgreSQL\15\bin",
        "$env:ProgramFiles\PostgreSQL\14\bin"
    )
    foreach ($pattern in $discover) {
        Get-Item -Path $pattern -ErrorAction SilentlyContinue | ForEach-Object {
            Add-ExtraPath $_.FullName
        }
    }

    if ($script:ExtraPathDirs.Count -gt 0) {
        $env:Path = (($script:ExtraPathDirs.ToArray() + $env:Path) -join ";")
    }

    # Clear command cache so Get-Command sees new binaries
    Get-Command java, mvn, node, npm, psql -ErrorAction SilentlyContinue | Out-Null
}

function Get-PathExportSnippet {
    if ($script:ExtraPathDirs.Count -eq 0) { return "" }
    $joined = ($script:ExtraPathDirs.ToArray() -join ";")
    return "`$env:Path = '$joined;' + `$env:Path"
}

function Install-WithWinget {
    param([string]$PackageId, [string]$Label)
    if (-not (Test-CommandExists "winget")) { return $false }
    Write-WarnMsg "Installing $Label via winget ($PackageId)..."
    $args = @(
        "install", "--id", $PackageId, "-e", "--accept-package-agreements",
        "--accept-source-agreements", "--disable-interactivity"
    )
    & winget @args
    $code = $LASTEXITCODE
    # 0 = success, -1978335189 (0x8A15002B) = already installed
    if ($code -eq 0 -or $code -eq -1978335189) {
        Write-Ok "$Label installed (or already present) via winget"
        return $true
    }
    Write-WarnMsg "winget install for $Label exited with code $code"
    return $false
}

function Install-WithChoco {
    param([string]$PackageName, [string]$Label)
    if (-not (Test-CommandExists "choco")) { return $false }
    Write-WarnMsg "Installing $Label via Chocolatey ($PackageName)..."
    & choco install $PackageName -y --no-progress
    if ($LASTEXITCODE -eq 0) {
        Write-Ok "$Label installed via Chocolatey"
        return $true
    }
    Write-WarnMsg "Chocolatey install for $Label exited with code $LASTEXITCODE"
    return $false
}

function Expand-ZipTo {
    param(
        [string]$ZipPath,
        [string]$Destination
    )
    if (Test-Path -LiteralPath $Destination) {
        Remove-Item -LiteralPath $Destination -Recurse -Force -ErrorAction SilentlyContinue
    }
    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    Expand-Archive -LiteralPath $ZipPath -DestinationPath $Destination -Force
}

function Get-SingleChildDirectory {
    param([string]$Parent)
    $kids = Get-ChildItem -LiteralPath $Parent -Directory -ErrorAction SilentlyContinue
    if ($kids.Count -eq 1) { return $kids[0].FullName }
    return $Parent
}

function Install-PortableJdk {
    Write-WarnMsg "Downloading portable Eclipse Temurin JDK 17..."
    New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null
    $zip = Join-Path $ToolsDir "jdk17.zip"
    $url = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
        $dest = Join-Path $ToolsDir "jdk-17"
        Expand-ZipTo -ZipPath $zip -Destination $dest
        $jdkHome = Get-SingleChildDirectory -Parent $dest
        $bin = Join-Path $jdkHome "bin"
        if (-not (Test-Path (Join-Path $bin "java.exe"))) {
            throw "java.exe not found after JDK extract"
        }
        Add-ExtraPath $bin
        $env:JAVA_HOME = $jdkHome
        Update-SessionPath
        Remove-Item $zip -Force -ErrorAction SilentlyContinue
        Write-Ok "Portable JDK ready at $jdkHome"
        return $true
    } catch {
        Write-WarnMsg "Portable JDK download failed: $($_.Exception.Message)"
        return $false
    }
}

function Install-PortableMaven {
    Write-WarnMsg "Downloading portable Apache Maven $MavenVersion..."
    New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null
    $zip = Join-Path $ToolsDir "maven.zip"
    $url = "https://dlcdn.apache.org/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
    $urlFallback = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        try {
            Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
        } catch {
            Invoke-WebRequest -Uri $urlFallback -OutFile $zip -UseBasicParsing
        }
        $dest = Join-Path $ToolsDir "maven"
        Expand-ZipTo -ZipPath $zip -Destination $dest
        $mavenHome = Get-SingleChildDirectory -Parent $dest
        $bin = Join-Path $mavenHome "bin"
        if (-not (Test-Path (Join-Path $bin "mvn.cmd"))) {
            throw "mvn.cmd not found after Maven extract"
        }
        Add-ExtraPath $bin
        $env:MAVEN_HOME = $mavenHome
        Update-SessionPath
        Remove-Item $zip -Force -ErrorAction SilentlyContinue
        Write-Ok "Portable Maven ready at $mavenHome"
        return $true
    } catch {
        Write-WarnMsg "Portable Maven download failed: $($_.Exception.Message)"
        return $false
    }
}

function Install-PortableNode {
    Write-WarnMsg "Downloading portable Node.js $NodeVersion..."
    New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null
    $zip = Join-Path $ToolsDir "node.zip"
    $url = "https://nodejs.org/dist/v$NodeVersion/node-v$NodeVersion-win-x64.zip"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
        $dest = Join-Path $ToolsDir "node"
        Expand-ZipTo -ZipPath $zip -Destination $dest
        $nodeHome = Get-SingleChildDirectory -Parent $dest
        if (-not (Test-Path (Join-Path $nodeHome "node.exe"))) {
            throw "node.exe not found after Node extract"
        }
        Add-ExtraPath $nodeHome
        Update-SessionPath
        Remove-Item $zip -Force -ErrorAction SilentlyContinue
        Write-Ok "Portable Node.js ready at $nodeHome"
        return $true
    } catch {
        Write-WarnMsg "Portable Node.js download failed: $($_.Exception.Message)"
        return $false
    }
}

function Ensure-JavaTool {
    Update-SessionPath
    if (Test-CommandExists "java") { return $true }
    if ($SkipToolInstall) { return $false }

    Write-Step "Installing JDK 17+"
    $ok = Install-WithWinget -PackageId "EclipseAdoptium.Temurin.17.JDK" -Label "JDK 17"
    if (-not $ok) { $ok = Install-WithWinget -PackageId "Microsoft.OpenJDK.17" -Label "Microsoft OpenJDK 17" }
    if (-not $ok) { $ok = Install-WithChoco -PackageName "temurin17" -Label "JDK 17" }
    Update-SessionPath
    if (Test-CommandExists "java") { return $true }

    return (Install-PortableJdk)
}

function Ensure-MavenTool {
    Update-SessionPath
    if (Test-CommandExists "mvn") { return $true }
    if ($SkipToolInstall) { return $false }

    Write-Step "Installing Maven"
    $ok = Install-WithWinget -PackageId "Apache.Maven" -Label "Maven"
    if (-not $ok) { $ok = Install-WithChoco -PackageName "maven" -Label "Maven" }
    Update-SessionPath
    if (Test-CommandExists "mvn") { return $true }

    return (Install-PortableMaven)
}

function Ensure-NodeTool {
    Update-SessionPath
    if ((Test-CommandExists "node") -and (Test-CommandExists "npm")) { return $true }
    if ($SkipToolInstall) { return $false }

    Write-Step "Installing Node.js (includes npm)"
    $ok = Install-WithWinget -PackageId "OpenJS.NodeJS.LTS" -Label "Node.js LTS"
    if (-not $ok) { $ok = Install-WithChoco -PackageName "nodejs-lts" -Label "Node.js LTS" }
    Update-SessionPath
    if ((Test-CommandExists "node") -and (Test-CommandExists "npm")) { return $true }

    return (Install-PortableNode)
}

function Ensure-PostgresTool {
    Update-SessionPath
    $pgService = Get-Service -ErrorAction SilentlyContinue |
        Where-Object {
            $_.Name -match "postgres|postgresql" -or
            $_.DisplayName -match "postgres|postgresql"
        }
    if ($pgService) { return $true }
    if (Test-CommandExists "psql") { return $true }
    if (Test-Path "$env:ProgramFiles\PostgreSQL\16\bin\psql.exe") { return $true }
    if (Test-Path "$env:ProgramFiles\PostgreSQL\17\bin\psql.exe") { return $true }
    if ($SkipToolInstall -or $SkipPostgresInstall) { return $false }

    Write-Step "Installing PostgreSQL"
    Write-WarnMsg "PostgreSQL installer may prompt for admin approval / password setup."
    Write-WarnMsg "Prefer DB password '$DbPassword' (or pass -DbPassword) to match local config."

    $ok = Install-WithWinget -PackageId "PostgreSQL.PostgreSQL.16" -Label "PostgreSQL 16"
    if (-not $ok) { $ok = Install-WithWinget -PackageId "PostgreSQL.PostgreSQL.17" -Label "PostgreSQL 17" }
    if (-not $ok) { $ok = Install-WithChoco -PackageName "postgresql16" -Label "PostgreSQL 16" }

    Update-SessionPath
    Start-Sleep -Seconds 3
    return [bool](
        (Get-Service -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "postgres" }) -or
        (Test-CommandExists "psql") -or
        (Test-Path "$env:ProgramFiles\PostgreSQL\16\bin\psql.exe") -or
        (Test-Path "$env:ProgramFiles\PostgreSQL\17\bin\psql.exe")
    )
}

function Show-ToolVersions {
    try {
        $javaVer = & java -version 2>&1 | Select-Object -First 1
        Write-Ok "Java: $javaVer"
    } catch {
        Write-Ok "Java found"
    }
    Write-Ok "Maven: $(mvn -v 2>$null | Select-Object -First 1)"
    Write-Ok "Node: $(node -v)"
    Write-Ok "npm: $(npm -v)"
}

function Ensure-Prerequisites {
    Write-Step "Checking prerequisites"

    Update-SessionPath

    $needJava = -not (Test-CommandExists "java")
    $needMaven = -not (Test-CommandExists "mvn")
    $needNode = -not ((Test-CommandExists "node") -and (Test-CommandExists "npm"))

    if ($needJava -or $needMaven -or $needNode) {
        if ($SkipToolInstall) {
            $missing = @()
            if ($needJava) { $missing += "JDK 17+ (java)" }
            if ($needMaven) { $missing += "Maven (mvn)" }
            if ($needNode) { $missing += "Node.js / npm" }
            Write-Fail "Missing: $($missing -join ', ')"
            throw "Install the missing tools or re-run without -SkipToolInstall."
        }

        Write-WarnMsg "Some tools are missing - attempting automatic install..."
        if (-not (Test-CommandExists "winget") -and -not (Test-CommandExists "choco")) {
            Write-WarnMsg "winget/choco not found - will use portable downloads into .tools\"
        }
    }

    if (-not (Ensure-JavaTool)) {
        Write-Fail "JDK (java) is not available after install attempts"
        throw "Could not install JDK 17+. Install manually from https://adoptium.net/ and re-run."
    }
    Write-Ok "JDK available"

    if (-not (Ensure-MavenTool)) {
        Write-Fail "Maven (mvn) is not available after install attempts"
        throw "Could not install Maven. Install manually and re-run."
    }
    Write-Ok "Maven available"

    if (-not (Ensure-NodeTool)) {
        Write-Fail "Node.js/npm is not available after install attempts"
        throw "Could not install Node.js. Install manually from https://nodejs.org/ and re-run."
    }
    Write-Ok "Node.js / npm available"

    # Re-check after installs
    Write-Step "Re-checking prerequisites"
    Update-SessionPath

    $stillMissing = @()
    if (-not (Test-CommandExists "java")) { $stillMissing += "java" }
    if (-not (Test-CommandExists "mvn")) { $stillMissing += "mvn" }
    if (-not (Test-CommandExists "node")) { $stillMissing += "node" }
    if (-not (Test-CommandExists "npm")) { $stillMissing += "npm" }

    if ($stillMissing.Count -gt 0) {
        Write-Fail "Still missing after install: $($stillMissing -join ', ')"
        throw "Close this window, open a new terminal (so PATH refreshes), and re-run start-local.bat."
    }

    Show-ToolVersions

    # PostgreSQL is required for next steps - best effort install
    $null = Ensure-PostgresTool
    Update-SessionPath
}

function Get-PidsOnPort {
    param([int]$Port)
    $pids = New-Object "System.Collections.Generic.HashSet[int]"
    try {
        Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
            ForEach-Object { [void]$pids.Add($_.OwningProcess) }
    } catch {
        # Fallback when Get-NetTCPConnection is unavailable
    }

    $netstat = netstat -ano -p tcp 2>$null | Select-String "LISTENING"
    foreach ($line in $netstat) {
        $text = $line.ToString()
        if ($text -notmatch ":$Port\s") { continue }
        $parts = ($text -split "\s+") | Where-Object { $_ -ne "" }
        if ($parts.Count -ge 5) {
            $procId = 0
            if ([int]::TryParse($parts[-1], [ref]$procId) -and $procId -gt 0) {
                [void]$pids.Add($procId)
            }
        }
    }
    return @($pids | Where-Object { $_ -gt 0 -and $_ -ne $PID })
}

function Clear-Port {
    param(
        [int]$Port,
        [string]$Label
    )
    $pids = Get-PidsOnPort -Port $Port
    if (-not $pids -or $pids.Count -eq 0) {
        Write-Ok "Port $Port ($Label) is free"
        return
    }

    Write-WarnMsg "Port $Port ($Label) in use by PID(s): $($pids -join ', ') - freeing..."
    foreach ($procId in $pids) {
        try {
            $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
            $name = if ($proc) { $proc.ProcessName } else { "unknown" }
            Stop-Process -Id $procId -Force -ErrorAction Stop
            Write-Ok "Stopped PID ${procId} ($name)"
        } catch {
            Write-WarnMsg "Could not stop PID ${procId}: $($_.Exception.Message)"
        }
    }

    Start-Sleep -Seconds 1
    $still = Get-PidsOnPort -Port $Port
    if ($still -and $still.Count -gt 0) {
        throw "Port $Port is still in use by PID(s): $($still -join ', '). Close those processes and retry."
    }
    Write-Ok "Port $Port is now free"
}

function Ensure-Folders {
    Write-Step "Preparing local data folders"
    New-Item -ItemType Directory -Force -Path $DeviceDir | Out-Null
    New-Item -ItemType Directory -Force -Path $LogsDir | Out-Null
    New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null
    Write-Ok "Uploaded files -> $DeviceDir"
    Write-Ok "Service logs   -> $LogsDir"
    Write-Ok "Local tools    -> $ToolsDir"
}

function Ensure-Postgres {
    Write-Step "Checking PostgreSQL (local database)"

    Update-SessionPath

    $pgServices = Get-Service -ErrorAction SilentlyContinue |
        Where-Object {
            $_.Name -match "postgres|postgresql" -or
            $_.DisplayName -match "postgres|postgresql"
        }

    if ($pgServices) {
        foreach ($svc in $pgServices) {
            if ($svc.Status -ne "Running") {
                Write-WarnMsg "Starting service $($svc.Name)..."
                try {
                    Start-Service -Name $svc.Name
                    Write-Ok "Started $($svc.Name)"
                } catch {
                    Write-WarnMsg "Could not start $($svc.Name): $($_.Exception.Message)"
                }
            } else {
                Write-Ok "Service running: $($svc.Name)"
            }
        }
    } else {
        Write-WarnMsg "No PostgreSQL Windows service found yet."
    }

    $env:PGPASSWORD = $DbPassword
    $psqlCmd = Get-Command "psql" -ErrorAction SilentlyContinue
    $psql = $null

    if ($psqlCmd) {
        $psql = $psqlCmd.Source
    } else {
        $candidates = @(
            "C:\Program Files\PostgreSQL\17\bin\psql.exe",
            "C:\Program Files\PostgreSQL\16\bin\psql.exe",
            "C:\Program Files\PostgreSQL\15\bin\psql.exe",
            "C:\Program Files\PostgreSQL\14\bin\psql.exe"
        )
        foreach ($c in $candidates) {
            if (Test-Path $c) {
                $psql = $c
                Add-ExtraPath (Split-Path $c -Parent)
                Update-SessionPath
                break
            }
        }
    }

    if (-not $psql) {
        Write-Fail "PostgreSQL client (psql) not found."
        Write-WarnMsg "Install PostgreSQL 14+, create DB staffcore33_ats, then re-run."
        Write-WarnMsg "Or run: winget install PostgreSQL.PostgreSQL.16"
        throw "PostgreSQL is required for local data storage."
    }

    Write-Ok "Using psql: $psql"

    $probe = & $psql -h 127.0.0.1 -p 5432 -U $DbUser -d postgres -tAc "SELECT 1" 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Fail "Cannot connect to PostgreSQL as $DbUser on 127.0.0.1:5432"
        Write-Host "    $probe" -ForegroundColor Red
        Write-WarnMsg "If you just installed Postgres, set the superuser password to match -DbPassword (default: root),"
        Write-WarnMsg "or run: .\run-local.ps1 -DbPassword YOUR_POSTGRES_PASSWORD"
        throw "Fix DB credentials (pass -DbUser / -DbPassword) or update application-local.yml, then retry."
    }
    Write-Ok "Connected to PostgreSQL as $DbUser"

    $exists = & $psql -h 127.0.0.1 -p 5432 -U $DbUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='staffcore33_ats'" 2>&1
    if (($exists | Out-String).Trim() -ne "1") {
        Write-WarnMsg "Creating database staffcore33_ats..."
        & $psql -h 127.0.0.1 -p 5432 -U $DbUser -d postgres -c "CREATE DATABASE staffcore33_ats;" | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to create database staffcore33_ats"
        }
        Write-Ok "Database staffcore33_ats created"
    } else {
        Write-Ok "Database staffcore33_ats already exists"
    }
}

function Ensure-FrontendDeps {
    if ($SkipNpmInstall) {
        Write-WarnMsg "Skipping npm install (-SkipNpmInstall)"
        return
    }

    Write-Step "Ensuring frontend npm packages"
    Update-SessionPath
    $nodeModules = Join-Path $FrontendDir "node_modules"
    $packageLock = Join-Path $FrontendDir "package-lock.json"

    $needsInstall = $false
    if (-not (Test-Path $nodeModules)) {
        $needsInstall = $true
        Write-WarnMsg "node_modules missing - running npm install..."
    } elseif (-not (Test-Path (Join-Path $nodeModules "next"))) {
        $needsInstall = $true
        Write-WarnMsg "Frontend packages incomplete - running npm install..."
    }

    if ($needsInstall) {
        Push-Location $FrontendDir
        try {
            if (Test-Path $packageLock) {
                npm ci
                if ($LASTEXITCODE -ne 0) {
                    Write-WarnMsg "npm ci failed - falling back to npm install..."
                    npm install
                }
            } else {
                npm install
            }
            if ($LASTEXITCODE -ne 0) { throw "npm install failed" }
            Write-Ok "Frontend packages installed"
        } finally {
            Pop-Location
        }
    } else {
        Write-Ok "Frontend node_modules present"
    }
}

function Ensure-BackendDeps {
    Write-Step "Ensuring backend Maven packages"
    Update-SessionPath
    Push-Location $BackendDir
    try {
        Write-WarnMsg "Downloading Maven dependencies (first run may take a few minutes)..."
        mvn -DskipTests dependency:resolve
        if ($LASTEXITCODE -ne 0) {
            Write-WarnMsg "dependency:resolve reported issues - spring-boot:run will retry downloads"
        } else {
            Write-Ok "Maven dependencies resolved"
        }
    } finally {
        Pop-Location
    }
}

function Start-BackendService {
    Write-Step "Starting backend on :$BackendPort"

    $pathLine = Get-PathExportSnippet
    $javaHomeLine = ""
    if ($env:JAVA_HOME) {
        $javaHomeLine = "`$env:JAVA_HOME = '$($env:JAVA_HOME)'"
    }

    $lines = @(
        $pathLine
        $javaHomeLine
        "`$env:SPRING_PROFILES_ACTIVE = 'local'"
        "`$env:SERVER_PORT = '$BackendPort'"
        "`$env:DB_USER = '$DbUser'"
        "`$env:DB_PASSWORD = '$DbPassword'"
        "`$env:FILE_UPLOAD_DIR = '$DeviceDir'"
        "`$env:CORS_ORIGINS = 'http://localhost:$FrontendPort,http://127.0.0.1:$FrontendPort'"
        "Set-Location -LiteralPath '$BackendDir'"
        "Write-Host 'Staffcore33 API - uploads: $DeviceDir' -ForegroundColor Green"
        "Write-Host 'DB: staffcore33_ats @ 127.0.0.1:5432 (user=$DbUser)' -ForegroundColor Green"
        "mvn -DskipTests spring-boot:run"
    ) | Where-Object { $_ -and $_.Trim() -ne "" }

    $backendScript = Join-Path $LogsDir "start-backend.ps1"
    Set-Content -Path $backendScript -Value ($lines -join "`r`n") -Encoding UTF8

    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-File", $backendScript
    ) -WorkingDirectory $BackendDir | Out-Null

    Write-Ok "Backend window launched (Spring Boot / Maven)"
}

function Start-FrontendService {
    Write-Step "Starting frontend on :$FrontendPort"

    $pathLine = Get-PathExportSnippet
    $lines = @(
        $pathLine
        "Set-Location -LiteralPath '$FrontendDir'"
        "Write-Host 'Staffcore33 Web - http://localhost:$FrontendPort' -ForegroundColor Green"
        "npm run dev"
    ) | Where-Object { $_ -and $_.Trim() -ne "" }

    $frontendScript = Join-Path $LogsDir "start-frontend.ps1"
    Set-Content -Path $frontendScript -Value ($lines -join "`r`n") -Encoding UTF8

    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-File", $frontendScript
    ) -WorkingDirectory $FrontendDir | Out-Null

    Write-Ok "Frontend window launched (Next.js)"
}

function Wait-PortOpen {
    param(
        [int]$Port,
        [int]$TimeoutSec = 120
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $client = New-Object System.Net.Sockets.TcpClient
            $iar = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
            $ok = $iar.AsyncWaitHandle.WaitOne(1000, $false)
            if ($ok -and $client.Connected) {
                $client.EndConnect($iar)
                $client.Close()
                return $true
            }
            $client.Close()
        } catch {
            # not ready yet
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

# --- main ---
Write-Host ""
Write-Host "Staffcore33 ATS - local Windows runner" -ForegroundColor White
Write-Host "Root: $Root"

Ensure-Folders
Ensure-Prerequisites
Ensure-Postgres

Write-Step "Freeing ports if occupied"
Clear-Port -Port $BackendPort -Label "backend"
Clear-Port -Port $FrontendPort -Label "frontend"

Ensure-FrontendDeps
Ensure-BackendDeps
Start-BackendService

Write-Step "Waiting for backend to accept connections"
$apiReady = Wait-PortOpen -Port $BackendPort -TimeoutSec 180
if ($apiReady) {
    Write-Ok "Backend is listening on :$BackendPort"
} else {
    Write-WarnMsg "Backend did not open :$BackendPort within 180s - frontend will still start; check the API window for errors"
}

Start-FrontendService

Write-Host ""
Write-Host "--------------------------------------------" -ForegroundColor White
Write-Host " App:      http://localhost:$FrontendPort" -ForegroundColor Green
Write-Host " API:      http://127.0.0.1:$BackendPort" -ForegroundColor Green
Write-Host " Database: staffcore33_ats @ 127.0.0.1:5432" -ForegroundColor Green
Write-Host " Uploads:  $DeviceDir" -ForegroundColor Green
Write-Host " Tools:    $ToolsDir" -ForegroundColor Green
Write-Host " Login:    admin@staffcore33.com / Admin@123" -ForegroundColor Green
Write-Host "--------------------------------------------" -ForegroundColor White
Write-Host "Re-run this script anytime to free ports and restart both services."
Write-Host ""
