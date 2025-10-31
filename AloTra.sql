-- =============================================
-- BƯỚC 0: XÓA DATABASE CŨ VÀ TẠO DATABASE MỚI
-- =============================================
USE master;
GO

IF DB_ID('AloTra') IS NOT NULL
    DROP DATABASE AloTra;
GO

CREATE DATABASE AloTra;
GO

USE AloTra;
GO

-- =============================================
-- BẢNG KHÔNG CÓ KHÓA NGOẠI (TẠO TRƯỚC)
-- =============================================

-- 1. Vai trò (Roles)
CREATE TABLE VaiTro (
    MaVaiTro INT PRIMARY KEY,
    TenVaiTro NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- 2. Hạng thành viên (Membership Tiers)
CREATE TABLE HangThanhVien (
    MaHangThanhVien INT IDENTITY(1,1) PRIMARY KEY,
    TenHang NVARCHAR(50) NOT NULL UNIQUE,
    DiemToiThieu INT NOT NULL,
    PhanTramGiamGia DECIMAL(5, 2) DEFAULT 0
);
GO

-- 6. Danh mục (Categories)
CREATE TABLE DanhMuc (
    MaDanhMuc INT IDENTITY(1,1) PRIMARY KEY,
    TenDanhMuc NVARCHAR(150) NOT NULL,
    HinhAnh NVARCHAR(180) UNIQUE,
    KichHoat BIT NOT NULL DEFAULT 1
);
GO

-- 7. Cửa hàng (Brands)
CREATE TABLE CuaHang (
    MaCuaHang INT IDENTITY(1,1) PRIMARY KEY,
    TenCuaHang NVARCHAR(150) NOT NULL,
    HinhAnh NVARCHAR(180) UNIQUE,
    MoTa NVARCHAR(MAX),
    KichHoat BIT NOT NULL DEFAULT 1
);
GO

-- 10. Nhà cung cấp (Suppliers)
CREATE TABLE NhaCungCap (
    MaNCC INT IDENTITY(1,1) PRIMARY KEY,
    TenNCC NVARCHAR(50) NOT NULL,
    SDT NVARCHAR(15) NOT NULL,
    DiaChi NVARCHAR(200) NOT NULL
);
GO

-- Nhà vận chuyển (Shipping Carriers)
CREATE TABLE NhaVanChuyen (
    MaNVC INT IDENTITY(1,1) PRIMARY KEY,
    TenNVC NVARCHAR(150) NOT NULL UNIQUE,
    SoDienThoai NVARCHAR(20),
    Website NVARCHAR(255)
);
GO

CREATE TABLE PhiVanChuyen (
    MaChiPhiVC INT IDENTITY(1,1) PRIMARY KEY,
    TenGoiCuoc NVARCHAR(200) NOT NULL,    
    MaNVC INT NOT NULL FOREIGN KEY REFERENCES NhaVanChuyen(MaNVC) ON DELETE CASCADE, 
    PhuongThucVanChuyen NVARCHAR(50) NOT NULL,
    ChiPhi DECIMAL(18, 2) NOT NULL CHECK (ChiPhi >= 0),
    NgayGiaoSomNhat INT NOT NULL,
    NgayGiaoMuonNhat INT NOT NULL,
    DonViThoiGian NVARCHAR(20) NOT NULL DEFAULT N'Ngày',
	CHECK (NgayGiaoMuonNhat >= NgayGiaoSomNhat)
);
GO

CREATE TABLE PhiApDungTungTinh (
    MaChiPhiVC INT NOT NULL
    FOREIGN KEY REFERENCES PhiVanChuyen(MaChiPhiVC) ON DELETE CASCADE,
    TenTinhThanh NVARCHAR(100) NOT NULL,
    PRIMARY KEY (MaChiPhiVC, TenTinhThanh)
);
GO

-- =============================================
-- BẢNG CÓ KHÓA NGOẠI (CẤP 1)
-- =============================================

-- 3. Người dùng (Users)
CREATE TABLE NguoiDung (
    MaNguoiDung INT IDENTITY(1,1) PRIMARY KEY,
    Email NVARCHAR(255) NOT NULL UNIQUE,
    TenDangNhap NVARCHAR(100) UNIQUE,
    MatKhau NVARCHAR(255) NOT NULL,
    HoTen NVARCHAR(150) NOT NULL,
    SoDienThoai NVARCHAR(20),
    MaVaiTro INT NOT NULL,
    MaHangThanhVien INT,
    TrangThai TINYINT DEFAULT 1 CHECK (TrangThai IN (0, 1)),
    AnhDaiDien NVARCHAR(500),
	DiemTichLuy INT NOT NULL DEFAULT 0,
	XacThucEmail BIT DEFAULT 0,
    NgayTao DATETIME2 DEFAULT SYSUTCDATETIME(),
    NgayCapNhat DATETIME2,
    FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro),
    FOREIGN KEY (MaHangThanhVien) REFERENCES HangThanhVien(MaHangThanhVien)
);
GO

