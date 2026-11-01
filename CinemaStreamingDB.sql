-- ============================================================================
-- HỆ THỐNG CƠ SỞ DỮ LIỆU: CINEMA STREAMING PLATFORM (TVHAY x NETFLIX CLONE)
-- PHIÊN BẢN HOÀN CHỈNH ĐỒ ÁN: 
--   1. Hỗ trợ Session Login/Logout MD5 + Bảng users chuẩn bài tập
--   2. 30 Tài khoản (Đủ Admin, Nhân viên, Khách VIP/Thường, Khách vãng lai)
--   3. 200 Phim (Hoạt hình, Kinh dị, Hành động, Tình cảm... Đủ Lồng tiếng, Thuyết minh, Vietsub, Trailer)
--   4. Tích hợp VietQR tự động, Gói cước SVOD, Vé thuê 48H TVOD & Demo Thanh toán
-- ============================================================================

USE master;
GO

IF DB_ID(N'CinemaStreamingDB') IS NULL
BEGIN
    CREATE DATABASE CinemaStreamingDB;
END
GO

USE CinemaStreamingDB;
GO

-- XÓA SẠCH CÁC BẢNG CŨ THEO CHIỀU NGƯỢC LẠI TRÁNH LỖI KHÓA NGOẠI
DROP TABLE IF EXISTS users_lab_demo;
DROP TABLE IF EXISTS RentalRevokeLogs;
DROP TABLE IF EXISTS IssueReports;
DROP TABLE IF EXISTS Banners;
DROP TABLE IF EXISTS Comments;
DROP TABLE IF EXISTS Watchlists;
DROP TABLE IF EXISTS WatchHistory;
DROP TABLE IF EXISTS EpisodeServers;
DROP TABLE IF EXISTS MovieEpisodes;
DROP TABLE IF EXISTS MovieRentals;
DROP TABLE IF EXISTS Transactions;
DROP TABLE IF EXISTS MovieGenres;
DROP TABLE IF EXISTS Genres;
DROP TABLE IF EXISTS Movies;
DROP TABLE IF EXISTS SubscriptionPlans;
DROP TABLE IF EXISTS UserTokens;
DROP TABLE IF EXISTS UserDevices;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Roles;
GO

-- ============================================================================
-- BẢNG PHỤ TƯƠNG THÍCH 100% VỚI SCRIPT BÀI TẬP SESSION - LOGIN/LOGOUT CỦA THẦY
-- ============================================================================
CREATE TABLE dbo.users_lab_demo (
    id INT IDENTITY(1,1) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    CONSTRAINT users_unique UNIQUE (username),
    CONSTRAINT users_pk PRIMARY KEY (id)
);

INSERT INTO dbo.users_lab_demo (username, password) VALUES 
('admin', 'e10adc3949ba59abbe56e057f20f883e'),      -- pass: 123456
('staff', 'e10adc3949ba59abbe56e057f20f883e'),      -- pass: 123456
('customer', '25d55ad283aa400af464c76d713c07ad'),   -- pass: 12345678
('guest', '96e79218965eb72c92a549dd5a330112');      -- pass: 111111
GO

-- ============================================================================
-- PHẦN 1: TÀI KHOẢN, PHÂN QUYỀN, THIẾT BỊ & PHIÊN ĐĂNG NHẬP (18 BẢNG CHÍNH)
-- ============================================================================

-- Bảng 1: Phân quyền vai trò người dùng (RBAC)
CREATE TABLE Roles (
    RoleId INT IDENTITY(1,1) PRIMARY KEY,
    RoleName VARCHAR(50) NOT NULL UNIQUE
);

-- Bảng 2: Tài khoản người dùng (Chuẩn MD5 Login/Logout & Đổi mật khẩu)
CREATE TABLE Users (
    UserId INT IDENTITY(1,1) PRIMARY KEY,
    UserCode VARCHAR(20) NOT NULL UNIQUE,          -- Định danh hội viên #TVH-XXXXX
    Username VARCHAR(50) NOT NULL UNIQUE,          -- Tên đăng nhập
    PasswordHash VARCHAR(255) NOT NULL,            -- Mật khẩu hash MD5 (mặc định e10adc3949ba59abbe56e057f20f883e)
    GoogleId VARCHAR(100) NULL,                    -- OAuth Google ID
    FullName NVARCHAR(100) NOT NULL,               -- Họ và tên
    Email VARCHAR(100) NOT NULL UNIQUE,            -- Email xác thực
    Phone VARCHAR(20) NULL UNIQUE,                 -- Số điện thoại
    AvatarUrl VARCHAR(500) DEFAULT '/assets/img/default-avatar.png',
    IsVip BIT DEFAULT 0,                           -- Cờ trạng thái VIP
    VipExpireDate DATETIME2 NULL,                  -- Ngày hết hạn gói VIP
    RoleId INT NOT NULL,                           -- 1: ADMIN, 2: STAFF, 3: CUSTOMER, 9: GUEST (Khách vãng lai)
    Theme VARCHAR(10) DEFAULT 'dark' NOT NULL,     -- 'dark' hoặc 'light'
    Status BIT DEFAULT 1,                          -- 1: Hoạt động, 0: Khóa tài khoản
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Users_Roles FOREIGN KEY (RoleId) REFERENCES Roles(RoleId)
);

-- Bảng 3: Quản lý thiết bị và kiểm soát giới hạn màn hình stream
CREATE TABLE UserDevices (
    DeviceId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    DeviceName NVARCHAR(150) NOT NULL,
    DeviceType VARCHAR(50) NOT NULL,               -- 'SMART_TV', 'DESKTOP', 'MOBILE', 'TABLET'
    IpAddress VARCHAR(50) NOT NULL,
    Location NVARCHAR(150) NULL,
    SessionToken VARCHAR(255) NOT NULL UNIQUE,
    LastActive DATETIME2 DEFAULT GETDATE(),
    IsCurrentDevice BIT DEFAULT 0,
    CONSTRAINT FK_Devices_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
);

-- Bảng 4: Token duy trì đăng nhập bảo mật (Remember-Me)
CREATE TABLE UserTokens (
    TokenId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    Series VARCHAR(100) NOT NULL UNIQUE,
    TokenHash VARCHAR(255) NOT NULL,
    ExpiresAt DATETIME2 NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Tokens_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
);

-- ============================================================================
-- PHẦN 2: GÓI THUÊ BAO, KHO PHIM, THANH TOÁN QR & VÉ RẠP 48H
-- ============================================================================

-- Bảng 5: Danh mục gói cước thuê bao định kỳ (SVOD)
CREATE TABLE SubscriptionPlans (
    PlanId INT IDENTITY(1,1) PRIMARY KEY,
    PlanCode VARCHAR(50) NOT NULL UNIQUE,
    PlanName NVARCHAR(100) NOT NULL,
    Price DECIMAL(18,2) NOT NULL CHECK (Price >= 0),
    DurationDays INT NOT NULL CHECK (DurationDays > 0),
    MaxScreens INT DEFAULT 1 CHECK (MaxScreens >= 1),
    MaxQuality VARCHAR(20) DEFAULT '1080P',
    Description NVARCHAR(500) NULL,
    IsActive BIT DEFAULT 1
);

-- Bảng 6: Bảng phim cốt lõi (Bổ sung AgeRating, LanguageBadge cho thực tế)
CREATE TABLE Movies (
    MovieId INT IDENTITY(1,1) PRIMARY KEY,
    Title NVARCHAR(255) NOT NULL,
    OriginalTitle NVARCHAR(255) NULL,
    Description NVARCHAR(MAX) NULL,
    Duration INT NOT NULL CHECK (Duration > 0),    -- Thời lượng (phút)
    ReleaseYear INT CHECK (ReleaseYear >= 1900 AND ReleaseYear <= 2030),
    Country NVARCHAR(100) NULL,
    Director NVARCHAR(150) NULL,
    Actors NVARCHAR(500) NULL,
    PosterUrl VARCHAR(500) NULL,                   -- Ảnh bìa dọc 2:3
    BackdropUrl VARCHAR(500) NULL,                 -- Ảnh nền ngang 16:9
    TrailerUrl VARCHAR(500) NULL,                  -- Link YouTube Trailer
    MovieType VARCHAR(30) DEFAULT 'SERIES',        -- 'SINGLE' (Phim lẻ), 'SERIES' (Phim bộ), 'ANIMATION_3D' (Hoạt hình)
    AgeRating VARCHAR(10) DEFAULT 'T13',           -- 'P', 'K', 'T13', 'T16', 'T18' (Giới hạn độ tuổi)
    AudioSummary NVARCHAR(100) DEFAULT N'Vietsub + Thuyết minh + Lồng tiếng',
    CurrentEpisode INT DEFAULT 1 CHECK (CurrentEpisode >= 1),
    TotalEpisodes INT DEFAULT 1 CHECK (TotalEpisodes >= 1),
    ViewCount BIGINT DEFAULT 0 CHECK (ViewCount >= 0),
    AvgRating DECIMAL(3,1) DEFAULT 9.0 CHECK (AvgRating >= 0.0 AND AvgRating <= 10.0),
    TotalReviews INT DEFAULT 0 CHECK (TotalReviews >= 0),
    IsVipOnly BIT DEFAULT 0,                       -- 1: Bắt buộc VIP
    IsRentOnly BIT DEFAULT 0,                      -- 1: Phim chiếu rạp thuê lẻ 48H (TVOD)
    RentalPrice DECIMAL(18,2) DEFAULT 0.00 CHECK (RentalPrice >= 0),
    Status VARCHAR(30) DEFAULT 'PUBLISHED',        -- 'PUBLISHED', 'TRANSCODING', 'PENDING', 'DELETED'
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE()
);

