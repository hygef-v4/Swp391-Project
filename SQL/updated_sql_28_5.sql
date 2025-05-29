USE master;
GO

IF EXISTS (SELECT NAME FROM sys.databases WHERE NAME = 'RoomBooking')
BEGIN
	ALTER DATABASE RoomBooking SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
	DROP DATABASE RoomBooking;
END
GO

CREATE DATABASE RoomBooking;
GO

USE RoomBooking;
GO

CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) DEFAULT '',
    email NVARCHAR(100) UNIQUE NOT NULL,
    password_hash NVARCHAR(255),
    phone NVARCHAR(20) DEFAULT '',
    role NVARCHAR(20) DEFAULT 'CUSTOMER' CHECK (role IN ('GUEST', 'CUSTOMER', 'HOTEL OWNER', 'MODERATOR', 'ADMIN')),
    is_active BIT DEFAULT 1
);

CREATE TABLE UserProfiles (
    profile_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    avatar_url NVARCHAR(255),
    address NVARCHAR(MAX),
    bio NVARCHAR(MAX),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE PasswordResetTokens (
    id INT PRIMARY KEY IDENTITY,
    user_id INT FOREIGN KEY REFERENCES Users(user_id),
    token NVARCHAR(255),
    expiry_date DATETIME
);

CREATE TABLE Locations (
    location_id INT IDENTITY(1,1) PRIMARY KEY,
    city_name NVARCHAR(255) UNIQUE NOT NULL,
    location_image_url NVARCHAR(255)
);

CREATE TABLE Hotels (
	hotel_id INT IDENTITY(1,1) PRIMARY KEY,
	host_id INT NOT NULL,
    hotel_name NVARCHAR(255) NOT NULL,
    hotel_image_url NVARCHAR(500),
	address NVARCHAR(MAX) NOT NULL,
	location_id INT NOT NULL,
    latitude FLOAT NOT NULL,
    longitude FLOAT NOT NULL,
    rating DECIMAL(3,1),
	description NVARCHAR(MAX),
	FOREIGN KEY (host_id) REFERENCES Users(user_id) ON DELETE CASCADE,
	FOREIGN KEY (location_id) REFERENCES Locations(location_id) ON DELETE CASCADE
);

CREATE TABLE RoomTypes (
    room_type_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255)
);

CREATE TABLE Rooms (
    room_id INT IDENTITY(1,1) PRIMARY KEY,
    hotel_id INT NOT NULL,                     
    title NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    price DECIMAL(10, 2) NOT NULL,
    max_guests INT NOT NULL,
	room_type_id INT NOT NULL,
	status NVARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'booked', 'ban')),
	quantity int DEFAULT 1,
    FOREIGN KEY (hotel_id) REFERENCES Hotels(hotel_id) ON DELETE CASCADE,
	FOREIGN KEY (room_type_id) REFERENCES RoomTypes(room_type_id) ON DELETE CASCADE
);

CREATE TABLE RoomImages (
    image_id INT IDENTITY(1,1) PRIMARY KEY,
    room_id INT NOT NULL,
    image_url NVARCHAR(255),
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE
);

CREATE TABLE Amenities (
    amenity_id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL UNIQUE,
    category NVARCHAR(50)
);

CREATE TABLE RoomAmenities (
    room_id INT,
    amenity_id INT,
    PRIMARY KEY (room_id, amenity_id),
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE,
    FOREIGN KEY (amenity_id) REFERENCES Amenities(amenity_id) ON DELETE CASCADE
);

