# Enable TLS 1.2
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$testFilesDir = "d:\Knowledge\test-files"
if (!(Test-Path -Path $testFilesDir)) {
    New-Item -ItemType Directory -Path $testFilesDir -Force | Out-Null
}

function Download-File {
    param (
        [string]$url,
        [string]$destination
    )
    Write-Output "Downloading $url to $destination..."
    try {
        $wc = New-Object System.Net.WebClient
        $wc.Headers.Add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        $wc.DownloadFile($url, $destination)
        Write-Output "Successfully downloaded to $destination"
    } catch {
        Write-Warning "Failed to download $url : $_"
    }
}

# 1. Text PDF
Download-File "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf" "$testFilesDir\text-pdf.pdf"

# 2. Scanned PDF
Download-File "https://raw.githubusercontent.com/alexschiller/file-format-commons/master/files/ffc.pdf" "$testFilesDir\scanned-pdf.pdf"

# 3. DOCX
Download-File "https://raw.githubusercontent.com/alexschiller/file-format-commons/master/files/ffc.docx" "$testFilesDir\sample.docx"

# 4. Image PNG
Download-File "https://raw.githubusercontent.com/alexschiller/file-format-commons/master/files/ffc.png" "$testFilesDir\sample-image.png"

# 5. Image JPG
Download-File "https://raw.githubusercontent.com/mathiasbynens/small/master/jpeg.jpg" "$testFilesDir\sample-image.jpg"

# 6. TXT
$txtContent = "Enterprise Knowledge Platform validation document. This is a text file that contains plain text context about platform deployment guidelines."
[IO.File]::WriteAllText("$testFilesDir\sample.txt", $txtContent)
Write-Output "Created sample.txt"

# 7. CSV
$csvContent = "id,name,role,status`r`n1,Alice,ROLE_ORG_ADMIN,ACTIVE`r`n2,Bob,ROLE_VIEWER,PENDING"
[IO.File]::WriteAllText("$testFilesDir\sample.csv", $csvContent)
Write-Output "Created sample.csv"

# 8. XLSX
Download-File "https://raw.githubusercontent.com/alexschiller/file-format-commons/master/files/ffc.xlsx" "$testFilesDir\sample.xlsx"

# 9. PPTX
Download-File "https://raw.githubusercontent.com/alexschiller/file-format-commons/master/files/ffc.pptx" "$testFilesDir\sample.pptx"

# 10. Large PDF
Download-File "https://docs.spring.io/spring-boot/docs/2.7.18/reference/pdf/spring-boot-reference.pdf" "$testFilesDir\large-file.pdf"