-- 8. Sản phẩm (Products)
CREATE TABLE SanPham (
    MaSanPham INT IDENTITY(1,1) PRIMARY KEY,
    TenSanPham NVARCHAR(255) NOT NULL,
    MaDanhMuc INT,
    MaCuaHang INT,
    MoTa NVARCHAR(MAX),
    GiaBan DECIMAL(18, 2) NOT NULL,
    GiaNiemYet DECIMAL(18, 2) NOT NULL,
    HanSuDung INT,
    HinhAnh NVARCHAR(500) NOT NULL,
    KichHoat BIT NOT NULL DEFAULT 1,
    NgayTao DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CHK_SanPham_Gia CHECK (GiaNiemYet >= GiaBan AND GiaBan > 0),
    FOREIGN KEY (MaDanhMuc) REFERENCES DanhMuc(MaDanhMuc),
    FOREIGN KEY (MaCuaHang) REFERENCES CuaHang(MaCuaHang)
);
GO

-- 11. Phiếu nhập (Goods Receipt - Header)
CREATE TABLE PhieuNhap (
    MaPhieuNhap INT IDENTITY(1,1) PRIMARY KEY,
    MaNCC INT,
    NgayTao DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (MaNCC) REFERENCES NhaCungCap(MaNCC)
);
GO

-- 13. Khuyến mãi (Promotions)
CREATE TABLE KhuyenMai (
    MaKhuyenMai NVARCHAR(30) PRIMARY KEY,
    TenChienDich NVARCHAR(200) NOT NULL,
    KieuApDung TINYINT CHECK (KieuApDung IN (0, 1)),
    GiaTri DECIMAL(18, 2) NOT NULL,
    BatDauLuc DATETIME2 NOT NULL,
    KetThucLuc DATETIME2 NOT NULL,
    TongTienToiThieu DECIMAL(18, 2) DEFAULT 0,
    GiamToiDa DECIMAL(18, 2),
    GioiHanTongSoLan INT,
    GioiHanMoiNguoi INT,
    TrangThai TINYINT CHECK (TrangThai IN (0, 1, 2)),
    NgayTao DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CHK_KhuyenMai_ThoiGian CHECK (KetThucLuc > BatDauLuc)
);
GO

-- =============================================
-- BẢNG CÓ KHÓA NGOẠI (CẤP 2)
-- =============================================

