Add-Type -AssemblyName System.Drawing

function Remove-BlackBackground {
    param (
        [string]$imagePath
    )

    try {
        $img = [System.Drawing.Bitmap]::FromFile($imagePath)
        $newImg = New-Object System.Drawing.Bitmap($img.Width, $img.Height)
        $newImg.SetResolution($img.HorizontalResolution, $img.VerticalResolution)

        $graphics = [System.Drawing.Graphics]::FromImage($newImg)
        $graphics.DrawImage($img, 0, 0, $img.Width, $img.Height)
        $graphics.Dispose()
        $img.Dispose()

        for ($x = 0; $x -lt $newImg.Width; $x++) {
            for ($y = 0; $y -lt $newImg.Height; $y++) {
                $pixel = $newImg.GetPixel($x, $y)
                # Check for dark pixels (R, G, B < 30)
                if ($pixel.R -lt 30 -and $pixel.G -lt 30 -and $pixel.B -lt 30) {
                    $newImg.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
                }
            }
        }

        $newImg.Save($imagePath, [System.Drawing.Imaging.ImageFormat]::Png)
        $newImg.Dispose()
        Write-Host "Processed: $imagePath"
    }
    catch {
        Write-Host "Error processing $imagePath : $_"
    }
}

$files = @(
    "ic_upgrade_aim.png",
    "ic_upgrade_energy.png",
    "ic_upgrade_luck.png",
    "ic_upgrade_shield.png"
)

$basePath = "c:\Users\raufk\OneDrive\Desktop\SpaceBillardOnline\app\src\main\res\drawable"

foreach ($f in $files) {
    $fullPath = Join-Path $basePath $f
    if (Test-Path $fullPath) {
        Remove-BlackBackground $fullPath
    } else {
        Write-Host "File not found: $fullPath"
    }
}