CREATE TABLE Favorites (
    user_id INT,
    room_id INT,
    PRIMARY KEY (user_id, room_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE NO ACTION
);

CREATE TABLE Coupons (
    coupon_id INT PRIMARY KEY IDENTITY(1,1),
    code NVARCHAR(50) NOT NULL UNIQUE,         
    type NVARCHAR(20) NOT NULL CHECK (type IN ('percentage', 'fixed')),  
    amount DECIMAL(10,2) NOT NULL,   
    valid_from DATE DEFAULT GETDATE(),
    valid_until DATE NOT NULL,	
    usage_limit INT DEFAULT NULL,             
    used_count INT DEFAULT 0,  
    is_active BIT DEFAULT 1,                
    min_total_price DECIMAL(10,2) DEFAULT 0 
);

CREATE TABLE UserCoupons (
	user_id INT,
	coupon_id INT,
	PRIMARY KEY (user_id, coupon_id),
	FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
	FOREIGN KEY (coupon_id) REFERENCES Coupons(coupon_id) ON DELETE CASCADE
);

CREATE TABLE Bookings (
    booking_id INT IDENTITY(1,1) PRIMARY KEY,
    room_id INT NOT NULL,
    customer_id INT NOT NULL,
    coupon_id INT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status NVARCHAR(20) CHECK (status IN ('pending', 'approved', 'rejected', 'cancelled')),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES Users(user_id) ON DELETE NO ACTION,
    FOREIGN KEY (coupon_id) REFERENCES Coupons(coupon_id) ON DELETE SET NULL
);

CREATE TABLE Payments (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method NVARCHAR(50) NOT NULL CHECK (payment_method IN ('VNPAY', 'MOMO', 'CreditCard')),
    payment_status NVARCHAR(20) NOT NULL CHECK (payment_status IN ('pending', 'success', 'failed', 'refunded')),
    paid_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE
);

CREATE TABLE Reviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment NVARCHAR(MAX),
	is_public BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES Users(user_id) ON DELETE NO ACTION
);

CREATE TABLE Notifications (
    notification_id INT IDENTITY(1,1) PRIMARY KEY,
    message NVARCHAR(MAX),
	is_global BIT DEFAULT 0,
    sent_at DATETIME DEFAULT GETDATE(),
);

CREATE TABLE UserNotifications (
  user_id INT,
  notification_id INT,
  PRIMARY KEY (user_id, notification_id),
  FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE NO ACTION,
  FOREIGN KEY (notification_id) REFERENCES Notifications(notification_id) ON DELETE NO ACTION
);

CREATE TABLE Messages (
    message_id INT IDENTITY(1,1) PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content NVARCHAR(MAX),
    sent_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sender_id) REFERENCES Users(user_id) ON DELETE NO ACTION,
    FOREIGN KEY (receiver_id) REFERENCES Users(user_id) ON DELETE NO ACTION
);


CREATE TABLE Reports (
    report_id INT IDENTITY(1,1) PRIMARY KEY,
    reporter_id INT NOT NULL,
    reported_user_id INT NOT NULL,
    reason NVARCHAR(MAX),
    status NVARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending')),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (reporter_id) REFERENCES Users(user_id),
    FOREIGN KEY (reported_user_id) REFERENCES Users(user_id)
);

INSERT INTO RoomTypes (name) VALUES
(N'Phòng Tiêu Chuẩn'),
(N'Phòng Đôi'),
(N'Phòng Suite'),
(N'Villa'),
(N'Phòng Gia Đình'),
(N'Phòng Cabin'),
(N'Phòng Cao Cấp'),
(N'Phòng Vườn'),
(N'Phòng Cổ Điển'),
(N'Phòng VIP');


USE RoomBooking;
GO