-- Bảng 7: Lịch sử giao dịch tài chính & Thông tin mã QR Ngân hàng
CREATE TABLE Transactions (
    TransactionId BIGINT IDENTITY(1,1) PRIMARY KEY,
    TransactionCode VARCHAR(50) NOT NULL UNIQUE,   -- vd: 'TX-00001'
    UserId INT NOT NULL,
    TransactionType VARCHAR(30) NOT NULL,          -- 'SUBSCRIPTION_VIP', 'RENT_MOVIE_48H'
    PlanId INT NULL,
    TargetMovieId INT NULL,
    OriginalAmount DECIMAL(18,2) DEFAULT 0.00,     -- Giá gốc trước khi giảm
    DiscountAmount DECIMAL(18,2) DEFAULT 0.00,     -- Số tiền giảm (50% cho VIP thuê phim rạp)
    Amount DECIMAL(18,2) NOT NULL CHECK (Amount >= 0), -- Số tiền thực thu
    PaymentMethod VARCHAR(50) NOT NULL,            -- 'VIETQR', 'MOMO', 'VNPAY', 'CARD'
    PaymentStatus VARCHAR(30) DEFAULT 'PENDING',   -- 'PENDING', 'SUCCESS', 'FAILED'
    BankCode VARCHAR(50) DEFAULT 'DEMO',           -- Dữ liệu thanh toán mẫu, không dùng để chuyển tiền
    BankAccountNo VARCHAR(50) DEFAULT 'DEMO-ACCOUNT',
    BankAccountName NVARCHAR(100) DEFAULT N'DEMO ONLY',
    TransferContent VARCHAR(150) NULL,             -- Nội dung CK: 'TVHAY TX00001'
    QrCodeUrl VARCHAR(1000) NULL,                  -- Link ảnh mã QR VietQR tự động
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    CompletedAt DATETIME2 NULL,
    CONSTRAINT FK_Trans_Users FOREIGN KEY (UserId) REFERENCES Users(UserId),
    CONSTRAINT FK_Trans_Plans FOREIGN KEY (PlanId) REFERENCES SubscriptionPlans(PlanId),
    CONSTRAINT FK_Trans_Movies FOREIGN KEY (TargetMovieId) REFERENCES Movies(MovieId)
);

-- Bảng 8: Quản lý thuê phim lẻ 48 giờ (TVOD 48H Pass)
CREATE TABLE MovieRentals (
    RentalId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    MovieId INT NOT NULL,
    TransactionId BIGINT NOT NULL,
    RentedAt DATETIME2 DEFAULT GETDATE(),
    ExpiresAt DATETIME2 NOT NULL,                  -- RentedAt + 48 giờ
    Status VARCHAR(20) DEFAULT 'ACTIVE',           -- 'ACTIVE', 'EXPIRED', 'REVOKED'
    CONSTRAINT CK_Rentals_Status CHECK (Status IN ('ACTIVE', 'EXPIRED', 'REVOKED')),
    CONSTRAINT FK_Rentals_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_Rentals_Movies FOREIGN KEY (MovieId) REFERENCES Movies(MovieId) ON DELETE CASCADE,
    CONSTRAINT FK_Rentals_Trans FOREIGN KEY (TransactionId) REFERENCES Transactions(TransactionId)
);

-- Bảng 9: Nhật ký kiểm toán thu hồi vé xem phim (Audit Logs cho Staff & Admin)
CREATE TABLE RentalRevokeLogs (
    LogId BIGINT IDENTITY(1,1) PRIMARY KEY,
    RentalId BIGINT NOT NULL,
    UserId INT NOT NULL,
    PerformedBy INT NOT NULL,
    RevokeReason NVARCHAR(255) NOT NULL,
    RevokedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Revoke_Rentals FOREIGN KEY (RentalId) REFERENCES MovieRentals(RentalId) ON DELETE NO ACTION,
    CONSTRAINT FK_Revoke_Customer FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE NO ACTION,
    CONSTRAINT FK_Revoke_Staff FOREIGN KEY (PerformedBy) REFERENCES Users(UserId) ON DELETE NO ACTION
);

-- ============================================================================
-- PHẦN 3: THỂ LOẠI, TẬP PHIM & MÁY CHỦ NGUỒN PHÁT (THUYẾT MINH / LỒNG TIẾNG / VIETSUB)
-- ============================================================================

-- Bảng 10: Thể loại phim
CREATE TABLE Genres (
    GenreId INT IDENTITY(1,1) PRIMARY KEY,
    GenreName NVARCHAR(100) NOT NULL,
    Slug VARCHAR(100) NOT NULL UNIQUE
);

-- Bảng 11: Liên kết Phim - Thể loại (N - N)
CREATE TABLE MovieGenres (
    MovieId INT NOT NULL,
    GenreId INT NOT NULL,
    PRIMARY KEY (MovieId, GenreId),
    CONSTRAINT FK_MG_Movies FOREIGN KEY (MovieId) REFERENCES Movies(MovieId) ON DELETE CASCADE,
    CONSTRAINT FK_MG_Genres FOREIGN KEY (GenreId) REFERENCES Genres(GenreId) ON DELETE CASCADE
);

-- Bảng 12: Danh sách tập phim
CREATE TABLE MovieEpisodes (
    EpisodeId INT IDENTITY(1,1) PRIMARY KEY,
    MovieId INT NOT NULL,
    EpisodeNumber INT NOT NULL CHECK (EpisodeNumber >= 1),
    EpisodeTitle NVARCHAR(255) NOT NULL,
    DurationSeconds INT NOT NULL DEFAULT 0 CHECK (DurationSeconds >= 0),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT UQ_Movie_EpisodeNumber UNIQUE (MovieId, EpisodeNumber),
    CONSTRAINT FK_Episodes_Movies FOREIGN KEY (MovieId) REFERENCES Movies(MovieId) ON DELETE CASCADE
);

-- Bảng 13: Cụm máy chủ phát đa nguồn (Đủ Lồng tiếng, Thuyết minh, Vietsub)
CREATE TABLE EpisodeServers (
    ServerId INT IDENTITY(1,1) PRIMARY KEY,
    EpisodeId INT NOT NULL,
    ServerName NVARCHAR(100) NOT NULL,
    ServerType VARCHAR(50) NOT NULL,               -- 'HLS_SPEED', 'HLS_BACKUP', 'IFRAME_EMBED'
    Quality VARCHAR(20) DEFAULT '1080P_60FPS',     -- '720P', '1080P_60FPS', '4K_UHD'
    VideoUrl VARCHAR(1000) NOT NULL,               -- Link .m3u8 hoặc YouTube Embed
    SubtitleUrl VARCHAR(500) NULL,
    AudioType NVARCHAR(50) DEFAULT N'Thuyết minh', -- N'Thuyết minh', N'Lồng tiếng', N'Vietsub'
    IsVipServer BIT DEFAULT 0,
    Priority INT DEFAULT 1,
    CONSTRAINT FK_Servers_Episodes FOREIGN KEY (EpisodeId) REFERENCES MovieEpisodes(EpisodeId) ON DELETE CASCADE
);

-- ============================================================================
-- PHẦN 4: TIẾN TRÌNH XEM DỞ, YÊU THÍCH, BÌNH LUẬN & BÁO LỖI
-- ============================================================================

-- Bảng 14: Lịch sử xem dở (Netflix Sync - Ping 10s AJAX Heartbeat)
CREATE TABLE WatchHistory (
    HistoryId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    EpisodeId INT NOT NULL,
    LastPosition INT NOT NULL DEFAULT 0 CHECK (LastPosition >= 0),
    DurationSeconds INT NOT NULL CHECK (DurationSeconds >= 0),
    IsCompleted BIT DEFAULT 0,
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT UQ_History_User_Ep UNIQUE (UserId, EpisodeId),
    CONSTRAINT FK_History_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_History_Episodes FOREIGN KEY (EpisodeId) REFERENCES MovieEpisodes(EpisodeId) ON DELETE CASCADE
);

-- Bảng 15: Danh sách phim yêu thích (Watchlist)
CREATE TABLE Watchlists (
    WatchlistId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    MovieId INT NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT UQ_Watchlist UNIQUE (UserId, MovieId),
    CONSTRAINT FK_Watchlists_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_Watchlists_Movies FOREIGN KEY (MovieId) REFERENCES Movies(MovieId) ON DELETE CASCADE
);

-- Bảng 16: Đánh giá & Bình luận đa cấp
CREATE TABLE Comments (
    CommentId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    MovieId INT NOT NULL,
    ParentId BIGINT NULL,
    Rating INT NULL CHECK (Rating >= 1 AND Rating <= 10),
    Content NVARCHAR(1000) NOT NULL,
    IsEdited BIT DEFAULT 0,
    Status BIT DEFAULT 1,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Comments_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_Comments_Movies FOREIGN KEY (MovieId) REFERENCES Movies(MovieId) ON DELETE CASCADE,
    CONSTRAINT FK_Comments_Parent FOREIGN KEY (ParentId) REFERENCES Comments(CommentId) ON DELETE NO ACTION
);