-- 4. Mã xác thực (OTP Codes)
CREATE TABLE MaXacThuc (
    MaOtp UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    MaNguoiDung INT NULL,                   
    Email NVARCHAR(255),                     
    MaSo NVARCHAR(10) NOT NULL,
    MucDich NVARCHAR(30) CHECK (MucDich IN (N'Đăng ký', N'Quên mật khẩu', N'Đăng nhập')),
    HetHanLuc DATETIME2 NOT NULL,
    DaSuDung BIT DEFAULT 0,
    NgayTao DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- 5. Địa chỉ (Addresses)
CREATE TABLE DiaChi (
    MaDiaChi INT IDENTITY(1,1) PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    TenNguoiNhan NVARCHAR(150) NOT NULL,
    SoDienThoai NVARCHAR(20) NOT NULL,
    TinhThanh NVARCHAR(100) NOT NULL,
    QuanHuyen NVARCHAR(100) NOT NULL,
    PhuongXa NVARCHAR(100) NOT NULL,
    SoNhaDuong NVARCHAR(255) NOT NULL,
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- 9. Sản phẩm yêu thích (Wishlist)
CREATE TABLE SanPhamYeuThich (
    MaSanPham INT NOT NULL,
    MaNguoiDung INT NOT NULL,
    PRIMARY KEY (MaSanPham, MaNguoiDung),
    FOREIGN KEY (MaSanPham) REFERENCES SanPham(MaSanPham),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- 11. Chi tiết phiếu nhập (Goods Receipt - Detail)
CREATE TABLE ChiTietPhieuNhap (
    MaPhieuNhap INT NOT NULL,
    MaSanPham INT NOT NULL,
    SoLuong INT NOT NULL,
    GiaNhap DECIMAL(18, 2) NOT NULL,
    ThanhTien AS (SoLuong * GiaNhap),
    PRIMARY KEY (MaPhieuNhap, MaSanPham), 
    FOREIGN KEY (MaPhieuNhap) REFERENCES PhieuNhap(MaPhieuNhap),
    FOREIGN KEY (MaSanPham) REFERENCES SanPham(MaSanPham),
    CONSTRAINT CHK_ChiTietPhieuNhap_SoLuong CHECK (SoLuong > 0),
    CONSTRAINT CHK_ChiTietPhieuNhap_GiaNhap CHECK (GiaNhap > 0)
);
GO

-- 12. Kho hàng (Warehouse) - Đã đơn giản hóa
CREATE TABLE KhoHang (
    MaSanPham INT PRIMARY KEY,
    SoLuongTon INT NOT NULL DEFAULT 0,
    NgayNhapGanNhat DATETIME2,
    FOREIGN KEY (MaSanPham) REFERENCES SanPham(MaSanPham) ON DELETE CASCADE
);
GO

-- 14. Giỏ hàng (Shopping Cart)
CREATE TABLE GioHang (
    MaNguoiDung INT NOT NULL,
    MaSanPham INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18, 2) NOT NULL,
    ThanhTien AS (SoLuong * DonGia),
    PRIMARY KEY (MaNguoiDung, MaSanPham),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung) ON DELETE CASCADE,
    FOREIGN KEY (MaSanPham) REFERENCES SanPham(MaSanPham) ON DELETE CASCADE,
    CONSTRAINT CHK_GioHang_SoLuong CHECK (SoLuong > 0)
);
GO

-- 15. Đơn hàng (Orders)
CREATE TABLE DonHang (
    MaDonHang BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaNguoiDung INT,
    NgayDat DATETIME2 DEFAULT SYSUTCDATETIME(),
    TrangThai NVARCHAR(30) CHECK (TrangThai IN (N'Đang xử lý', N'Đã xác nhận', N'Đang giao', N'Đã giao', N'Trả hàng-Hoàn tiền', N'Đã hủy')),
    PhuongThucThanhToan NVARCHAR(20) CHECK (PhuongThucThanhToan IN (N'COD', N'ONLINE')),
    TrangThaiThanhToan NVARCHAR(20) CHECK (TrangThaiThanhToan IN (N'Chưa thanh toán', N'Đã thanh toán')),
    TienHang DECIMAL(18, 2) NOT NULL CHECK (TienHang >= 0),
    MaKhuyenMai NVARCHAR(30),
	PhuongThucVanChuyen NVARCHAR(50) NOT NULL,
    PhiVanChuyen DECIMAL(18, 2) NOT NULL DEFAULT 0 CHECK (PhiVanChuyen >= 0),
    TongTien DECIMAL(18, 2) NOT NULL,
    TenNguoiNhan NVARCHAR(150) NOT NULL,
    SoDienThoaiNhan NVARCHAR(20) NOT NULL,
    DiaChiNhan NVARCHAR(500) NOT NULL,
    MaDiaChiNhan INT,
    GhiChu NVARCHAR(500),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    FOREIGN KEY (MaKhuyenMai) REFERENCES KhuyenMai(MaKhuyenMai),
    FOREIGN KEY (MaDiaChiNhan) REFERENCES DiaChi(MaDiaChi)
);
GO

-- 19. Đánh giá (Reviews)
CREATE TABLE DanhGia (
    MaSanPham INT NOT NULL,
    MaNguoiDung INT NOT NULL,
    DiemDanhGia TINYINT NOT NULL CHECK (DiemDanhGia BETWEEN 1 AND 5),
    BinhLuan NVARCHAR(500),
    NgayTao DATETIME2 DEFAULT SYSUTCDATETIME(),
	ImageUrl NVARCHAR(255),
	VideoUrl NVARCHAR(255)
    PRIMARY KEY (MaSanPham, MaNguoiDung),
    FOREIGN KEY (MaSanPham) REFERENCES SanPham(MaSanPham),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- 20. Liên hệ (Contacts)
CREATE TABLE LienHe (
    MaLienHe BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaNguoiDung INT,
    ChuDe NVARCHAR(200),
    NoiDung NVARCHAR(MAX) NOT NULL,
    TrangThai NVARCHAR(20) DEFAULT N'Mới' CHECK (TrangThai IN (N'Mới', N'Đã xử lý')),
    NgayGui DATETIME2 DEFAULT SYSUTCDATETIME(),
    MaQuanTriVien INT,
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    FOREIGN KEY (MaQuanTriVien) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- =============================================
-- BẢNG CÓ KHÓA NGOẠI (CẤP 3 - LIÊN QUAN ĐẾN ĐƠN HÀNG)
-- =============================================

-- 16. Đơn hàng chi tiết (Order Details)
CREATE TABLE DonHang_ChiTiet (
    MaDonHang BIGINT NOT NULL,
    MaSanPham INT NOT NULL,
    TenSanPham NVARCHAR(255) NOT NULL,
    DonGia DECIMAL(18, 2) NOT NULL CHECK (DonGia >= 0),
    SoLuong INT NOT NULL CHECK (SoLuong > 0),
    ThanhTien AS (SoLuong * DonGia),
    PRIMARY KEY (MaDonHang, MaSanPham),
    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang) ON DELETE CASCADE,
    FOREIGN KEY (MaSanPham) REFERENCES SanPham(MaSanPham)
);
GO

-- 17. Lịch sử trạng thái đơn hàng (Order Status History)
CREATE TABLE LichSuTrangThaiDon (
    MaLichSu BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaDonHang BIGINT NOT NULL,
    TuTrangThai NVARCHAR(30) NOT NULL,
    DenTrangThai NVARCHAR(30) NOT NULL,
    MaQuanTriVien INT,
    ThoiDiemThayDoi DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
    FOREIGN KEY (MaQuanTriVien) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- 18. Vận chuyển (Shipping)
CREATE TABLE VanChuyen (
    MaVanChuyen BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaDonHang BIGINT NOT NULL,
    MaNVC INT,
    MaVanDon NVARCHAR(100),
    GuiLuc DATETIME2,
    GiaoLuc DATETIME2,
    TrangThai NVARCHAR(20) CHECK (TrangThai IN (N'Đã khởi tạo', N'Đang giao', N'Đã giao', N'Trả hàng')),
    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
    FOREIGN KEY (MaNVC) REFERENCES NhaVanChuyen(MaNVC)
);
GO

USE OneShop;
GO

-- Xóa bảng cũ nếu có
IF OBJECT_ID('TinNhanChat', 'U') IS NOT NULL
    DROP TABLE TinNhanChat;
GO

IF OBJECT_ID('PhienChat', 'U') IS NOT NULL
    DROP TABLE PhienChat;
GO

-- Tạo lại bảng PhienChat
CREATE TABLE PhienChat (
    MaPhienChat NVARCHAR(100) PRIMARY KEY,
    MaNguoiDung INT,                            
    TenKhach NVARCHAR(150),                     
    EmailKhach NVARCHAR(255),                   
    TinNhanDauTien DATETIME2,                   
    TinNhanCuoiCung DATETIME2,                  
    TrangThai NVARCHAR(20) DEFAULT N'Đang mở'   
        CHECK (TrangThai IN (N'Đang mở', N'Đã đóng')),
    SoTinChuaDoc INT DEFAULT 0,                 
    
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    INDEX idx_tin_nhan_cuoi (TinNhanCuoiCung DESC)
);
GO

-- Tạo lại bảng TinNhanChat (QUAN TRỌNG: Có MaNguoiDung)
CREATE TABLE TinNhanChat (
    MaTinNhan BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaPhienChat NVARCHAR(100) NOT NULL,         
    MaNguoiDung INT,                            -- ✅ CỘT NÀY QUAN TRỌNG
    NoiDung NVARCHAR(MAX) NOT NULL,             
    LoaiNguoiGui NVARCHAR(20) NOT NULL          
        CHECK (LoaiNguoiGui IN (N'CUSTOMER', N'ADMIN')),
    ThoiGian DATETIME2 DEFAULT SYSUTCDATETIME(),
    DaXem BIT DEFAULT 0,                        
    
    FOREIGN KEY (MaPhienChat) REFERENCES PhienChat(MaPhienChat),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),  -- ✅ FOREIGN KEY
    INDEX idx_phien_chat (MaPhienChat, ThoiGian)
);
GO


CREATE TABLE DangKyTamThoi (
    MaDangKy INT IDENTITY(1,1) PRIMARY KEY,
    Email NVARCHAR(255) NOT NULL UNIQUE,
    TenDangNhap NVARCHAR(100) NOT NULL,
    MatKhau NVARCHAR(255) NOT NULL,
    HoTen NVARCHAR(150) NOT NULL,
    NgayTao DATETIME2 DEFAULT SYSUTCDATETIME(),
    HetHanLuc DATETIME2 NOT NULL

);
GO

PRINT N'✅ Đã tạo lại bảng thành công!';
GO

-- Kiểm tra cấu trúc bảng
EXEC sp_columns 'TinNhanChat';
GO