-- 1. Users (Parent to many)
SET IDENTITY_INSERT [dbo].[Users] ON
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (1, N'Alice Nguyen', N'hashedpw1', N'alice.nguyen@example.com', N'0901234567', N'CUSTOMER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (2, N'Bob Tran', N'hashedpw2', N'bob.tran@example.com', N'0912345678', N'HOTEL OWNER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (3, N'Charlie Le', N'hashedpw3', N'charlie.le@example.com', N'0923456789', N'CUSTOMER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (4, N'Diana Vu', N'hashedpw4', N'diana.vu@example.com', N'0934567890', N'CUSTOMER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (5, N'Eric Pham', N'hashedpw5', N'eric.pham@example.com', N'0945678901', N'CUSTOMER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (6, N'Fiona Do', N'hashedpw6', N'fiona.do@example.com', N'0956789012', N'HOTEL OWNER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (7, N'George Nguyen', N'hashedpw7', N'george.nguyen@example.com', N'0967890123', N'ADMIN', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (8, N'Hien Mai', N'hashedpw8', N'hien.mai@example.com', N'0978901234', N'MODERATOR', 1) -- manager -> MODERATOR (assumption)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (9, N'Ivy Tran', N'hashedpw9', N'ivy.tran@example.com', N'0989012345', N'CUSTOMER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (10, N'Jack Le', N'hashedpw10', N'jack.le@example.com', N'0990123456', N'CUSTOMER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (13, N'', N'$2a$10$fV2ZjzMhz9a3LbOiPrXYvuqHokGuAFJwZHZGUDnEuMaI8qi56N6fW', N'cuong16032005@gmail.com', N'', N'CUSTOMER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (14, N'', N'$2a$10$CVFxGhbYzFfdmOOJZpYMu.Ny1bpUIUaImnACoKSOhmyViDBLdkF8q', N'minhtaivo22@gmail.com', N'', N'CUSTOMER', 1)
GO
INSERT [dbo].[Users] ([user_id], [full_name], [password_hash], [email], [phone], [role], [is_active]) VALUES (15, N'', N'$2a$10$skDjKu9XKXTFipLxs/8LIO9YnTlNvI66sya.OdNt4NkXKjxaIrYnK', N'hungsct1702@gmail.com', N'', N'CUSTOMER', 1)
GO
SET IDENTITY_INSERT [dbo].[Users] OFF
GO

-- 2. Locations (Parent to Hotels)
SET IDENTITY_INSERT [dbo].[Locations] ON
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (1, N'Đà Nẵng', N'https://pix6.agoda.net/geo/city/16440/1_16440_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (2, N'Sapa', N'https://pix6.agoda.net/geo/city/17160/1_17160_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (3, N'TP.HCM', N'https://pix6.agoda.net/geo/city/13170/1_13170_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (4, N'Quy Nhơn', N'https://pix6.agoda.net/geo/city/17242/1_17242_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (5, N'Nha Trang', N'https://pix6.agoda.net/geo/city/2679/1_2679_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (6, N'Đà Lạt', N'https://pix6.agoda.net/geo/city/15932/1_15932_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (7, N'Hà Nội', N'https://pix6.agoda.net/geo/city/2758/065f4f2c9fa263611ab65239ecbeaff7.jpg?ce=0&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (8, N'Huế', N'https://pix6.agoda.net/geo/city/3738/1_3738_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (9, N'Hạ Long', N'https://pix6.agoda.net/geo/city/17182/1_17182_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (10, N'Phan Thiết', N'https://pix6.agoda.net/geo/city/16264/1_16264_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (11, N'Cát Bà', N'https://pix6.agoda.net/geo/city/17243/1_17243_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
INSERT [dbo].[Locations] ([location_id], [city_name], [location_image_url]) VALUES (12, N'Phú Quốc', N'https://pix6.agoda.net/geo/city/17188/1_17188_02.jpg?ca=6&ce=1&s=375x&ar=1x1')
GO
SET IDENTITY_INSERT [dbo].[Locations] OFF
GO

-- 3. Amenities (Parent to RoomAmenities)
SET IDENTITY_INSERT [dbo].[Amenities] ON
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (1, N'Máy lạnh', N'Tiện nghi phòng')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (2, N'Wifi miễn phí', N'Kết nối')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (3, N'Tivi', N'Giải trí')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (4, N'Tủ lạnh mini', N'Tiện nghi phòng')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (5, N'Bồn tắm', N'Phòng tắm')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (6, N'Máy sấy tóc', N'Phòng tắm')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (7, N'Bàn làm việc', N'Tiện nghi phòng')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (8, N'Bữa sáng miễn phí', N'Dịch vụ')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (9, N'Chỗ đậu xe miễn phí', N'Dịch vụ')
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category]) VALUES (10, N'Hồ bơi', N'Tiện nghi chung')
GO
SET IDENTITY_INSERT [dbo].[Amenities] OFF
GO

-- 4. Coupons (Parent to Bookings, UserCoupons)
SET IDENTITY_INSERT [dbo].[Coupons] ON
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (1, N'WELCOME10', N'percentage', CAST(10.00 AS Decimal(10, 2)), 100, 5, CAST(500000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-12-31' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (2, N'HOTEL100K', N'fixed', CAST(100000.00 AS Decimal(10, 2)), 50, 10, CAST(800000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-11-30' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (3, N'SUMMER20', N'percentage', CAST(20.00 AS Decimal(10, 2)), 200, 20, CAST(1000000.00 AS Decimal(10, 2)), CAST(N'2025-06-01' AS Date), CAST(N'2025-08-31' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (4, N'FREESTAY', N'fixed', CAST(500000.00 AS Decimal(10, 2)), 10, 2, CAST(2000000.00 AS Decimal(10, 2)), CAST(N'2025-05-15' AS Date), CAST(N'2025-12-31' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (5, N'WEEKEND5', N'percentage', CAST(5.00 AS Decimal(10, 2)), NULL, 0, CAST(300000.00 AS Decimal(10, 2)), CAST(N'2025-01-01' AS Date), CAST(N'2025-12-31' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (6, N'VIPONLY', N'fixed', CAST(250000.00 AS Decimal(10, 2)), 5, 1, CAST(2500000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-07-01' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (7, N'LASTMINUTE', N'percentage', CAST(15.00 AS Decimal(10, 2)), 30, 4, CAST(400000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-05-31' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (8, N'FLASH50', N'fixed', CAST(50000.00 AS Decimal(10, 2)), 100, 50, CAST(200000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-06-15' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (9, N'NEWYEAR25', N'percentage', CAST(25.00 AS Decimal(10, 2)), 300, 10, CAST(1500000.00 AS Decimal(10, 2)), CAST(N'2025-12-15' AS Date), CAST(N'2026-01-10' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (10, N'BIGSALE', N'fixed', CAST(200000.00 AS Decimal(10, 2)), 20, 0, CAST(1000000.00 AS Decimal(10, 2)), CAST(N'2025-06-01' AS Date), CAST(N'2025-07-31' AS Date), 1)
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active]) VALUES (11, N'FALLDEAL', N'percentage', CAST(12.50 AS Decimal(10, 2)), 50, 0, CAST(3000000.00 AS Decimal(10, 2)), CAST(N'2025-09-01' AS Date), CAST(N'2025-10-31' AS Date), 1)
GO
SET IDENTITY_INSERT [dbo].[Coupons] OFF
GO

-- 5. Notifications (Parent to UserNotifications)
SET IDENTITY_INSERT [dbo].[Notifications] ON
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (1, N'Notification message 1', 0, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (2, N'Notification message 2', 0, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (3, N'Notification message 3', 1, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (4, N'Notification message 4', 0, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (5, N'Notification message 5', 0, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (6, N'Notification message 6', 1, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (7, N'Notification message 7', 0, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (8, N'Notification message 8', 0, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (9, N'Notification message 9', 1, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
INSERT [dbo].[Notifications] ([notification_id], [message], [is_global], [sent_at]) VALUES (10, N'Notification message 10', 0, CAST(N'2025-05-23T10:32:07.373' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Notifications] OFF
GO

-- 6. UserProfiles (Depends on Users)
SET IDENTITY_INSERT [dbo].[UserProfiles] ON
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (1, 1, N'/assets/images/team/01.jpg', N'Yêu thích du lịch và khám phá.', N'123 Lê Lợi, Q1, TP.HCM')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (2, 2, N'/assets/images/team/02.jpg', N'Chủ nhà thân thiện và nhiệt tình.', N'456 Trần Hưng Đạo, Hà Nội')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (3, 3, N'/assets/images/team/03.jpg', N'Khách hàng thường xuyên đặt phòng.', N'789 Nguyễn Huệ, Đà Nẵng')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (4, 4, N'/assets/images/team/04.jpg', N'Tôi thích nghỉ dưỡng vào cuối tuần.', N'12 Cách Mạng Tháng 8, Huế')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (5, 5, N'/assets/images/team/05.jpg', N'Du khách quốc tế đến từ Úc.', N'55 Hai Bà Trưng, Hội An')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (6, 6, N'/assets/images/team/06.jpg', N'Chủ nhà căn hộ cao cấp.', N'88 Pasteur, TP.HCM')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (7, 7, N'/assets/images/team/07.jpg', N'Quản trị viên hệ thống.', N'1 Nguyễn Văn Linh, Cần Thơ')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (8, 8, N'/assets/images/team/08.jpg', N'Quản lý vận hành hệ thống.', N'234 Phạm Văn Đồng, Đà Lạt')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (9, 9, N'/assets/images/team/09.jpg', N'Khách hàng yêu thích tiện nghi.', N'89 Trường Chinh, Nha Trang')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (10, 10, N'/assets/images/team/10.jpg', N'Thích trải nghiệm những nơi mới.', N'101 Lý Thường Kiệt, TP.HCM')
GO
SET IDENTITY_INSERT [dbo].[UserProfiles] OFF
GO

-- 7. PasswordResetTokens (Depends on Users)
SET IDENTITY_INSERT [dbo].[PasswordResetTokens] ON
GO
INSERT [dbo].[PasswordResetTokens] ([id], [user_id], [token], [expiry_date]) VALUES (1, 13, N'471284f1-49ab-4cb5-a345-81241313589d', CAST(N'2025-05-26T19:10:27.167' AS DateTime))
GO
INSERT [dbo].[PasswordResetTokens] ([id], [user_id], [token], [expiry_date]) VALUES (3, 13, N'2983c1cd-32d1-48b9-b010-57697fffe235', CAST(N'2025-05-26T19:28:39.437' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[PasswordResetTokens] OFF
GO

-- 8. Hotels (Depends on Users, Locations)
SET IDENTITY_INSERT [dbo].[Hotels] ON
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (1, 2, N'Sunrise Hotel', N'123 Biển Đông, Đà Nẵng', N'Khách sạn gần biển, view bình minh tuyệt đẹp.', 1, N'https://a0.muscache.com/im/pictures/miso/Hosting-563395722592154027/original/1c876f75-c12e-4685-9bfc-ed0deeba23bc.jpeg?im_w=1200', CAST(4.5 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (2, 2, N'Mountain Retreat', N'456 Đỉnh Núi, Sapa', N'Khu nghỉ dưỡng yên bình giữa núi rừng.', 2, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1397063721518116060/original/d8fefc5c-e6cf-4638-8ccb-4505ddf091b1.jpeg?im_w=1200', CAST(4.7 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (3, 2, N'City Center Inn', N'789 Nguyễn Huệ, TP.HCM', N'Tọa lạc ngay trung tâm thành phố.', 3, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1290893809938879853/original/2bc15218-97ef-47fb-9fdf-47214f4e72bb.jpeg?im_w=1200', CAST(4.6 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (4, 2, N'Riverside Villa', N'101 Bờ Sông, Quy Nhơn', N'Biệt thự ven sông cho kỳ nghỉ thư giãn.', 4, N'https://a0.muscache.com/im/pictures/33e52704-0cbb-4956-9615-833c37e9b9a0.jpg?im_w=1200', CAST(4.3 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (5, 2, N'Ocean Pearl Resort', N'22 Trần Phú, Nha Trang', N'Khu nghỉ dưỡng cao cấp sát biển.', 5, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1137255372775817650/original/62bb905e-39d8-4c92-8d25-933703d5ffb9.jpeg?im_w=1200', CAST(4.8 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (6, 6, N'Forest Lodge', N'77 Rừng Thông, Đà Lạt', N'Không gian yên tĩnh giữa thiên nhiên.', 6, N'https://a0.muscache.com/im/pictures/miso/Hosting-1151210001062722717/original/093d7ce4-c63a-4366-85cf-3f5d5e0462e3.jpeg?im_w=1200', CAST(4.4 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (7, 6, N'Skyline Hotel', N'88 Lê Lợi, Hà Nội', N'Tầng cao với tầm nhìn thành phố.', 7, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1245878818590732072/original/12b33c38-6b3b-45f9-b7f8-1bf8fd73c49e.jpeg?im_w=1200', CAST(4.2 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (8, 6, N'Garden Paradise', N'19 Hoa Viên, Huế', N'Sân vườn xanh mát và tiện nghi.', 8, N'https://a0.muscache.com/im/pictures/miso/Hosting-701040943529768791/original/a0881fd9-0e23-4ab5-b3ca-45df5f7409f4.jpeg?im_w=1200', CAST(4.6 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (9, 6, N'Heritage Hotel', N'321 Phố Cổ, Hạ Long', N'Phong cách cổ kính, gần các di tích.', 9, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1039793559517374786/original/3d27e14c-24ec-42a7-b3f9-27cb3a9c6faf.jpeg?im_w=1200', CAST(4.9 AS Decimal(3, 1)), 0.0, 0.0)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (10, 6, N'Star Luxury', N'654 Nguyễn Văn Linh, TP.HCM', N'Khách sạn 5 sao với dịch vụ đẳng cấp.', 3, N'https://a0.muscache.com/im/pictures/miso/Hosting-12414161/original/272cedc5-e40f-41f0-8146-1245474cfacd.jpeg?im_w=1200', CAST(4.7 AS Decimal(3, 1)), 0.0, 0.0)
GO
SET IDENTITY_INSERT [dbo].[Hotels] OFF
GO

-- 9. Rooms (Depends on Hotels, RoomTypes - RoomTypes already populated by DDL)
SET IDENTITY_INSERT [dbo].[Rooms] ON
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (1, 1, N'Phòng Đơn Biển', N'Phòng đơn có cửa sổ nhìn ra biển.', CAST(500000.00 AS Decimal(10, 2)), 1, N'active', 1, 5)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (2, 2, N'Phòng Đôi Núi', N'Phòng đôi với view núi tuyệt đẹp.', CAST(750000.00 AS Decimal(10, 2)), 2, N'active', 2, 4)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (3, 3, N'Suite Trung Tâm', N'Suite rộng rãi ngay trung tâm thành phố.', CAST(1200000.00 AS Decimal(10, 2)), 3, N'active', 3, 6)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (4, 4, N'Villa Sông', N'Villa riêng biệt cạnh bờ sông.', CAST(1800000.00 AS Decimal(10, 2)), 4, N'active', 4, 3)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (5, 5, N'Phòng Gia Đình Biển', N'Phòng phù hợp cho gia đình, gần biển.', CAST(1500000.00 AS Decimal(10, 2)), 4, N'booked', 5, 2)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (6, 6, N'Phòng Cabin Rừng', N'Phòng gỗ giữa rừng thông yên tĩnh.', CAST(900000.00 AS Decimal(10, 2)), 2, N'active', 6, 3)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (7, 7, N'Phòng Cao Cấp View Thành Phố', N'Tầng cao, nhìn ra skyline Hà Nội.', CAST(1100000.00 AS Decimal(10, 2)), 2, N'inactive', 7, 4)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (8, 8, N'Phòng Sân Vườn', N'Phòng có ban công nhìn ra vườn.', CAST(850000.00 AS Decimal(10, 2)), 2, N'active', 8, 2)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (9, 9, N'Phòng Phố Cổ', N'Phòng theo phong cách cổ Hội An.', CAST(950000.00 AS Decimal(10, 2)), 2, N'active', 9, 5)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [room_type_id], [quantity]) VALUES (10, 10, N'Phòng VIP 5 Sao', N'Phòng cao cấp với dịch vụ hàng đầu.', CAST(2200000.00 AS Decimal(10, 2)), 4, N'ban', 10, 1)
GO
SET IDENTITY_INSERT [dbo].[Rooms] OFF
GO

-- 10. RoomImages (Depends on Rooms)
SET IDENTITY_INSERT [dbo].[RoomImages] ON
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (1, 1, N'https://example.com/images/room1.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (2, 2, N'https://example.com/images/room2.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (3, 3, N'https://example.com/images/room3.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (4, 4, N'https://example.com/images/room4.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (5, 5, N'https://example.com/images/room5.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (6, 6, N'https://example.com/images/room6.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (7, 7, N'https://example.com/images/room7.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (8, 8, N'https://example.com/images/room8.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (9, 9, N'https://example.com/images/room9.jpg')
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url]) VALUES (10, 10, N'https://example.com/images/room10.jpg')
GO
SET IDENTITY_INSERT [dbo].[RoomImages] OFF
GO

-- 11. RoomAmenities (Depends on Rooms, Amenities)
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (1, 4)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (1, 6)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (1, 7)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (2, 1)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (2, 2)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (2, 3)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (2, 5)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (2, 7)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (2, 8)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (2, 9)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (3, 1)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (3, 3)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (3, 4)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (3, 6)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (3, 8)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (4, 3)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (4, 5)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (4, 6)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (4, 8)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (4, 9)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (5, 3)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (5, 4)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (5, 7)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (5, 10)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (6, 1)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (6, 4)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (6, 6)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (6, 7)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (7, 4)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (7, 6)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (7, 9)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (7, 10)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (8, 1)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (8, 2)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (8, 6)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (8, 7)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (8, 10)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (9, 3)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (9, 5)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (9, 10)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (10, 2)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (10, 3)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (10, 7)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (10, 8)
GO
INSERT [dbo].[RoomAmenities] ([room_id], [amenity_id]) VALUES (10, 10)
GO

-- 12. Favorites (Depends on Users, Rooms)
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (1, 7)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (3, 4)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (5, 1)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (5, 2)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (5, 3)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (5, 8)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (5, 9)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (6, 5)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (9, 3)
GO
INSERT [dbo].[Favorites] ([user_id], [room_id]) VALUES (10, 2)
GO

-- 13. UserCoupons (Depends on Users, Coupons)
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (1, 1)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (1, 2)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (2, 3)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (2, 4)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (3, 5)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (3, 6)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (4, 7)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (4, 8)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (5, 9)
GO
INSERT [dbo].[UserCoupons] ([user_id], [coupon_id]) VALUES (5, 10)
GO

-- 14. Bookings (Depends on Rooms, Users, Coupons)
SET IDENTITY_INSERT [dbo].[Bookings] ON
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (1, 1, 1, 1, CAST(N'2025-06-01' AS Date), CAST(N'2025-06-03' AS Date), CAST(950000.00 AS Decimal(10, 2)), N'approved', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (2, 2, 2, 2, CAST(N'2025-06-05' AS Date), CAST(N'2025-06-07' AS Date), CAST(1400000.00 AS Decimal(10, 2)), N'approved', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (3, 3, 3, NULL, CAST(N'2025-06-10' AS Date), CAST(N'2025-06-12' AS Date), CAST(2400000.00 AS Decimal(10, 2)), N'pending', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (4, 4, 4, 4, CAST(N'2025-06-15' AS Date), CAST(N'2025-06-17' AS Date), CAST(3200000.00 AS Decimal(10, 2)), N'cancelled', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (5, 5, 5, NULL, CAST(N'2025-06-20' AS Date), CAST(N'2025-06-22' AS Date), CAST(3000000.00 AS Decimal(10, 2)), N'approved', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (6, 6, 1, 5, CAST(N'2025-06-25' AS Date), CAST(N'2025-06-26' AS Date), CAST(850000.00 AS Decimal(10, 2)), N'rejected', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (7, 7, 2, NULL, CAST(N'2025-07-01' AS Date), CAST(N'2025-07-03' AS Date), CAST(2200000.00 AS Decimal(10, 2)), N'approved', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (8, 8, 3, 6, CAST(N'2025-07-05' AS Date), CAST(N'2025-07-06' AS Date), CAST(900000.00 AS Decimal(10, 2)), N'approved', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (9, 9, 4, NULL, CAST(N'2025-07-10' AS Date), CAST(N'2025-07-12' AS Date), CAST(1900000.00 AS Decimal(10, 2)), N'pending', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
INSERT [dbo].[Bookings] ([booking_id], [room_id], [customer_id], [coupon_id], [check_in], [check_out], [total_price], [status], [created_at]) VALUES (10, 10, 5, 7, CAST(N'2025-07-15' AS Date), CAST(N'2025-07-17' AS Date), CAST(3900000.00 AS Decimal(10, 2)), N'cancelled', CAST(N'2025-05-23T10:32:07.330' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Bookings] OFF
GO

-- 15. Payments (Depends on Bookings)
SET IDENTITY_INSERT [dbo].[Payments] ON
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (1, 1, N'MOMO', CAST(2601869.68 AS Decimal(10, 2)), N'success', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (2, 2, N'CreditCard', CAST(1966365.37 AS Decimal(10, 2)), N'pending', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (3, 3, N'MOMO', CAST(824472.53 AS Decimal(10, 2)), N'refunded', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (4, 4, N'VNPAY', CAST(2067198.37 AS Decimal(10, 2)), N'failed', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (5, 5, N'MOMO', CAST(541013.43 AS Decimal(10, 2)), N'failed', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (6, 6, N'MOMO', CAST(606470.78 AS Decimal(10, 2)), N'refunded', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (7, 7, N'MOMO', CAST(1671953.23 AS Decimal(10, 2)), N'success', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (8, 8, N'MOMO', CAST(1392636.43 AS Decimal(10, 2)), N'success', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (9, 9, N'CreditCard', CAST(1035340.51 AS Decimal(10, 2)), N'failed', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
INSERT [dbo].[Payments] ([payment_id], [booking_id], [payment_method], [amount], [payment_status], [paid_at]) VALUES (10, 10, N'MOMO', CAST(840107.50 AS Decimal(10, 2)), N'pending', CAST(N'2025-05-23T10:32:07.503' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Payments] OFF
GO

-- 16. Reviews (Depends on Bookings, Users)
SET IDENTITY_INSERT [dbo].[Reviews] ON
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (1, 1, 1, 5, N'Phòng rất sạch sẽ và có view biển tuyệt đẹp!', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (2, 2, 2, 4, N'Dịch vụ tốt, nhân viên thân thiện và chu đáo.', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (3, 3, 3, 4, N'Không gian yên tĩnh, phòng ốc sạch sẽ.', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (4, 4, 4, 5, N'Khách sạn hiện đại, vị trí trung tâm, rất tiện lợi.', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (5, 5, 5, 5, N'Tuyệt vời! Sẽ quay lại lần sau.', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (6, 6, 6, 5, N'Phòng rất sạch sẽ và có view biển tuyệt đẹp!', 0, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (7, 7, 7, 4, N'Khách sạn hiện đại, gần trung tâm.', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (8, 8, 8, 5, N'Rất ưng ý với không gian và thái độ phục vụ.', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (9, 9, 9, 4, N'Rất hài lòng với kỳ nghỉ tại đây. Sẽ quay lại!', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
INSERT [dbo].[Reviews] ([review_id], [booking_id], [reviewer_id], [rating], [comment], [is_public], [created_at]) VALUES (10, 10, 10, 4, N'Phòng đẹp, nhân viên thân thiện.', 1, CAST(N'2025-05-23T10:32:07.353' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Reviews] OFF
GO

-- 17. UserNotifications (Depends on Users, Notifications)
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (1, 1)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (1, 2)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (1, 4)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (1, 5)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (1, 8)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (1, 9)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (1, 10)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (2, 3)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (2, 7)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (2, 10)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (3, 2)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (3, 5)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (3, 10)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (4, 2)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (4, 8)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (4, 10)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (5, 1)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (5, 4)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (5, 6)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (5, 7)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (6, 1)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (6, 4)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (6, 5)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (6, 7)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (7, 1)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (7, 2)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (7, 3)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (7, 4)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (7, 5)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (7, 8)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (7, 10)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (8, 1)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (8, 2)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (8, 3)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (8, 10)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (9, 3)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (9, 4)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (9, 6)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (9, 9)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (10, 1)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (10, 6)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (10, 7)
GO
INSERT [dbo].[UserNotifications] ([user_id], [notification_id]) VALUES (10, 9)
GO

-- 18. Messages (Depends on Users)
SET IDENTITY_INSERT [dbo].[Messages] ON
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (1, 6, 4, N'Message 1 from user 6 to 4', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (2, 5, 8, N'Message 2 from user 5 to 8', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (3, 1, 8, N'Message 3 from user 1 to 8', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (4, 6, 5, N'Message 4 from user 6 to 5', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (5, 9, 3, N'Message 5 from user 9 to 3', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (6, 2, 7, N'Message 6 from user 2 to 7', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (7, 4, 2, N'Message 7 from user 4 to 2', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (8, 5, 10, N'Message 8 from user 5 to 10', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (9, 8, 5, N'Message 9 from user 8 to 5', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
INSERT [dbo].[Messages] ([message_id], [sender_id], [receiver_id], [content], [sent_at]) VALUES (10, 9, 10, N'Message 10 from user 9 to 10', CAST(N'2025-05-23T10:32:07.363' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Messages] OFF
GO

-- 19. Reports (Depends on Users)
SET IDENTITY_INSERT [dbo].[Reports] ON
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (1, 2, 9, N'Report reason 1', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (2, 7, 9, N'Report reason 2', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (3, 5, 4, N'Report reason 3', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (4, 2, 7, N'Report reason 4', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (5, 7, 10, N'Report reason 5', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (6, 4, 10, N'Report reason 6', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (7, 8, 9, N'Report reason 7', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (8, 3, 10, N'Report reason 8', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (9, 2, 8, N'Report reason 9', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
INSERT [dbo].[Reports] ([report_id], [reporter_id], [reported_user_id], [reason], [status], [created_at]) VALUES (10, 8, 3, N'Report reason 10', N'pending', CAST(N'2025-05-23T10:32:07.513' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Reports] OFF
GO