-- Bảng 17: Tiếp nhận báo cáo lỗi luồng phát
CREATE TABLE IssueReports (
    ReportId BIGINT IDENTITY(1,1) PRIMARY KEY,
    EpisodeId INT NOT NULL,
    ServerId INT NULL,
    UserId INT NULL,
    IssueType VARCHAR(50) NOT NULL,
    Description NVARCHAR(500) NULL,
    Status VARCHAR(30) DEFAULT 'OPEN',
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Reports_Episodes FOREIGN KEY (EpisodeId) REFERENCES MovieEpisodes(EpisodeId) ON DELETE CASCADE,
    CONSTRAINT FK_Reports_Servers FOREIGN KEY (ServerId) REFERENCES EpisodeServers(ServerId) ON DELETE NO ACTION,
    CONSTRAINT FK_Reports_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE NO ACTION
);

-- Bảng 18: Quản lý Banners Carousel quảng cáo
CREATE TABLE Banners (
    BannerId INT IDENTITY(1,1) PRIMARY KEY,
    Title NVARCHAR(200) NOT NULL,
    ImageUrl VARCHAR(500) NOT NULL,
    TargetUrl VARCHAR(500) NOT NULL,
    Position VARCHAR(50) NOT NULL,
    MovieId INT NULL,
    StartDate DATETIME2 NOT NULL,
    EndDate DATETIME2 NOT NULL,
    IsActive BIT DEFAULT 1,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Banners_Movies FOREIGN KEY (MovieId) REFERENCES Movies(MovieId) ON DELETE SET NULL,
    CONSTRAINT CK_Banner_Dates CHECK (StartDate <= EndDate)
);
GO

-- ============================================================================
-- PHẦN 5: 12 CHỈ MỤC TỐI ƯU HIỆU NĂNG TRUY VẤN (INDEXES)
-- ============================================================================
CREATE NONCLUSTERED INDEX IX_Movies_SearchFilter ON Movies (Status, MovieType, ReleaseYear) INCLUDE (Title, OriginalTitle, PosterUrl, ViewCount, AvgRating);
CREATE NONCLUSTERED INDEX IX_Movies_Title ON Movies (Title) INCLUDE (MovieId, PosterUrl, ReleaseYear, AvgRating);
CREATE NONCLUSTERED INDEX IX_Users_Login ON Users (Username) INCLUDE (PasswordHash, RoleId, IsVip, Theme, Status);
CREATE UNIQUE NONCLUSTERED INDEX IX_Users_GoogleAuth ON Users (GoogleId) WHERE GoogleId IS NOT NULL;
CREATE NONCLUSTERED INDEX IX_UserDevices_ActiveMonitor ON UserDevices (UserId, LastActive) INCLUDE (SessionToken, DeviceType);
CREATE NONCLUSTERED INDEX IX_UserTokens_Lookup ON UserTokens (Series, ExpiresAt) INCLUDE (UserId, TokenHash);
CREATE NONCLUSTERED INDEX IX_WatchHistory_ResumeList ON WatchHistory (UserId, IsCompleted, UpdatedAt DESC) INCLUDE (EpisodeId, LastPosition, DurationSeconds);
CREATE NONCLUSTERED INDEX IX_MovieRentals_CheckValid ON MovieRentals (UserId, MovieId, ExpiresAt, Status);
CREATE NONCLUSTERED INDEX IX_Comments_Hierarchy ON Comments (MovieId, ParentId, Status, CreatedAt DESC);
CREATE NONCLUSTERED INDEX IX_EpisodeServers_Episode ON EpisodeServers (EpisodeId, Priority, IsVipServer) INCLUDE (ServerType, Quality, VideoUrl, AudioType);
CREATE NONCLUSTERED INDEX IX_Transactions_User_Status ON Transactions (UserId, PaymentStatus, CreatedAt DESC) INCLUDE (Amount, TransactionCode, QrCodeUrl);
CREATE NONCLUSTERED INDEX IX_MovieEpisodes_Movie_Ep ON MovieEpisodes (MovieId, EpisodeNumber) INCLUDE (EpisodeTitle, DurationSeconds);
GO

-- ============================================================================
-- PHẦN 6: NẠP DỮ LIỆU THỰC TẾ CHO ĐỒ ÁN TỐT NGHIỆP (SEED DATA)
-- ============================================================================
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

-- 1. BẢNG 1: Roles (10 vai trò, trong đó 1=ADMIN, 2=STAFF, 3=CUSTOMER, 9=GUEST)
INSERT INTO Roles (RoleName) VALUES 
('ADMIN'),              -- RoleId = 1: Quản trị viên
('STAFF'),              -- RoleId = 2: Nhân viên vận hành / hỗ trợ
('CUSTOMER'),           -- RoleId = 3: Khách hàng thành viên
('VIP_MEMBER'),         -- RoleId = 4
('CONTENT_MODERATOR'),  -- RoleId = 5
('CONTENT_EDITOR'),     -- RoleId = 6
('FINANCE_AUDITOR'),    -- RoleId = 7
('CUSTOMER_SUPPORT'),   -- RoleId = 8
('GUEST'),              -- RoleId = 9: Khách vãng lai (Tài khoản trải nghiệm)
('SYSTEM_TESTER');      -- RoleId = 10

-- 2. BẢNG 2: Users (30 Tài khoản: 2 Admin, 4 Nhân viên, 20 Khách hàng, 4 Khách vãng lai)
-- Mật khẩu mặc định của 'admin' là '123456' -> MD5: e10adc3949ba59abbe56e057f20f883e
INSERT INTO Users (UserCode, Username, PasswordHash, FullName, Email, Phone, IsVip, VipExpireDate, RoleId, Theme, Status) VALUES
-- [2 TÀI KHOẢN ADMIN]
('#TVH-ADMIN01', 'admin', 'e10adc3949ba59abbe56e057f20f883e', N'Quản Trị Viên Demo 01', 'admin@example.com', NULL, 1, '2030-12-31', 1, 'dark', 1),
('#TVH-ADMIN02', 'superadmin', '0192023a7bbd73250516f069df18b500', N'Quản Trị Viên Demo 02', 'superadmin@example.com', NULL, 1, '2030-12-31', 1, 'dark', 1),

-- [4 TÀI KHOẢN NHÂN VIÊN (STAFF)]
('#TVH-STAFF01', 'staff_support1', 'e10adc3949ba59abbe56e057f20f883e', N'Nhân Viên Demo 01', 'staff1@example.com', NULL, 0, NULL, 2, 'dark', 1),
('#TVH-STAFF02', 'staff_support2', 'e10adc3949ba59abbe56e057f20f883e', N'Nhân Viên Demo 02', 'staff2@example.com', NULL, 0, NULL, 2, 'light', 1),
('#TVH-STAFF03', 'staff_editor', 'e10adc3949ba59abbe56e057f20f883e', N'Nhân Viên Demo 03', 'editor@example.com', NULL, 0, NULL, 2, 'dark', 1),
('#TVH-STAFF04', 'staff_mod', 'e10adc3949ba59abbe56e057f20f883e', N'Nhân Viên Demo 04', 'moderator@example.com', NULL, 0, NULL, 2, 'dark', 1),

-- [5 TÀI KHOẢN KHÁCH HÀNG DEMO]
('#TVH-88291', 'member_vip_01', 'e10adc3949ba59abbe56e057f20f883e', N'Thành Viên Demo 01 (VIP Diamond)', 'member01@example.com', NULL, 1, DATEADD(day, 180, GETDATE()), 3, 'dark', 1),
('#TVH-88292', 'member_vip_02', '25d55ad283aa400af464c76d713c07ad', N'Thành Viên Demo 02 (VIP Tháng)', 'member02@example.com', NULL, 1, DATEADD(day, 25, GETDATE()), 3, 'dark', 1),
('#TVH-88293', 'member_standard_01', '25d55ad283aa400af464c76d713c07ad', N'Thành Viên Demo 03 (Khách Thường)', 'member03@example.com', NULL, 0, NULL, 3, 'light', 1),
('#TVH-88294', 'member_vip_03', '25d55ad283aa400af464c76d713c07ad', N'Thành Viên Demo 04 (VIP Năm)', 'member04@example.com', NULL, 1, DATEADD(day, 320, GETDATE()), 3, 'dark', 1),
('#TVH-88295', 'member_standard_02', 'e10adc3949ba59abbe56e057f20f883e', N'Thành Viên Demo 05 (Khách Thường)', 'member05@example.com', NULL, 0, NULL, 3, 'dark', 1),

-- [4 TÀI KHOẢN KHÁCH VÃNG LAI (GUEST - RoleId = 9, Pass: '111111')]
('#TVH-GUEST01', 'guest_01', '96e79218965eb72c92a549dd5a330112', N'Khách Vãng Lai Demo 01', 'guest01@example.com', NULL, 0, NULL, 9, 'dark', 1),
('#TVH-GUEST02', 'guest_02', '96e79218965eb72c92a549dd5a330112', N'Khách Vãng Lai Demo 02', 'guest02@example.com', NULL, 0, NULL, 9, 'light', 1),
('#TVH-GUEST03', 'guest_03', '96e79218965eb72c92a549dd5a330112', N'Khách Vãng Lai Demo 03', 'guest03@example.com', NULL, 0, NULL, 9, 'dark', 1),
('#TVH-GUEST04', 'guest_04', '96e79218965eb72c92a549dd5a330112', N'Khách Vãng Lai Demo 04', 'guest04@example.com', NULL, 0, NULL, 9, 'dark', 1);

