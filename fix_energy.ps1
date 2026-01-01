Add-Type -AssemblyName System.Drawing

$f = "c:\Users\raufk\OneDrive\Desktop\SpaceBillardOnline\app\src\main\res\drawable\ic_upgrade_energy.png"

try {
    $img = [System.Drawing.Bitmap]::FromFile($f)
    $newImg = New-Object System.Drawing.Bitmap($img.Width, $img.Height)
    $newImg.SetResolution($img.HorizontalResolution, $img.VerticalResolution)

    $graphics = [System.Drawing.Graphics]::FromImage($newImg)
    $graphics.DrawImage($img, 0, 0, $img.Width, $img.Height)
    $graphics.Dispose()
    $img.Dispose()

    for ($x = 0; $x -lt $newImg.Width; $x++) {
        for ($y = 0; $y -lt $newImg.Height; $y++) {
            $pixel = $newImg.GetPixel($x, $y)
            # Higher threshold (50) to catch dark purple artifacts
            if ($pixel.R -lt 50 -and $pixel.G -lt 50 -and $pixel.B -lt 50) {
                $newImg.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
            }
        }
    }

    $newImg.Save($f, [System.Drawing.Imaging.ImageFormat]::Png)
    $newImg.Dispose()
    Write-Host "Reprocessed Energy Icon"
}
catch {
    Write-Host "Error: $_"
}
