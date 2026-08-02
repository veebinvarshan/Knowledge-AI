$directories = @(
    "frontend\features\chat",
    "frontend\features\document-viewer",
    "frontend\features\search",
    "frontend\features\dashboard",
    "frontend\features\admin",
    "frontend\components\ui",
    "frontend\components\layouts",
    "frontend\shared\types",
    "frontend\shared\hooks",
    "frontend\shared\providers",
    "frontend\shared\services",
    "frontend\shared\styles",
    "frontend\shared\constants",
    "frontend\lib"
)

$basePath = "d:\Knowledge"

foreach ($dir in $directories) {
    $fullPath = Join-Path $basePath $dir
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath -Force
    }
    
    $keepFile = Join-Path $fullPath ".gitkeep"
    Set-Content -Path $keepFile -Value "" -Force
    Write-Host "Created .gitkeep in $fullPath"
}