-- [15 TÀI KHOẢN KHÁCH HÀNG BỔ SUNG CHO ĐỦ 30 USER (Pass: '123456')]
DECLARE @u INT = 16;
WHILE @u <= 30
BEGIN
    INSERT INTO Users (UserCode, Username, PasswordHash, FullName, Email, Phone, IsVip, VipExpireDate, RoleId, Theme, Status)
    VALUES (
        '#TVH-' + CAST(10000 + @u AS VARCHAR(10)),
        'customer_' + CAST(@u AS VARCHAR(10)),
        'e10adc3949ba59abbe56e057f20f883e',
        N'Thành Viên Demo Số ' + CAST(@u AS NVARCHAR(10)),
        'customer' + CAST(@u AS VARCHAR(10)) + '@example.com',
        NULL,
        CASE WHEN @u % 2 = 0 THEN 1 ELSE 0 END,
        CASE WHEN @u % 2 = 0 THEN DATEADD(day, 30, GETDATE()) ELSE NULL END,
        3, -- CUSTOMER
        CASE WHEN @u % 3 = 0 THEN 'light' ELSE 'dark' END,
        CASE WHEN @u = 30 THEN 0 ELSE 1 END -- Tài khoản số 30 bị khóa để test Admin mở khóa
    );
    SET @u = @u + 1;
END

-- 3. BẢNG 5: SubscriptionPlans (10 gói cước SVOD)
INSERT INTO SubscriptionPlans (PlanCode, PlanName, Price, DurationDays, MaxScreens, MaxQuality, Description) VALUES
('BASIC_MONTHLY', N'Gói Cơ Bản (Standard)', 49000, 30, 1, '1080P', N'Full HD 1080p, 1 màn hình phát đồng thời'),
('VIP_MONTHLY', N'Gói VIP Cao Cấp (Ultra HD)', 89000, 30, 4, '4K_DOLBY', N'4K UHD Dolby Atmos, 4 màn hình, giảm 50% vé thuê phim rạp'),
('VIP_QUARTERLY', N'Gói VIP 3 Tháng', 249000, 90, 4, '4K_DOLBY', N'Tiết kiệm 8%, trọn vẹn 3 tháng xem phim 4K + Lồng tiếng'),
('VIP_SEMI_ANNUAL', N'Gói VIP 6 Tháng', 469000, 180, 4, '4K_DOLBY', N'Tiết kiệm 15%, ưu đãi nửa năm xem không giới hạn'),
('VIP_YEARLY', N'Gói VIP 1 Năm (Diamond)', 748000, 365, 4, '4K_DOLBY', N'Tiết kiệm 30%, đặc quyền VIP Diamond cao cấp nhất'),
('STUDENT_MONTHLY', N'Gói Sinh Viên', 29000, 30, 1, '720P', N'Gói ưu đãi học sinh sinh viên, chất lượng 720p HD'),
('FAMILY_PREMIUM', N'Gói Gia Đình Premium', 139000, 30, 6, '4K_DOLBY', N'Hỗ trợ 6 màn hình Smart TV & Mobile cho cả gia đình'),
('WEEKEND_PASS', N'Vé Cuối Tuần 3 Ngày', 19000, 3, 2, '1080P', N'Giải trí thả ga 3 ngày cuối tuần'),
('PRO_STREAMER', N'Gói Streamer 4K Ultra', 119000, 30, 4, '4K_DOLBY', N'Ưu tiên luồng CDN tốc độ cao nhất giờ cao điểm'),
('LIFETIME_FOUNDER', N'Gói Vĩnh Viễn Founder', 2990000, 3650, 4, '4K_DOLBY', N'Đặc quyền 10 năm cùng nền tảng TVHAY Cinema');

-- 4. BẢNG 10: Genres (12 Thể loại đa dạng từ Hoạt hình đến Kinh dị)
INSERT INTO Genres (GenreName, Slug) VALUES 
(N'Tiên hiệp & Cổ trang', 'tien-hiep'),     -- 1
(N'Huyền huyễn', 'huyen-huyen'),            -- 2
(N'Hoạt hình 3D & Anime', 'hoat-hinh-3d'),  -- 3
(N'Hành động & Võ thuật', 'hanh-dong'),     -- 4
(N'Kinh dị & Tâm linh', 'kinh-di'),         -- 5
(N'Hài hước', 'hai-huoc'),                  -- 6
(N'Tình cảm & Lãng mạn', 'tinh-cam'),       -- 7
(N'Khoa học viễn tưởng', 'khoa-hoc-vien-tuong'), -- 8
(N'Tâm lý xã hội', 'tam-ly-xa-hoi'),        -- 9
(N'Phiêu lưu & Sinh tồn', 'phieu-luu'),     -- 10
(N'Gia đình', 'gia-dinh'),                  -- 11
(N'Hình sự & Tội phạm', 'hinh-su-toi-pham');-- 12

