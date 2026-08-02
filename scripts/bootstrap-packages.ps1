$modules = @(
    "authentication",
    "users",
    "organizations",
    "documents",
    "folders",
    "tags",
    "knowledge",
    "chat",
    "search",
    "analytics",
    "notifications",
    "settings",
    "administration",
    "audit",
    "shared"
)

$basePath = "d:\Knowledge\backend\src\main\java\com\enterprise\platform\modules"

if (-not (Test-Path $basePath)) {
    New-Item -ItemType Directory -Path $basePath -Force
}

foreach ($module in $modules) {
    $modulePath = Join-Path $basePath $module
    if (-not (Test-Path $modulePath)) {
        New-Item -ItemType Directory -Path $modulePath -Force
    }
    
    # Sub-packages for Clean Architecture
    $subpackages = @("api", "domain", "service", "adapter")
    foreach ($sub in $subpackages) {
        $subPath = Join-Path $modulePath $sub
        if (-not (Test-Path $subPath)) {
            New-Item -ItemType Directory -Path $subPath -Force
        }
        
        $packageInfoContent = "package com.enterprise.platform.modules.$module.$sub;" + [Environment]::NewLine
        $filePath = Join-Path $subPath "package-info.java"
        Set-Content -Path $filePath -Value $packageInfoContent -Force
        Write-Host "Created package-info.java in $subPath"
    }
}
