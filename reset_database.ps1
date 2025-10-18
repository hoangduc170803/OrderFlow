# PowerShell script để reset database
# CẢNH BÁO: Script này sẽ xóa toàn bộ dữ liệu trong database

Write-Host "=== RESET DATABASE SCRIPT ===" -ForegroundColor Red
Write-Host "CẢNH BÁO: Script này sẽ xóa toàn bộ dữ liệu trong database flower_shop!" -ForegroundColor Yellow
Write-Host ""

$confirm = Read-Host "Bạn có chắc chắn muốn tiếp tục? (yes/no)"
if ($confirm -ne "yes") {
    Write-Host "Hủy bỏ thao tác." -ForegroundColor Green
    exit
}

Write-Host "Đang reset database..." -ForegroundColor Yellow

# Thay đổi đường dẫn MySQL nếu cần
$mysqlPath = "mysql"
$username = "root"
$password = "170803"
$database = "flower_shop"

try {
    # Chạy script SQL
    Get-Content "reset_database.sql" | & $mysqlPath -u $username -p$password
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Database đã được reset thành công!" -ForegroundColor Green
        Write-Host "Bây giờ bạn có thể chạy ứng dụng Spring Boot." -ForegroundColor Green
    } else {
        Write-Host "❌ Có lỗi xảy ra khi reset database." -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Lỗi: $_" -ForegroundColor Red
    Write-Host "Hãy kiểm tra:" -ForegroundColor Yellow
    Write-Host "1. MySQL đã được cài đặt và đang chạy" -ForegroundColor Yellow
    Write-Host "2. Username và password đúng" -ForegroundColor Yellow
    Write-Host "3. Đường dẫn MySQL đúng" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Nhấn Enter để thoát..."
Read-Host