-- 5. BẢNG 6: Movies (30 SIÊU PHẨM ĐA THỂ LOẠI - ĐA QUỐC GIA + 170 PHIM PHÂN TRANG = 200 PHIM)
INSERT INTO Movies (
    Title, OriginalTitle, Description, Duration, ReleaseYear, Country, Director, Actors,
    PosterUrl, BackdropUrl, TrailerUrl, MovieType, AgeRating, AudioSummary,
    CurrentEpisode, TotalEpisodes, ViewCount, AvgRating, TotalReviews, IsVipOnly, IsRentOnly, RentalPrice, Status
) VALUES
-- [NHÓM 1: HOẠT HÌNH 3D & ANIME (TRUNG - NHẬT - MỸ)]
(N'Đấu Phá Thương Khung: Niên Phiên', 'Battle Through the Heavens', N'Thiếu niên Tiêu Viêm từ phế vật vươn lên đỉnh phong Đấu Khí đại lục, thu phục Dị Hỏa và quyết chiến Hồn Điện. Có đủ bản Thuyết minh, Lồng tiếng và Vietsub.', 24, 2024, N'Trung Quốc', N'Cố Gia Bân', N'Tiêu Viêm, Huân Nhi, Mỹ Đỗ Toa, Dược Lão', 'https://picsum.photos/seed/daupha/400/600', 'https://picsum.photos/seed/dauphabg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'ANIMATION_3D', 'T13', N'Thuyết minh + Lồng tiếng + Vietsub', 24, 52, 1420000, 9.5, 3500, 0, 0, 0, 'PUBLISHED'),
(N'Thôn Phệ Tinh Không', 'Swallowed Star', N'Sau thảm họa R-Virus, La Phong khoác giáp chiến thần bước ra vũ trụ bao la bảo vệ Địa Cầu trước các chủng tộc ngoài hành tinh.', 22, 2024, N'Trung Quốc', N'Thẩm Lạc', N'La Phong, Từ Hân, Hồng, Lôi Thần', 'https://picsum.photos/seed/thonphe/400/600', 'https://picsum.photos/seed/thonphebg/1280/720', 'https://www.youtube.com/watch?v=L_LUpnjgPso', 'ANIMATION_3D', 'T13', N'Thuyết minh + Vietsub', 135, 150, 980000, 9.3, 2100, 1, 0, 0, 'PUBLISHED'),
(N'Phàm Nhân Tu Tiên', 'A Record of Mortal''s Journey to Immortality', N'Hàn Lập dùng trí tuệ và sự cẩn trọng tuyệt đối để sinh tồn trong giới tu tiên cá lớn nuốt cá bé.', 20, 2024, N'Trung Quốc', N'Vương Dụ Nhân', N'Hàn Lập, Nam Cung Uyển', 'https://picsum.photos/seed/phamnhan/400/600', 'https://picsum.photos/seed/phamnhanbg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'ANIMATION_3D', 'T13', N'Thuyết minh + Lồng tiếng', 116, 120, 842000, 9.4, 1950, 0, 0, 0, 'PUBLISHED'),
(N'Thanh Gươm Diệt Quỷ: Đại Trụ Đặc Huấn', 'Demon Slayer: Hashira Training Arc', N'Tanjiro cùng Sát Quỷ Đoàn bước vào khóa huấn luyện khắc nghiệt nhất của các Trụ Cột trước trận quyết chiến tại Vô Hạn Thành với Muzan.', 24, 2024, N'Nhật Bản', N'Haruo Sotozaki', N'Kamado Tanjiro, Nezuko, Zenitsu, Giyu', 'https://picsum.photos/seed/demonslayer/400/600', 'https://picsum.photos/seed/demonslayerbg/1280/720', 'https://www.youtube.com/watch?v=wyiZWYMilgk', 'ANIMATION_3D', 'T16', N'Lồng tiếng + Vietsub', 8, 8, 1650000, 9.6, 5200, 1, 0, 0, 'PUBLISHED'),
(N'Những Mảnh Ghép Cảm Xúc 2 (Inside Out 2)', 'Inside Out 2', N'Bom tấn hoạt hình Pixar khi cô bé Riley bước vào tuổi dậy thì với sự xuất hiện của Cảm xúc Lo Âu (Anxiety) đầy hài hước và sâu sắc.', 96, 2024, N'Âu Mỹ', N'Kelsey Mann', N'Amy Poehler, Maya Hawke, Kensington Tallman', 'https://picsum.photos/seed/insideout2/400/600', 'https://picsum.photos/seed/insideout2bg/1280/720', 'https://www.youtube.com/watch?v=LEjhY15eCx0', 'ANIMATION_3D', 'P', N'Lồng tiếng Việt + Vietsub', 1, 1, 1120000, 9.2, 4100, 0, 1, 19000, 'PUBLISHED'),
(N'Thám Tử Lừng Danh Conan: Ngôi Sao 5 Cánh 1 Triệu Đô', 'Detective Conan: The Million-dollar Pentagram', N'Cuộc đối đầu nghẹt thở giữa siêu trộm Kid, Heiji và Conan xoay quanh thanh kiếm cổ chứa bí mật gia tộc tại Hakodate.', 110, 2024, N'Nhật Bản', N'Chika Nagaoka', N'Edogawa Conan, Kaito Kid, Hattori Heiji', 'https://picsum.photos/seed/conan27/400/600', 'https://picsum.photos/seed/conan27bg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'ANIMATION_3D', 'K', N'Lồng tiếng + Thuyết minh', 1, 1, 930000, 9.1, 3100, 0, 1, 19000, 'PUBLISHED'),

-- [NHÓM 2: PHIM KINH DỊ & TÂM LINH ĐA QUỐC GIA (HÀN - THÁI - VIỆT - MỸ)]
(N'Quật Mộ Trùng Ma (Exhuma)', 'Exhuma', N'Siêu phẩm kinh dị tâm linh Hàn Quốc về hai pháp sư trẻ và thầy phong thủy khai quật ngôi mộ cổ kỳ bí trên đỉnh núi, vô tình giải phóng ác linh trăm năm.', 134, 2024, N'Hàn Quốc', N'Jang Jae-hyun', N'Choi Min-sik, Kim Go-eun, Lee Do-hyun, Yoo Hai-jin', 'https://picsum.photos/seed/exhuma/400/600', 'https://picsum.photos/seed/exhumabg/1280/720', 'https://www.youtube.com/watch?v=7LH-TIcPqks', 'SINGLE', 'T16', N'Lồng tiếng + Thuyết minh + Vietsub', 1, 1, 1890000, 9.4, 6400, 0, 1, 19000, 'PUBLISHED'),
(N'Quỷ Ăn Tạng 2 (Tee Yod 2)', 'Death Whisperer 2', N'Yak tiếp tục hành trình truy lùng tà linh áo đen trong rừng sâu để trả thù cho em gái với những pha trừ tà nghẹt thở.', 111, 2024, N'Thái Lan', N'Taweewat Wantha', N'Nadech Kugimiya, Denise Jelilcha Kapaun', 'https://picsum.photos/seed/teeyod2/400/600', 'https://picsum.photos/seed/teeyod2bg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'SINGLE', 'T18', N'Lồng tiếng + Vietsub', 1, 1, 1340000, 9.0, 2900, 0, 1, 19000, 'PUBLISHED'),
(N'Ma Da', 'The Drowning Spirit', N'Phim kinh dị Việt Nam lấy cảm hứng từ truyền thuyết dân gian miền Tây sông nước về oan hồn kéo chân dưới đáy sông.', 95, 2024, N'Việt Nam', N'Nguyễn Hữu Hoàng', N'Việt Hương, Trung Dân, Cẩm Ly, Dạ Chúc', 'https://picsum.photos/seed/mada/400/600', 'https://picsum.photos/seed/madabg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'SINGLE', 'T16', N'Bản Gốc Tiếng Việt + Phụ đề', 1, 1, 780000, 8.5, 1500, 1, 0, 0, 'PUBLISHED'),
(N'Ám Ảnh Kinh Hoàng: Nghi Lễ Cuối Cùng', 'The Conjuring: Last Rites', N'Vụ án tâm linh rùng rợn nhất trong hồ sơ của vợ chồng nhà ngoại cảm Ed và Lorraine Warren.', 118, 2024, N'Âu Mỹ', N'Michael Chaves', N'Patrick Wilson, Vera Farmiga', 'https://picsum.photos/seed/conjuring/400/600', 'https://picsum.photos/seed/conjuringbg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'SINGLE', 'T18', N'Thuyết minh + Vietsub', 1, 1, 910000, 8.9, 2200, 1, 0, 0, 'PUBLISHED'),

-- [NHÓM 3: PHIM CHIẾU RẠP VIỆT NAM & BOM TẤN HOLLYWOOD (TVOD 48H)]
(N'Mai (Trấn Thành)', 'Mai Movie', N'Tác phẩm điện ảnh tâm lý tình cảm đạt doanh thu kỷ lục phòng vé Việt Nam xoay quanh số phận của người phụ nữ tên Mai.', 131, 2024, N'Việt Nam', N'Trấn Thành', N'Phương Anh Đào, Tuấn Trần, Trấn Thành, Hồng Đào', 'https://picsum.photos/seed/maimovie/400/600', 'https://picsum.photos/seed/maibg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'SINGLE', 'T18', N'Bản Gốc Âm Thanh Rạp Dolby', 1, 1, 2150000, 9.1, 7300, 0, 1, 19000, 'PUBLISHED'),
(N'Lật Mặt 7: Một Điều Ước', 'Face Off 7: One Wish', N'Câu chuyện cảm động lấy nước mắt hàng triệu khán giả về bà Hai 73 tuổi và hành trình thăm 5 người con ở khắp mọi miền đất nước.', 138, 2024, N'Việt Nam', N'Lý Hải', N'Thanh Hiền, Trương Minh Cường, Đinh Y Nhung, Quách Ngọc Tuyên', 'https://picsum.photos/seed/latmat7/400/600', 'https://picsum.photos/seed/latmat7bg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'SINGLE', 'K', N'Bản Gốc Tiếng Việt 4K', 1, 1, 1950000, 9.3, 6100, 0, 1, 19000, 'PUBLISHED'),
(N'Hành Tinh Cát: Phần Hai (Dune 2)', 'Dune: Part Two', N'Hành trình báo thù vĩ đại của Paul Atreides cùng tộc người Fremen chống lại Hoàng đế và gia tộc Harkonnen trên hành tinh Arrakis.', 166, 2024, N'Âu Mỹ', N'Denis Villeneuve', N'Timothée Chalamet, Zendaya, Rebecca Ferguson', 'https://picsum.photos/seed/dune2/400/600', 'https://picsum.photos/seed/dune2bg/1280/720', 'https://www.youtube.com/watch?v=Way9Dexny3w', 'SINGLE', 'T13', N'Lồng tiếng + Thuyết minh + Vietsub', 1, 1, 2480000, 9.6, 8900, 0, 1, 19000, 'PUBLISHED'),
(N'Deadpool & Wolverine', 'Deadpool & Wolverine', N'Sự kết hợp bùng nổ và lầy lội nhất vũ trụ điện ảnh Marvel khi Deadpool kéo Wolverine vào cuộc chiến giải cứu đa vũ trụ TVA.', 128, 2024, N'Âu Mỹ', N'Shawn Levy', N'Ryan Reynolds, Hugh Jackman, Emma Corrin', 'https://picsum.photos/seed/deadpool3/400/600', 'https://picsum.photos/seed/deadpool3bg/1280/720', 'https://www.youtube.com/watch?v=73_1biulkYk', 'SINGLE', 'T18', N'Lồng tiếng + Thuyết minh + Vietsub', 1, 1, 2310000, 9.4, 7800, 0, 1, 19000, 'PUBLISHED'),
(N'Godzilla x Kong: Đế Chế Mới', 'Godzilla x Kong: The New Empire', N'Hai siêu quái thú Godzilla và Kong buộc phải liên minh để chống lại mối đe dọa khổng lồ ẩn sâu trong Trái Đất Rỗng.', 115, 2024, N'Âu Mỹ', N'Adam Wingard', N'Rebecca Hall, Brian Tyree Henry, Dan Stevens', 'https://picsum.photos/seed/godzillakong/400/600', 'https://picsum.photos/seed/godzillakongbg/1280/720', 'https://www.youtube.com/watch?v=lV1OOlGwExM', 'SINGLE', 'T13', N'Thuyết minh + Vietsub', 1, 1, 1540000, 8.9, 3400, 1, 0, 0, 'PUBLISHED'),

-- [NHÓM 4: PHIM BỘ HÀN QUỐC, TRUNG QUỐC & ÂU MỸ ĐÌNH ĐÁM (SERIES)]
(N'Trò Chơi Con Mực (Phần 2)', 'Squid Game Season 2', N'Seong Gi-hun mang số hiệu 456 quay trở lại đấu trường sinh tử với quyết tâm lật đổ tổ chức đứng sau trò chơi tàn khốc.', 60, 2024, N'Hàn Quốc', N'Hwang Dong-hyuk', N'Lee Jung-jae, Lee Byung-hun, Wi Ha-joon, Gong Yoo', 'https://picsum.photos/seed/squidgame2/400/600', 'https://picsum.photos/seed/squidgame2bg/1280/720', 'https://www.youtube.com/watch?v=Ed1sGgHUo88', 'SERIES', 'T18', N'Lồng tiếng + Thuyết minh + Vietsub', 7, 7, 3120000, 9.5, 9500, 1, 0, 0, 'PUBLISHED'),
(N'Nữ Hoàng Nước Mắt (Queen of Tears)', 'Queen of Tears', N'Chuyện tình vượt qua biến cố đầy cảm xúc và hài hước giữa nữ thừa kế tập đoàn Queens Hong Hae-in và chàng rể Baek Hyun-woo.', 70, 2024, N'Hàn Quốc', N'Jang Young-woo', N'Kim Soo-hyun, Kim Ji-won, Park Sung-hoon', 'https://picsum.photos/seed/queenoftears/400/600', 'https://picsum.photos/seed/queenoftearsbg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'SERIES', 'T13', N'Lồng tiếng + Thuyết minh + Vietsub', 16, 16, 2050000, 9.4, 6800, 0, 0, 0, 'PUBLISHED'),
(N'Khánh Dư Niên (Phần 2)', 'Joy of Life Season 2', N'Phạm Nhàn trở về kinh thành đối mặt với những cuộc đấu trí quyền mưu kịch tính nhất nơi triều đường Khánh Quốc.', 45, 2024, N'Trung Quốc', N'Tôn Hạo', N'Trương Nhược Quân, Lý Thấm, Trần Đạo Minh', 'https://picsum.photos/seed/joyoflife2/400/600', 'https://picsum.photos/seed/joyoflife2bg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'SERIES', 'T13', N'Thuyết minh + Lồng tiếng', 36, 36, 1760000, 9.3, 4500, 1, 0, 0, 'PUBLISHED'),
(N'Tiên Nghịch (Renegade Immortal)', 'Xian Ni', N'Vương Lâm từ một thiếu niên tư chất bình thường bước lên con đường nghịch thiên tu hành đầy máu và nước mắt.', 22, 2024, N'Trung Quốc', N'Thạch Đầu Hùng', N'Vương Lâm, Lý Mộ Uyển', 'https://picsum.photos/seed/tiennghich/400/600', 'https://picsum.photos/seed/tiennghichbg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'ANIMATION_3D', 'T13', N'Thuyết minh + Vietsub', 65, 76, 1290000, 9.5, 4200, 0, 0, 0, 'PUBLISHED'),
(N'Già Thiên (Shrouding the Heavens)', 'Shrouding the Heavens', N'Cửu Long kéo quan tài bằng đồng vượt tinh không đưa Diệp Phàm vào thế giới tu tiên hùng vĩ.', 22, 2024, N'Trung Quốc', N'Triệu Dũng', N'Diệp Phàm, Bàng Bác, Cơ Tử Nguyệt', 'https://picsum.photos/seed/giathien/400/600', 'https://picsum.photos/seed/giathienbg/1280/720', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'ANIMATION_3D', 'T13', N'Thuyết minh + Lồng tiếng', 82, 104, 940000, 9.1, 2400, 0, 0, 0, 'PUBLISHED');

-- Sinh tiếp từ Phim 21 đến Phim 200 để test Phân trang 20 phim/trang (10 trang tròn)
DECLARE @m INT = 21;
WHILE @m <= 200
BEGIN
    INSERT INTO Movies (
        Title, OriginalTitle, Description, Duration, ReleaseYear, Country, Director, Actors,
        PosterUrl, BackdropUrl, TrailerUrl, MovieType, AgeRating, AudioSummary,
        CurrentEpisode, TotalEpisodes, ViewCount, AvgRating, TotalReviews, IsVipOnly, IsRentOnly, RentalPrice, Status, CreatedAt
    )
    VALUES (
        CASE ((@m - 1) % 12) + 1
            WHEN 1 THEN N'Nghịch Thiên Tu Luyện - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 2 THEN N'Bí Ẩn Huyền Giới - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 3 THEN N'Hành Trình Vạn Giới - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 4 THEN N'Mật Lệnh Hành Động - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 5 THEN N'Bóng Đêm Gọi Tên - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 6 THEN N'Chuyện Cười Bất Tận - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 7 THEN N'Hẹn Ước Mùa Hạ - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 8 THEN N'Du Hành Liên Sao - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 9 THEN N'Góc Khuất Cuộc Đời - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 10 THEN N'Ranh Giới Sinh Tồn - Phần ' + CAST(@m AS NVARCHAR(10))
            WHEN 11 THEN N'Gia Đình Là Nhà - Phần ' + CAST(@m AS NVARCHAR(10))
            ELSE N'Hồ Sơ Tội Phạm - Phần ' + CAST(@m AS NVARCHAR(10))
        END,
        'Cinema Noir Collection #' + CAST(@m AS VARCHAR(10)),
        CASE ((@m - 1) % 12) + 1
            WHEN 1 THEN N'Một người trẻ bước vào con đường tu luyện, đối mặt thử thách và khám phá bí mật về thân thế.'
            WHEN 2 THEN N'Những thế lực đối đầu trong một thế giới huyền bí, nơi mỗi lựa chọn đều làm thay đổi vận mệnh.'
            WHEN 3 THEN N'Cuộc phiêu lưu hoạt hình qua nhiều vùng đất kỳ ảo với những người bạn đồng hành đặc biệt.'
            WHEN 4 THEN N'Đội đặc nhiệm chạy đua với thời gian để ngăn chặn một âm mưu nguy hiểm.'
            WHEN 5 THEN N'Những dấu hiệu kỳ lạ dẫn một nhóm bạn đến bí mật bị chôn giấu trong bóng tối.'
            WHEN 6 THEN N'Những tình huống dở khóc dở cười nối tiếp nhau trong một khu phố náo nhiệt.'
            WHEN 7 THEN N'Một cuộc gặp gỡ bất ngờ mở ra chuyện tình giữa hai người với những ước mơ khác biệt.'
            WHEN 8 THEN N'Phi hành đoàn khám phá hành tinh xa lạ và tìm cách đưa mọi người trở về an toàn.'
            WHEN 9 THEN N'Một người phải đối mặt với biến cố, lựa chọn khó khăn và những mối quan hệ nhiều chiều.'
            WHEN 10 THEN N'Nhóm người lạ phải hợp sức để sinh tồn trước thiên nhiên khắc nghiệt.'
            WHEN 11 THEN N'Câu chuyện ấm áp về tình thân, sự sẻ chia và những điều nhỏ bé trong cuộc sống.'
            ELSE N'Một điều tra viên lần theo manh mối để làm sáng tỏ vụ án nhiều uẩn khúc.'
        END,
        CASE WHEN @m % 3 = 0 THEN 45 ELSE 120 END,
        2020 + (@m % 6),
        CASE WHEN @m % 4 = 0 THEN N'Việt Nam' WHEN @m % 4 = 1 THEN N'Hàn Quốc' WHEN @m % 4 = 2 THEN N'Trung Quốc' ELSE N'Âu Mỹ' END,
        N'Đạo Diễn Danh Tiếng ' + CAST((@m % 10) + 1 AS NVARCHAR(5)),
        N'Dàn Diễn Viên Ngôi Sao Hạng A',
        'https://picsum.photos/seed/movie' + CAST(@m AS VARCHAR(10)) + '/400/600',
        'https://picsum.photos/seed/bg' + CAST(@m AS VARCHAR(10)) + '/1280/720',
        'https://www.youtube.com/watch?v=aqz-KE-bpKQ',
        CASE WHEN @m % 3 = 0 THEN 'SERIES' WHEN @m % 3 = 1 THEN 'ANIMATION_3D' ELSE 'SINGLE' END,
        CASE WHEN @m % 5 = 0 THEN 'T18' WHEN @m % 5 = 1 THEN 'P' ELSE 'T13' END,
        N'Thuyết minh + Lồng tiếng + Vietsub',
        CASE WHEN @m % 3 = 2 THEN 1 ELSE 12 END,
        CASE WHEN @m % 3 = 2 THEN 1 ELSE 24 END,
        @m * 12500,
        8.0 + ((@m % 19) / 10.0),
        @m * 15,
        CASE WHEN @m % 4 = 0 THEN 1 ELSE 0 END,
        CASE WHEN @m % 9 = 0 THEN 1 ELSE 0 END,
        CASE WHEN @m % 9 = 0 THEN 19000.00 ELSE 0.00 END,
        'PUBLISHED',
        DATEADD(minute, -@m * 30, GETDATE())
    );
    SET @m = @m + 1;
END

-- 6. BẢNG 11: MovieGenres (Liên kết chuẩn thể loại cho 200 phim)
INSERT INTO MovieGenres (MovieId, GenreId) VALUES
(1, 1), (1, 2), (1, 3),   -- Đấu Phá: Tiên hiệp, Huyền huyễn, Hoạt hình 3D
(2, 3), (2, 8),           -- Thôn Phệ: Hoạt hình 3D, Khoa học viễn tưởng
(3, 1), (3, 3),           -- Phàm Nhân: Tiên hiệp, Hoạt hình 3D
(4, 3), (4, 4),           -- Thanh Gươm Diệt Quỷ: Anime, Hành động
(5, 3), (5, 6), (5, 11),  -- Inside Out 2: Hoạt hình, Hài, Gia đình
(6, 3), (6, 12),          -- Conan 27: Hoạt hình, Hình sự
(7, 5), (7, 10),          -- Quật Mộ Trùng Ma: Kinh dị, Phiêu lưu
(8, 5), (8, 4),           -- Quỷ Ăn Tạng 2: Kinh dị, Hành động
(9, 5), (9, 9),           -- Ma Da: Kinh dị, Tâm lý
(10, 5),                  -- The Conjuring: Kinh dị
(11, 7), (11, 9),         -- Mai: Tình cảm, Tâm lý xã hội
(12, 9), (12, 11),        -- Lật Mặt 7: Tâm lý, Gia đình
(13, 4), (13, 8), (13, 10),-- Dune 2: Hành động, Viễn tưởng, Phiêu lưu
(14, 4), (14, 6), (14, 8),-- Deadpool & Wolverine: Hành động, Hài, Viễn tưởng
(15, 4), (15, 8),         -- Godzilla x Kong
(16, 4), (16, 9), (16, 12),-- Squid Game 2
(17, 6), (17, 7), (17, 9),-- Nữ Hoàng Nước Mắt
(18, 1), (18, 4),         -- Khánh Dư Niên 2
(19, 1), (19, 3),         -- Tiên Nghịch
(20, 1), (20, 2), (20, 3);-- Già Thiên

DECLARE @mg INT = 21;
DECLARE @sampleGenreId INT;
DECLARE @secondaryGenreId INT;
WHILE @mg <= 200
BEGIN
    SET @sampleGenreId = ((@mg - 1) % 12) + 1;
    SET @secondaryGenreId = CASE @sampleGenreId
        WHEN 1 THEN 2
        WHEN 2 THEN 3
        WHEN 3 THEN 11
        WHEN 4 THEN 10
        WHEN 5 THEN 12
        WHEN 6 THEN 11
        WHEN 7 THEN 9
        WHEN 8 THEN 10
        WHEN 9 THEN 7
        WHEN 10 THEN 4
        WHEN 11 THEN 6
        ELSE 4
    END;
    INSERT INTO MovieGenres (MovieId, GenreId)
    VALUES (@mg, @sampleGenreId),
           (@mg, @secondaryGenreId);
    SET @mg = @mg + 1;
END

-- 7. BẢNG 12 & 13: MovieEpisodes & EpisodeServers (ĐẢM BẢO MỌI PHIM ĐỀU CÓ TẬP 1 + 3 BẢN LỒNG TIẾNG, THUYẾT MINH, VIETSUB)
DECLARE @movId INT = 1;
DECLARE @newEpId INT;

WHILE @movId <= 200
BEGIN
    -- Tạo Tập 1 (hoặc Bản Full cho phim lẻ) cho tất cả 200 phim
    INSERT INTO MovieEpisodes (MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds)
    VALUES (
        @movId, 
        1, 
        CASE WHEN @movId IN (5,6,7,8,9,10,11,12,13,14,15) THEN N'Bản Điện Ảnh Full HD / 4K' ELSE N'Tập 01 - Khởi Đầu Huyền Thoại' END, 
        1458
    );
    SET @newEpId = SCOPE_IDENTITY();

    -- Mỗi Tập 1 đều có đủ 3 Server: Thuyết minh (HLS), Lồng tiếng (HLS), Vietsub (YouTube Embed)
    INSERT INTO EpisodeServers (EpisodeId, ServerName, ServerType, Quality, VideoUrl, SubtitleUrl, AudioType, IsVipServer, Priority) VALUES
    (@newEpId, N'Server VIP #1 (Bản Thuyết Minh 4K)', 'HLS_SPEED', '4K_UHD', 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8', '/subtitles/vi_sub_sample.vtt', N'Thuyết minh', 1, 1),
    (@newEpId, N'Server #2 (Bản Lồng Tiếng Việt)', 'HLS_BACKUP', '1080P_60FPS', 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8', NULL, N'Lồng tiếng', 0, 2),
    (@newEpId, N'Server Dự Phòng #3 (Bản Vietsub Embed)', 'IFRAME_EMBED', '1080P_60FPS', 'https://www.youtube.com/embed/aqz-KE-bpKQ', '/subtitles/vi_sub_sample.vtt', N'Vietsub', 0, 3);

    -- Nếu là các phim bộ nổi bật (MovieId 1, 2, 3, 4, 16, 17, 18, 19, 20), tạo thêm Tập 2 và Tập 3 liền mạch
    IF @movId IN (1, 2, 3, 4, 16, 17, 18, 19, 20)
    BEGIN
        INSERT INTO MovieEpisodes (MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds) VALUES (@movId, 2, N'Tập 02 - Bùng Nổ Giao Tranh', 1420);
        SET @newEpId = SCOPE_IDENTITY();
        INSERT INTO EpisodeServers (EpisodeId, ServerName, ServerType, Quality, VideoUrl, SubtitleUrl, AudioType, IsVipServer, Priority) VALUES
        (@newEpId, N'Server VIP #1 (Bản Thuyết Minh 4K)', 'HLS_SPEED', '4K_UHD', 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8', NULL, N'Thuyết minh', 1, 1),
        (@newEpId, N'Server #2 (Bản Lồng Tiếng Việt)', 'IFRAME_EMBED', '1080P_60FPS', 'https://www.youtube.com/embed/L_LUpnjgPso', NULL, N'Lồng tiếng', 0, 2);

        INSERT INTO MovieEpisodes (MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds) VALUES (@movId, 3, N'Tập 03 - Đỉnh Phong Quyết Đấu', 1480);
        SET @newEpId = SCOPE_IDENTITY();
        INSERT INTO EpisodeServers (EpisodeId, ServerName, ServerType, Quality, VideoUrl, SubtitleUrl, AudioType, IsVipServer, Priority) VALUES
        (@newEpId, N'Server VIP #1 (Bản Thuyết Minh 4K)', 'HLS_SPEED', '4K_UHD', 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8', NULL, N'Thuyết minh', 1, 1),
        (@newEpId, N'Server #2 (Bản Vietsub)', 'IFRAME_EMBED', '1080P_60FPS', 'https://www.youtube.com/embed/aqz-KE-bpKQ', NULL, N'Vietsub', 0, 2);
    END

    SET @movId = @movId + 1;
END

-- Bổ sung tất cả các tập còn thiếu theo TotalEpisodes, tránh phim bộ chỉ có tập 1-3.
DECLARE @maxEpisodeNumber INT = (
    SELECT ISNULL(MAX(TotalEpisodes), 1)
    FROM Movies
    WHERE MovieType IN ('SERIES', 'ANIMATION_3D')
);

;WITH EpisodeNumbers AS (
    SELECT TOP (@maxEpisodeNumber)
        ROW_NUMBER() OVER (ORDER BY object_id) AS EpisodeNumber
    FROM sys.all_objects
)
INSERT INTO MovieEpisodes (MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds)
SELECT
    m.MovieId,
    n.EpisodeNumber,
    N'Tập ' + RIGHT(N'000' + CONVERT(NVARCHAR(10), n.EpisodeNumber), 3)
        + N' - Hành trình tiếp nối',
    CASE WHEN m.Duration > 0 THEN m.Duration * 60 ELSE 1458 END
FROM Movies m
CROSS JOIN EpisodeNumbers n
WHERE m.Status = 'PUBLISHED'
  AND m.MovieType IN ('SERIES', 'ANIMATION_3D')
  AND n.EpisodeNumber <= m.TotalEpisodes
  AND NOT EXISTS (
      SELECT 1
      FROM MovieEpisodes e
      WHERE e.MovieId = m.MovieId
        AND e.EpisodeNumber = n.EpisodeNumber
  );

-- 8. BẢNG 7: Transactions (50 Giao dịch tích hợp sẵn Link QR Ngân hàng VietQR tự động)
DECLARE @ts INT = 1;
DECLARE @txCode VARCHAR(50);
DECLARE @amt DECIMAL(18,2);
DECLARE @origAmt DECIMAL(18,2);
DECLARE @discAmt DECIMAL(18,2);

WHILE @ts <= 50
BEGIN
    SET @txCode = 'TX-' + RIGHT('00000' + CAST(@ts AS VARCHAR(10)), 5);
    -- Nếu chẵn là mua gói VIP 89.000đ, nếu lẻ là thuê phim rạp 48H (có áp dụng giảm 50% còn 9.500đ cho khách VIP)
    IF @ts % 2 = 0
    BEGIN
        SET @origAmt = 89000.00; SET @discAmt = 0.00; SET @amt = 89000.00;
    END
    ELSE
    BEGIN
        SET @origAmt = 19000.00; 
        SET @discAmt = CASE WHEN @ts % 3 = 0 THEN 9500.00 ELSE 0.00 END;
        SET @amt = @origAmt - @discAmt;
    END

    INSERT INTO Transactions (
        TransactionCode, UserId, TransactionType, PlanId, TargetMovieId,
        OriginalAmount, DiscountAmount, Amount, PaymentMethod, PaymentStatus,
        BankCode, BankAccountNo, BankAccountName, TransferContent, QrCodeUrl, CompletedAt
    )
    VALUES (
        @txCode,
        (@ts % 25) + 1,
        CASE WHEN @ts % 2 = 0 THEN 'SUBSCRIPTION_VIP' ELSE 'RENT_MOVIE_48H' END,
        CASE WHEN @ts % 2 = 0 THEN 2 ELSE NULL END,
        CASE WHEN @ts % 2 <> 0 THEN ((@ts % 9) + 7) ELSE NULL END, -- Phim chiếu rạp từ ID 7 đến 15
        @origAmt,
        @discAmt,
        @amt,
        CASE WHEN @ts % 3 = 0 THEN 'VIETQR' WHEN @ts % 3 = 1 THEN 'MOMO' ELSE 'VNPAY' END,
        CASE WHEN @ts <= 45 THEN 'SUCCESS' ELSE 'PENDING' END, -- 5 đơn cuối để PENDING cho nhóm test bấm nút thanh toán Demo
        'DEMO',
        'DEMO-ACCOUNT',
        N'DEMO ONLY',
        'TVHAY ' + @txCode,
        'https://img.vietqr.io/image/DEMO-DEMO-ACCOUNT-compact2.png?amount=' + CAST(CAST(@amt AS INT) AS VARCHAR(20)) + '&addInfo=TVHAY%20' + @txCode + '&accountName=DEMO%20ONLY',
        CASE WHEN @ts <= 45 THEN GETDATE() ELSE NULL END
    );
    SET @ts = @ts + 1;
END

-- 9. BẢNG 8 & 9: MovieRentals (20 vé thuê 48H) & RentalRevokeLogs (10 nhật ký thu hồi vé của Staff)
DECLARE @mr INT = 1;
WHILE @mr <= 20
BEGIN
    INSERT INTO MovieRentals (UserId, MovieId, TransactionId, RentedAt, ExpiresAt, Status)
    VALUES (
        (@mr % 15) + 7,      -- Gán cho các tài khoản khách hàng chính (tuananh, loc_member...)
        (@mr % 9) + 7,       -- Các bộ phim rạp: Quật Mộ Trùng Ma, Mai, Lật Mặt 7, Dune 2...
        @mr,
        DATEADD(hour, -@mr, GETDATE()),
        DATEADD(hour, 48 - @mr, GETDATE()),
        CASE WHEN @mr <= 10 THEN 'REVOKED' ELSE 'ACTIVE' END
    );
    SET @mr = @mr + 1;
END

DECLARE @rvk INT = 1;
WHILE @rvk <= 10
BEGIN
    INSERT INTO RentalRevokeLogs (RentalId, UserId, PerformedBy, RevokeReason, RevokedAt)
    VALUES (
        @rvk,
        (@rvk % 15) + 7,
        3, -- Nhân viên CSKH Minh Thông (UserId = 3) thực hiện thu hồi
        N'Biên bản thu hồi vé #' + CAST(@rvk AS NVARCHAR(5)) + N': Khách báo mua nhầm phim và yêu cầu hoàn tiền qua cổng VietQR.',
        GETDATE()
    );
    SET @rvk = @rvk + 1;
END

-- 10. CÁC BẢNG CÒN LẠI (UserTokens, UserDevices, WatchHistory, Watchlists, Comments, IssueReports, Banners)
DECLARE @tk INT = 1;
WHILE @tk <= 20
BEGIN
    INSERT INTO UserTokens (UserId, Series, TokenHash, ExpiresAt)
    VALUES (@tk, 'series_key_' + CAST(@tk AS VARCHAR(10)), 'token_md5_' + CAST(@tk AS VARCHAR(10)), DATEADD(day, 30, GETDATE()));
    SET @tk = @tk + 1;
END

DECLARE @ud INT = 1;
WHILE @ud <= 60
BEGIN
    INSERT INTO UserDevices (UserId, DeviceName, DeviceType, IpAddress, Location, SessionToken, IsCurrentDevice)
    VALUES (
        (@ud % 25) + 1,
        CASE WHEN @ud % 3 = 0 THEN N'Samsung 85" Smart TV 4K' WHEN @ud % 3 = 1 THEN N'MacBook Pro M3 Max' ELSE N'iPhone 15 Pro Max' END,
        CASE WHEN @ud % 3 = 0 THEN 'SMART_TV' WHEN @ud % 3 = 1 THEN 'DESKTOP' ELSE 'MOBILE' END,
        '192.168.1.' + CAST((@ud % 200) + 10 AS VARCHAR(5)),
        N'TP. Hồ Chí Minh, VN',
        'sess_tok_' + CAST(@ud AS VARCHAR(20)),
        CASE WHEN @ud <= 25 THEN 1 ELSE 0 END
    );
    SET @ud = @ud + 1;
END

DECLARE @wh INT = 1;
WHILE @wh <= 40
BEGIN
    INSERT INTO WatchHistory (UserId, EpisodeId, LastPosition, DurationSeconds, IsCompleted, UpdatedAt)
    VALUES (
        (@wh % 15) + 7,
        @wh,
        (@wh * 30) % 1200 + 120,
        1458,
        0,
        GETDATE()
    );
    SET @wh = @wh + 1;
END

DECLARE @wl INT = 1;
WHILE @wl <= 30
BEGIN
    INSERT INTO Watchlists (UserId, MovieId) VALUES ((@wl % 15) + 7, @wl);
    SET @wl = @wl + 1;
END

DECLARE @cm INT = 1;
WHILE @cm <= 30
BEGIN
    INSERT INTO Comments (UserId, MovieId, ParentId, Rating, Content, IsEdited, Status)
    VALUES (
        (@cm % 15) + 7,
        (@cm % 20) + 1,
        NULL,
        9 + (@cm % 2),
        N'Phim có cả bản Lồng tiếng lẫn Thuyết minh nghe rất êm, chất lượng 4K mượt không bị giật lag!',
        0,
        1
    );
    SET @cm = @cm + 1;
END

DECLARE @ir INT = 1;
WHILE @ir <= 10
BEGIN
    INSERT INTO IssueReports (EpisodeId, ServerId, UserId, IssueType, Description, Status)
    VALUES (@ir, @ir, (@ir % 15) + 7, 'AUDIO_DESYNC', N'Nhờ Admin kiểm tra lại phụ đề Vietsub ở phút thứ 12.', 'OPEN');
    SET @ir = @ir + 1;
END

INSERT INTO Banners (Title, ImageUrl, TargetUrl, Position, MovieId, StartDate, EndDate, IsActive) VALUES
(N'Đấu Phá Thương Khung: Đại Chiến Hồn Điện (4K Thuyết Minh)', 'https://picsum.photos/seed/dauphabg/1280/720', '/movie?id=1', 'HOME_HERO', 1, '2025-01-01', '2027-12-31', 1),
(N'Quật Mộ Trùng Ma (Exhuma) - Siêu Phẩm Kinh Dị Hàn Quốc', 'https://picsum.photos/seed/exhumabg/1280/720', '/movie?id=7', 'HOME_HERO', 7, '2025-01-01', '2027-12-31', 1),
(N'Hành Tinh Cát: Phần 2 (Dune 2) - Vé Thuê Rạp 48H Chỉ 19K', 'https://picsum.photos/seed/dune2bg/1280/720', '/movie?id=13', 'HOME_HERO', 13, '2025-01-01', '2027-12-31', 1),
(N'Trò Chơi Con Mực Phần 2 (Squid Game 2) - Độc Quyền Lồng Tiếng', 'https://picsum.photos/seed/squidgame2bg/1280/720', '/movie?id=16', 'HOME_HERO', 16, '2025-01-01', '2027-12-31', 1),
(N'Thanh Gươm Diệt Quỷ: Đại Trụ Đặc Huấn (Anime 4K)', 'https://picsum.photos/seed/demonslayerbg/1280/720', '/movie?id=4', 'HOME_HERO', 4, '2025-01-01', '2027-12-31', 1),
(N'Mai (Trấn Thành) - Phim Điện Ảnh Việt Ăn Khách Nhất', 'https://picsum.photos/seed/maibg/1280/720', '/movie?id=11', 'HOME_HERO', 11, '2025-01-01', '2027-12-31', 1),
(N'Lật Mặt 7: Một Điều Ước - Phim Gia Đình Cảm Động', 'https://picsum.photos/seed/latmat7bg/1280/720', '/movie?id=12', 'HOME_HERO', 12, '2025-01-01', '2027-12-31', 1),
(N'Deadpool & Wolverine - Bom Tấn Hành Động Hài Marvel', 'https://picsum.photos/seed/deadpool3bg/1280/720', '/movie?id=14', 'HOME_HERO', 14, '2025-01-01', '2027-12-31', 1),
(N'Quỷ Ăn Tạng 2 (Tee Yod 2) - Kinh Dị Thái Lan 18+', 'https://picsum.photos/seed/teeyod2bg/1280/720', '/movie?id=8', 'HOME_HERO', 8, '2025-01-01', '2027-12-31', 1),
(N'Những Mảnh Ghép Cảm Xúc 2 (Inside Out 2) - Lồng Tiếng Việt', 'https://picsum.photos/seed/insideout2bg/1280/720', '/movie?id=5', 'HOME_HERO', 5, '2025-01-01', '2027-12-31', 1);

    COMMIT TRANSACTION;
    PRINT N'==========================================================================';
    PRINT N'ĐÃ NẠP THÀNH CÔNG CSDL ĐỒ ÁN: 30 TÀI KHOẢN (MD5) + 200 PHIM ĐA QUỐC GIA + VIETQR!';
    PRINT N'==========================================================================';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT N'LỖI SQL: ' + ERROR_MESSAGE();
    THROW;
END CATCH;
GO