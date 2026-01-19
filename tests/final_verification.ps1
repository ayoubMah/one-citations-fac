$ErrorActionPreference = 'Stop'

function Get-Token {
    param ($username, $password)
    $body = @{
        client_id = "test-client"
        username = $username
        password = $password
        grant_type = "password"
    }
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8085/realms/one-citations/protocol/openid-connect/token" -Method Post -Body $body -UseBasicParsing
        return $response.access_token
    } catch {
        Write-Error "Failed to get token for $username : $_"
        exit 1
    }
}

function Test-Endpoint {
    param ($name, $uri, $method="Get", $headers=@{}, $body=$null, $expectStatus=200)
    Write-Host "Testing $name..." -NoNewline
    try {
        $params = @{
            Uri = $uri
            Method = $method
            Headers = $headers
            ErrorAction = "Stop"
            UseBasicParsing = $true
        }
        if ($body) { 
            $params.Body = $body 
            $params.ContentType = "application/json"
        }

        $response = Invoke-WebRequest @params
        
        $status = [int]$response.StatusCode
        if ($status -eq $expectStatus) {
            Write-Host " OK ($status)" -ForegroundColor Green
            try {
                return $response.Content | ConvertFrom-Json
            } catch {
                return $response.Content
            }
        } else {
            Write-Host " FAILED (Expected $expectStatus, got $status)" -ForegroundColor Red
            return $null
        }
    } catch {
        $ex = $_.Exception.Response
        if ($ex) {
             # PS 5.1 WebException wrapper
             try {
                $status = [int]$ex.StatusCode
             } catch {
                $status = 0
             }
             if ($status -eq $expectStatus) {
                Write-Host " OK ($status)" -ForegroundColor Green
                try {
                     # Read stream from exception response for body
                     $stream = $ex.GetResponseStream()
                     $reader = New-Object System.IO.StreamReader($stream)
                     $content = $reader.ReadToEnd()
                     return $content | ConvertFrom-Json
                } catch {
                     return $null
                }
             }
        }
        Write-Host " ERROR: $_" -ForegroundColor Red
        return $null
    }
}

# 1. Public Image
$apiKey = "one-citations-api-key"
Write-Host "Testing Public Random Image..." -NoNewline
try {
    # Request just headers for the image to verify 200 OK and content-type
    $imgResp = Invoke-WebRequest -Uri "http://localhost:8000/api/v1/images/random?width=100&height=100&apikey=$apiKey" -Method Head -ErrorAction Stop -UseBasicParsing
    if ($imgResp.StatusCode -eq 200 -and $imgResp.Headers["Content-Type"] -eq "image/jpeg") {
        Write-Host " OK (200 image/jpeg)" -ForegroundColor Green
    } else {
        Write-Host " FAILED ($($imgResp.StatusCode) $($imgResp.Headers["Content-Type"]))" -ForegroundColor Red
    }
} catch {
    Write-Host " ERROR: $_" -ForegroundColor Red
} 

# 2. Login Writer
$writerToken = Get-Token "writer" "writer"
Write-Host "Writer Token obtained."

# 3. Submit Citation (Writer)
$citationText = "This is a test citation $(Get-Date -Format 'yyyyMMdd-HHmmss')"
$headersWriter = @{
    "Authorization" = "Bearer $writerToken"
    "apikey" = $apiKey
}
$bodyCitation = @{
    text = $citationText
} | ConvertTo-Json

$createdCitation = Test-Endpoint "Submit Citation" "http://localhost:8000/api/v1/citations" -Method Post -Headers $headersWriter -Body $bodyCitation -ExpectStatus 200

# 4. Login Moderator
$modToken = Get-Token "moderator" "moderator"
Write-Host "Moderator Token obtained."

# 5. List Pending (Moderator)
$headersMod = @{
    "Authorization" = "Bearer $modToken"
    "apikey" = $apiKey
}
$pendingList = Test-Endpoint "List Pending Citations" "http://localhost:8000/api/v1/citations/pending" -Headers $headersMod

# Find the citation we just created
if ($pendingList) {
    # Spring Page object has 'content' array
    $items = $pendingList.content
    if (-not $items) { $items = $pendingList } # Fallback if not paginated
    
    $target = $items | Where-Object { $_.text -eq $citationText }
    if ($target) {
        Write-Host "Found submitted citation ID: $($target.id)" -ForegroundColor Cyan
        
        # 6. Validate Citation (Moderator)
        Test-Endpoint "Validate Citation" "http://localhost:8000/api/v1/citations/$($target.id)/validate" -Method Put -Headers $headersMod
        
        # 7. Public Random Citation
        # Should now potentially return our validated citation
        # We might need to try a few times if there are many, but here we just check 200 OK
        Test-Endpoint "Get Random Citation" "http://localhost:8000/api/v1/citations/random?apikey=$apiKey"
        
    } else {
        Write-Host "Could not find the submitted citation in pending list!" -ForegroundColor Red
    }
}
