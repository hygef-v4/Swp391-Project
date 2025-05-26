USE master

IF EXISTS (SELECT NAME FROM sys.databases WHERE NAME = 'RoomBooking')
BEGIN
	ALTER DATABASE RoomBooking SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
	DROP DATABASE RoomBooking;
END
GO

CREATE DATABASE RoomBooking
GO
use RoomBooking
GO
CREATE TABLE [dbo].[Amenities](
	[amenity_id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](100) NOT NULL,
	[category] [nvarchar](50) NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
 CONSTRAINT [PK__Amenitie__E908452D4C77B051] PRIMARY KEY CLUSTERED 
(
	[amenity_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Bookings]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Bookings](
	[booking_id] [int] IDENTITY(1,1) NOT NULL,
	[room_id] [int] NOT NULL,
	[customer_id] [int] NOT NULL,
	[coupon_id] [int] NULL,
	[check_in] [date] NOT NULL,
	[check_out] [date] NOT NULL,
	[total_price] [decimal](10, 2) NOT NULL,
	[status] [nvarchar](20) NULL,
	[created_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[booking_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Coupons]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Coupons](
	[coupon_id] [int] IDENTITY(1,1) NOT NULL,
	[code] [varchar](50) NOT NULL,
	[type] [varchar](20) NOT NULL,
	[amount] [decimal](10, 2) NOT NULL,
	[usage_limit] [int] NULL,
	[used_count] [int] NULL,
	[min_total_price] [decimal](10, 2) NULL,
	[valid_from] [date] NOT NULL,
	[valid_until] [date] NOT NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[coupon_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Favorites]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Favorites](
	[user_id] [int] NOT NULL,
	[room_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC,
	[room_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Hotels]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Hotels](
	[hotel_id] [int] IDENTITY(1,1) NOT NULL,
	[host_id] [int] NOT NULL,
	[hotel_name] [nvarchar](255) NOT NULL,
	[address] [nvarchar](max) NOT NULL,
	[description] [nvarchar](max) NULL,
	[location_id] [int] NULL,
	[hotel_image_url] [nvarchar](max) NULL,
	[rating] [decimal](2, 1) NULL,
	[latitude] [decimal](20, 15) NULL,
	[longitude] [decimal](20, 15) NULL,
PRIMARY KEY CLUSTERED 
(
	[hotel_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Locations]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Locations](
	[location_id] [int] IDENTITY(1,1) NOT NULL,
	[city_name] [nvarchar](100) NOT NULL,
	[location_image_url] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
	[location_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Messages]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Messages](
	[message_id] [int] IDENTITY(1,1) NOT NULL,
	[sender_id] [int] NOT NULL,
	[receiver_id] [int] NOT NULL,
	[content] [nvarchar](max) NULL,
	[sent_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[message_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Notifications]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Notifications](
	[notification_id] [int] IDENTITY(1,1) NOT NULL,
	[message] [nvarchar](max) NULL,
	[is_global] [bit] NULL,
	[sent_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[notification_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Payments]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Payments](
	[payment_id] [int] IDENTITY(1,1) NOT NULL,
	[booking_id] [int] NOT NULL,
	[payment_method] [nvarchar](50) NULL,
	[amount] [decimal](10, 2) NOT NULL,
	[payment_status] [nvarchar](20) NULL,
	[paid_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[payment_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Reports]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Reports](
	[report_id] [int] IDENTITY(1,1) NOT NULL,
	[reporter_id] [int] NOT NULL,
	[reported_user_id] [int] NOT NULL,
	[reason] [nvarchar](max) NULL,
	[status] [nvarchar](20) NULL,
	[created_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[report_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Reviews]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Reviews](
	[review_id] [int] IDENTITY(1,1) NOT NULL,
	[booking_id] [int] NOT NULL,
	[reviewer_id] [int] NOT NULL,
	[rating] [int] NULL,
	[comment] [nvarchar](max) NULL,
	[is_public] [bit] NULL,
	[created_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[review_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomAmenities]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoomAmenities](
	[room_id] [int] NOT NULL,
	[amenity_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[room_id] ASC,
	[amenity_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomImages]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoomImages](
	[image_id] [int] IDENTITY(1,1) NOT NULL,
	[room_id] [int] NOT NULL,
	[image_url] [nvarchar](255) NULL,
	[is_cover] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[image_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Rooms]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Rooms](
	[room_id] [int] IDENTITY(1,1) NOT NULL,
	[hotel_id] [int] NOT NULL,
	[title] [nvarchar](255) NOT NULL,
	[description] [nvarchar](max) NULL,
	[price] [decimal](10, 2) NOT NULL,
	[max_guests] [int] NOT NULL,
	[status] [nvarchar](20) NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
	[room_type_id] [int] NULL,
	[quantity] [int] NULL,
 CONSTRAINT [PK__Rooms__19675A8A3783044D] PRIMARY KEY CLUSTERED 
(
	[room_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomTypes]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoomTypes](
	[room_type_id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](100) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[room_type_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserCoupons]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[UserCoupons](
	[user_id] [int] NOT NULL,
	[coupon_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC,
	[coupon_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserNotifications]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[UserNotifications](
	[user_id] [int] NOT NULL,
	[notification_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC,
	[notification_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserProfiles]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[UserProfiles](
	[profile_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NOT NULL,
	[avatar_url] [nvarchar](255) NULL,
	[bio] [nvarchar](max) NULL,
	[address] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
	[profile_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Users]    Script Date: 05/25/25 04:59:51 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Users](
	[user_id] [int] IDENTITY(1,1) NOT NULL,
	[full_name] [nvarchar](100) NOT NULL,
	[username] [nchar](50) NOT NULL,
	[password] [nvarchar](255) NOT NULL,
	[email] [nvarchar](100) NOT NULL,
	[phone] [nvarchar](20) NULL,
	[role] [nvarchar](20) NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
 CONSTRAINT [PK__Users__B9BE370FBC933F6B] PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[Amenities] ON 
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (1, N'Máy lạnh', N'Tiện nghi phòng', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (2, N'Wifi miễn phí', N'Kết nối', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (3, N'Tivi', N'Giải trí', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (4, N'Tủ lạnh mini', N'Tiện nghi phòng', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (5, N'Bồn tắm', N'Phòng tắm', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (6, N'Máy sấy tóc', N'Phòng tắm', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (7, N'Bàn làm việc', N'Tiện nghi phòng', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (8, N'Bữa sáng miễn phí', N'Dịch vụ', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (9, N'Chỗ đậu xe miễn phí', N'Dịch vụ', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
INSERT [dbo].[Amenities] ([amenity_id], [name], [category], [is_active], [created_at], [updated_at]) VALUES (10, N'Hồ bơi', N'Tiện nghi chung', 1, CAST(N'2025-05-23T10:32:07.440' AS DateTime), CAST(N'2025-05-23T10:32:07.440' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Amenities] OFF
GO
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
SET IDENTITY_INSERT [dbo].[Coupons] ON 
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (1, N'WELCOME10', N'percentage', CAST(10.00 AS Decimal(10, 2)), 100, 5, CAST(500000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-12-31' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (2, N'HOTEL100K', N'fixed', CAST(100000.00 AS Decimal(10, 2)), 50, 10, CAST(800000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-11-30' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (3, N'SUMMER20', N'percentage', CAST(20.00 AS Decimal(10, 2)), 200, 20, CAST(1000000.00 AS Decimal(10, 2)), CAST(N'2025-06-01' AS Date), CAST(N'2025-08-31' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (4, N'FREESTAY', N'fixed', CAST(500000.00 AS Decimal(10, 2)), 10, 2, CAST(2000000.00 AS Decimal(10, 2)), CAST(N'2025-05-15' AS Date), CAST(N'2025-12-31' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (5, N'WEEKEND5', N'percentage', CAST(5.00 AS Decimal(10, 2)), NULL, 0, CAST(300000.00 AS Decimal(10, 2)), CAST(N'2025-01-01' AS Date), CAST(N'2025-12-31' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (6, N'VIPONLY', N'fixed', CAST(250000.00 AS Decimal(10, 2)), 5, 1, CAST(2500000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-07-01' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (7, N'LASTMINUTE', N'percentage', CAST(15.00 AS Decimal(10, 2)), 30, 4, CAST(400000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-05-31' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (8, N'FLASH50', N'fixed', CAST(50000.00 AS Decimal(10, 2)), 100, 50, CAST(200000.00 AS Decimal(10, 2)), CAST(N'2025-05-01' AS Date), CAST(N'2025-06-15' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (9, N'NEWYEAR25', N'percentage', CAST(25.00 AS Decimal(10, 2)), 300, 10, CAST(1500000.00 AS Decimal(10, 2)), CAST(N'2025-12-15' AS Date), CAST(N'2026-01-10' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (10, N'BIGSALE', N'fixed', CAST(200000.00 AS Decimal(10, 2)), 20, 0, CAST(1000000.00 AS Decimal(10, 2)), CAST(N'2025-06-01' AS Date), CAST(N'2025-07-31' AS Date), 1, CAST(N'2025-05-23T10:32:07.287' AS DateTime))
GO
INSERT [dbo].[Coupons] ([coupon_id], [code], [type], [amount], [usage_limit], [used_count], [min_total_price], [valid_from], [valid_until], [is_active], [created_at]) VALUES (11, N'FALLDEAL', N'percentage', CAST(12.50 AS Decimal(10, 2)), 50, 0, CAST(3000000.00 AS Decimal(10, 2)), CAST(N'2025-09-01' AS Date), CAST(N'2025-10-31' AS Date), 1, CAST(N'2025-05-23T11:04:52.673' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Coupons] OFF
GO
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
SET IDENTITY_INSERT [dbo].[Hotels] ON 
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (1, 2, N'Sunrise Hotel', N'123 Biển Đông, Đà Nẵng', N'Khách sạn gần biển, view bình minh tuyệt đẹp.', 1, N'https://a0.muscache.com/im/pictures/miso/Hosting-563395722592154027/original/1c876f75-c12e-4685-9bfc-ed0deeba23bc.jpeg?im_w=1200', CAST(4.5 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (2, 2, N'Mountain Retreat', N'456 Đỉnh Núi, Sapa', N'Khu nghỉ dưỡng yên bình giữa núi rừng.', 2, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1397063721518116060/original/d8fefc5c-e6cf-4638-8ccb-4505ddf091b1.jpeg?im_w=1200', CAST(4.7 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (3, 2, N'City Center Inn', N'789 Nguyễn Huệ, TP.HCM', N'Tọa lạc ngay trung tâm thành phố.', 3, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1290893809938879853/original/2bc15218-97ef-47fb-9fdf-47214f4e72bb.jpeg?im_w=1200', CAST(4.6 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (4, 2, N'Riverside Villa', N'101 Bờ Sông, Quy Nhơn', N'Biệt thự ven sông cho kỳ nghỉ thư giãn.', 4, N'https://a0.muscache.com/im/pictures/33e52704-0cbb-4956-9615-833c37e9b9a0.jpg?im_w=1200', CAST(4.3 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (5, 2, N'Ocean Pearl Resort', N'22 Trần Phú, Nha Trang', N'Khu nghỉ dưỡng cao cấp sát biển.', 5, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1137255372775817650/original/62bb905e-39d8-4c92-8d25-933703d5ffb9.jpeg?im_w=1200', CAST(4.8 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (6, 6, N'Forest Lodge', N'77 Rừng Thông, Đà Lạt', N'Không gian yên tĩnh giữa thiên nhiên.', 6, N'https://a0.muscache.com/im/pictures/miso/Hosting-1151210001062722717/original/093d7ce4-c63a-4366-85cf-3f5d5e0462e3.jpeg?im_w=1200', CAST(4.4 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (7, 6, N'Skyline Hotel', N'88 Lê Lợi, Hà Nội', N'Tầng cao với tầm nhìn thành phố.', 7, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1245878818590732072/original/12b33c38-6b3b-45f9-b7f8-1bf8fd73c49e.jpeg?im_w=1200', CAST(4.2 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (8, 6, N'Garden Paradise', N'19 Hoa Viên, Huế', N'Sân vườn xanh mát và tiện nghi.', 8, N'https://a0.muscache.com/im/pictures/miso/Hosting-701040943529768791/original/a0881fd9-0e23-4ab5-b3ca-45df5f7409f4.jpeg?im_w=1200', CAST(4.6 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (9, 6, N'Heritage Hotel', N'321 Phố Cổ, Hạ Long', N'Phong cách cổ kính, gần các di tích.', 9, N'https://a0.muscache.com/im/pictures/hosting/Hosting-1039793559517374786/original/3d27e14c-24ec-42a7-b3f9-27cb3a9c6faf.jpeg?im_w=1200', CAST(4.9 AS Decimal(2, 1)), NULL, NULL)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [location_id], [hotel_image_url], [rating], [latitude], [longitude]) VALUES (10, 6, N'Star Luxury', N'654 Nguyễn Văn Linh, TP.HCM', N'Khách sạn 5 sao với dịch vụ đẳng cấp.', 3, N'https://a0.muscache.com/im/pictures/miso/Hosting-12414161/original/272cedc5-e40f-41f0-8146-1245474cfacd.jpeg?im_w=1200', CAST(4.7 AS Decimal(2, 1)), NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[Hotels] OFF
GO
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
SET IDENTITY_INSERT [dbo].[RoomImages] ON 
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (1, 1, N'https://example.com/images/room1.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (2, 2, N'https://example.com/images/room2.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (3, 3, N'https://example.com/images/room3.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (4, 4, N'https://example.com/images/room4.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (5, 5, N'https://example.com/images/room5.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (6, 6, N'https://example.com/images/room6.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (7, 7, N'https://example.com/images/room7.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (8, 8, N'https://example.com/images/room8.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (9, 9, N'https://example.com/images/room9.jpg', 1)
GO
INSERT [dbo].[RoomImages] ([image_id], [room_id], [image_url], [is_cover]) VALUES (10, 10, N'https://example.com/images/room10.jpg', 1)
GO
SET IDENTITY_INSERT [dbo].[RoomImages] OFF
GO
SET IDENTITY_INSERT [dbo].[Rooms] ON 
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (1, 1, N'Phòng Đơn Biển', N'Phòng đơn có cửa sổ nhìn ra biển.', CAST(500000.00 AS Decimal(10, 2)), 1, N'active', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 1, 5)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (2, 2, N'Phòng Đôi Núi', N'Phòng đôi với view núi tuyệt đẹp.', CAST(750000.00 AS Decimal(10, 2)), 2, N'active', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 2, 4)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (3, 3, N'Suite Trung Tâm', N'Suite rộng rãi ngay trung tâm thành phố.', CAST(1200000.00 AS Decimal(10, 2)), 3, N'active', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 3, 6)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (4, 4, N'Villa Sông', N'Villa riêng biệt cạnh bờ sông.', CAST(1800000.00 AS Decimal(10, 2)), 4, N'active', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 4, 3)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (5, 5, N'Phòng Gia Đình Biển', N'Phòng phù hợp cho gia đình, gần biển.', CAST(1500000.00 AS Decimal(10, 2)), 4, N'booked', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 5, 2)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (6, 6, N'Phòng Cabin Rừng', N'Phòng gỗ giữa rừng thông yên tĩnh.', CAST(900000.00 AS Decimal(10, 2)), 2, N'active', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 6, 3)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (7, 7, N'Phòng Cao Cấp View Thành Phố', N'Tầng cao, nhìn ra skyline Hà Nội.', CAST(1100000.00 AS Decimal(10, 2)), 2, N'inactive', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 7, 4)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (8, 8, N'Phòng Sân Vườn', N'Phòng có ban công nhìn ra vườn.', CAST(850000.00 AS Decimal(10, 2)), 2, N'active', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 8, 2)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (9, 9, N'Phòng Phố Cổ', N'Phòng theo phong cách cổ Hội An.', CAST(950000.00 AS Decimal(10, 2)), 2, N'active', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 9, 5)
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at], [room_type_id], [quantity]) VALUES (10, 10, N'Phòng VIP 5 Sao', N'Phòng cao cấp với dịch vụ hàng đầu.', CAST(2200000.00 AS Decimal(10, 2)), 4, N'ban', CAST(N'2025-05-23T10:32:07.257' AS DateTime), CAST(N'2025-05-23T10:32:07.257' AS DateTime), 10, 1)
GO
SET IDENTITY_INSERT [dbo].[Rooms] OFF
GO
SET IDENTITY_INSERT [dbo].[RoomTypes] ON 
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (6, N'Phòng Cabin')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (7, N'Phòng Cao Cấp')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (9, N'Phòng Cổ Điển')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (2, N'Phòng Đôi')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (5, N'Phòng Gia Đình')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (3, N'Phòng Suite')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (1, N'Phòng Tiêu Chuẩn')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (10, N'Phòng VIP')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (8, N'Phòng Vườn')
GO
INSERT [dbo].[RoomTypes] ([room_type_id], [name]) VALUES (4, N'Villa')
GO
SET IDENTITY_INSERT [dbo].[RoomTypes] OFF
GO
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
SET IDENTITY_INSERT [dbo].[UserProfiles] ON 
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (1, 1, N'/assets/images/team/01.jpg
', N'Yêu thích du lịch và khám phá.', N'123 Lê Lợi, Q1, TP.HCM')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (2, 2, N'/assets/images/team/02.jpg
', N'Chủ nhà thân thiện và nhiệt tình.', N'456 Trần Hưng Đạo, Hà Nội')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (3, 3, N'/assets/images/team/03.jpg
', N'Khách hàng thường xuyên đặt phòng.', N'789 Nguyễn Huệ, Đà Nẵng')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (4, 4, N'/assets/images/team/04.jpg
', N'Tôi thích nghỉ dưỡng vào cuối tuần.', N'12 Cách Mạng Tháng 8, Huế')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (5, 5, N'/assets/images/team/05.jpg
', N'Du khách quốc tế đến từ Úc.', N'55 Hai Bà Trưng, Hội An')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (6, 6, N'/assets/images/team/06.jpg
', N'Chủ nhà căn hộ cao cấp.', N'88 Pasteur, TP.HCM')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (7, 7, N'/assets/images/team/07.jpg
', N'Quản trị viên hệ thống.', N'1 Nguyễn Văn Linh, Cần Thơ')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (8, 8, N'/assets/images/team/08.jpg
', N'Quản lý vận hành hệ thống.', N'234 Phạm Văn Đồng, Đà Lạt')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (9, 9, N'/assets/images/team/09.jpg
', N'Khách hàng yêu thích tiện nghi.', N'89 Trường Chinh, Nha Trang')
GO
INSERT [dbo].[UserProfiles] ([profile_id], [user_id], [avatar_url], [bio], [address]) VALUES (10, 10, N'/assets/images/team/10.jpg
', N'Thích trải nghiệm những nơi mới.', N'101 Lý Thường Kiệt, TP.HCM')
GO
SET IDENTITY_INSERT [dbo].[UserProfiles] OFF
GO
SET IDENTITY_INSERT [dbo].[Users] ON 
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (1, N'Alice Nguyen', N'alice1                                            ', N'hashedpw1', N'alice.nguyen@example.com', N'0901234567', N'customer', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (2, N'Bob Tran', N'bob2                                              ', N'hashedpw2', N'bob.tran@example.com', N'0912345678', N'host', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (3, N'Charlie Le', N'charlie3                                          ', N'hashedpw3', N'charlie.le@example.com', N'0923456789', N'customer', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (4, N'Diana Vu', N'diana4                                            ', N'hashedpw4', N'diana.vu@example.com', N'0934567890', N'customer', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (5, N'Eric Pham', N'eric5                                             ', N'hashedpw5', N'eric.pham@example.com', N'0945678901', N'customer', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (6, N'Fiona Do', N'fiona6                                            ', N'hashedpw6', N'fiona.do@example.com', N'0956789012', N'host', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (7, N'George Nguyen', N'george7                                           ', N'hashedpw7', N'george.nguyen@example.com', N'0967890123', N'admin', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (8, N'Hien Mai', N'hien8                                             ', N'hashedpw8', N'hien.mai@example.com', N'0978901234', N'manager', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (9, N'Ivy Tran', N'ivy9                                              ', N'hashedpw9', N'ivy.tran@example.com', N'0989012345', N'customer', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [username], [password], [email], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (10, N'Jack Le', N'jack10                                            ', N'hashedpw10', N'jack.le@example.com', N'0990123456', N'customer', 1, CAST(N'2025-05-23T10:32:07.203' AS DateTime), CAST(N'2025-05-23T10:32:07.203' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Users] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Amenitie__72E12F1B346A14D8]    Script Date: 05/25/25 04:59:51 PM ******/
ALTER TABLE [dbo].[Amenities] ADD  CONSTRAINT [UQ__Amenitie__72E12F1B346A14D8] UNIQUE NONCLUSTERED 
(
	[name] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Coupons__357D4CF9EA661668]    Script Date: 05/25/25 04:59:51 PM ******/
ALTER TABLE [dbo].[Coupons] ADD UNIQUE NONCLUSTERED 
(
	[code] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__RoomType__72E12F1BC45E7A2B]    Script Date: 05/25/25 04:59:51 PM ******/
ALTER TABLE [dbo].[RoomTypes] ADD UNIQUE NONCLUSTERED 
(
	[name] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Users__AB6E61646E4790FC]    Script Date: 05/25/25 04:59:51 PM ******/
ALTER TABLE [dbo].[Users] ADD  CONSTRAINT [UQ__Users__AB6E61646E4790FC] UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Amenities] ADD  CONSTRAINT [DF__Amenities__is_ac__1AD3FDA4]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[Amenities] ADD  CONSTRAINT [DF__Amenities__creat__1BC821DD]  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Amenities] ADD  CONSTRAINT [DF__Amenities__updat__1CBC4616]  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[Bookings] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Coupons] ADD  DEFAULT (NULL) FOR [usage_limit]
GO
ALTER TABLE [dbo].[Coupons] ADD  DEFAULT ((0)) FOR [used_count]
GO
ALTER TABLE [dbo].[Coupons] ADD  DEFAULT ((0)) FOR [min_total_price]
GO
ALTER TABLE [dbo].[Coupons] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[Coupons] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Messages] ADD  DEFAULT (getdate()) FOR [sent_at]
GO
ALTER TABLE [dbo].[Notifications] ADD  DEFAULT ((0)) FOR [is_global]
GO
ALTER TABLE [dbo].[Notifications] ADD  DEFAULT (getdate()) FOR [sent_at]
GO
ALTER TABLE [dbo].[Payments] ADD  DEFAULT (getdate()) FOR [paid_at]
GO
ALTER TABLE [dbo].[Reports] ADD  DEFAULT ('pending') FOR [status]
GO
ALTER TABLE [dbo].[Reports] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Reviews] ADD  DEFAULT ((1)) FOR [is_public]
GO
ALTER TABLE [dbo].[Reviews] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[RoomImages] ADD  DEFAULT ((0)) FOR [is_cover]
GO
ALTER TABLE [dbo].[Rooms] ADD  CONSTRAINT [DF__Rooms__status__693CA210]  DEFAULT ('active') FOR [status]
GO
ALTER TABLE [dbo].[Rooms] ADD  CONSTRAINT [DF__Rooms__created_a__6B24EA82]  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Rooms] ADD  CONSTRAINT [DF__Rooms__updated_a__6C190EBB]  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[Rooms] ADD  DEFAULT ((1)) FOR [quantity]
GO
ALTER TABLE [dbo].[Users] ADD  CONSTRAINT [DF__Users__is_active__5EBF139D]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[Users] ADD  CONSTRAINT [DF__Users__created_a__5FB337D6]  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Users] ADD  CONSTRAINT [DF__Users__updated_a__60A75C0F]  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD FOREIGN KEY([coupon_id])
REFERENCES [dbo].[Coupons] ([coupon_id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD  CONSTRAINT [FK__Bookings__custom__02FC7413] FOREIGN KEY([customer_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Bookings] CHECK CONSTRAINT [FK__Bookings__custom__02FC7413]
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD  CONSTRAINT [FK__Bookings__room_i__02084FDA] FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([room_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Bookings] CHECK CONSTRAINT [FK__Bookings__room_i__02084FDA]
GO
ALTER TABLE [dbo].[Favorites]  WITH CHECK ADD  CONSTRAINT [FK__Favorites__room___2EDAF651] FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([room_id])
GO
ALTER TABLE [dbo].[Favorites] CHECK CONSTRAINT [FK__Favorites__room___2EDAF651]
GO
ALTER TABLE [dbo].[Favorites]  WITH CHECK ADD  CONSTRAINT [FK__Favorites__user___2DE6D218] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Favorites] CHECK CONSTRAINT [FK__Favorites__user___2DE6D218]
GO
ALTER TABLE [dbo].[Hotels]  WITH CHECK ADD  CONSTRAINT [FK__Hotels__host_id__66603565] FOREIGN KEY([host_id])
REFERENCES [dbo].[Users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Hotels] CHECK CONSTRAINT [FK__Hotels__host_id__66603565]
GO
ALTER TABLE [dbo].[Hotels]  WITH CHECK ADD  CONSTRAINT [FK_Hotels_Locations] FOREIGN KEY([location_id])
REFERENCES [dbo].[Locations] ([location_id])
GO
ALTER TABLE [dbo].[Hotels] CHECK CONSTRAINT [FK_Hotels_Locations]
GO
ALTER TABLE [dbo].[Messages]  WITH CHECK ADD  CONSTRAINT [FK__Messages__receiv__0F624AF8] FOREIGN KEY([receiver_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Messages] CHECK CONSTRAINT [FK__Messages__receiv__0F624AF8]
GO
ALTER TABLE [dbo].[Messages]  WITH CHECK ADD  CONSTRAINT [FK__Messages__sender__0E6E26BF] FOREIGN KEY([sender_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Messages] CHECK CONSTRAINT [FK__Messages__sender__0E6E26BF]
GO
ALTER TABLE [dbo].[Payments]  WITH CHECK ADD FOREIGN KEY([booking_id])
REFERENCES [dbo].[Bookings] ([booking_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Reports]  WITH CHECK ADD  CONSTRAINT [FK__Reports__reporte__2A164134] FOREIGN KEY([reporter_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Reports] CHECK CONSTRAINT [FK__Reports__reporte__2A164134]
GO
ALTER TABLE [dbo].[Reports]  WITH CHECK ADD  CONSTRAINT [FK__Reports__reporte__2B0A656D] FOREIGN KEY([reported_user_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Reports] CHECK CONSTRAINT [FK__Reports__reporte__2B0A656D]
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD FOREIGN KEY([booking_id])
REFERENCES [dbo].[Bookings] ([booking_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD  CONSTRAINT [FK__Reviews__reviewe__0A9D95DB] FOREIGN KEY([reviewer_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Reviews] CHECK CONSTRAINT [FK__Reviews__reviewe__0A9D95DB]
GO
ALTER TABLE [dbo].[RoomAmenities]  WITH CHECK ADD  CONSTRAINT [FK__RoomAmeni__ameni__208CD6FA] FOREIGN KEY([amenity_id])
REFERENCES [dbo].[Amenities] ([amenity_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[RoomAmenities] CHECK CONSTRAINT [FK__RoomAmeni__ameni__208CD6FA]
GO
ALTER TABLE [dbo].[RoomAmenities]  WITH CHECK ADD  CONSTRAINT [FK__RoomAmeni__room___1F98B2C1] FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([room_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[RoomAmenities] CHECK CONSTRAINT [FK__RoomAmeni__room___1F98B2C1]
GO
ALTER TABLE [dbo].[RoomImages]  WITH CHECK ADD  CONSTRAINT [FK__RoomImage__room___70DDC3D8] FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([room_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[RoomImages] CHECK CONSTRAINT [FK__RoomImage__room___70DDC3D8]
GO
ALTER TABLE [dbo].[Rooms]  WITH CHECK ADD  CONSTRAINT [FK__Rooms__hotel_id__6D0D32F4] FOREIGN KEY([hotel_id])
REFERENCES [dbo].[Hotels] ([hotel_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Rooms] CHECK CONSTRAINT [FK__Rooms__hotel_id__6D0D32F4]
GO
ALTER TABLE [dbo].[Rooms]  WITH CHECK ADD  CONSTRAINT [FK_Rooms_RoomTypes] FOREIGN KEY([room_type_id])
REFERENCES [dbo].[RoomTypes] ([room_type_id])
GO
ALTER TABLE [dbo].[Rooms] CHECK CONSTRAINT [FK_Rooms_RoomTypes]
GO
ALTER TABLE [dbo].[UserCoupons]  WITH CHECK ADD FOREIGN KEY([coupon_id])
REFERENCES [dbo].[Coupons] ([coupon_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[UserCoupons]  WITH CHECK ADD  CONSTRAINT [FK__UserCoupo__user___7C4F7684] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[UserCoupons] CHECK CONSTRAINT [FK__UserCoupo__user___7C4F7684]
GO
ALTER TABLE [dbo].[UserNotifications]  WITH CHECK ADD FOREIGN KEY([notification_id])
REFERENCES [dbo].[Notifications] ([notification_id])
GO
ALTER TABLE [dbo].[UserNotifications]  WITH CHECK ADD  CONSTRAINT [FK__UserNotif__user___160F4887] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[UserNotifications] CHECK CONSTRAINT [FK__UserNotif__user___160F4887]
GO
ALTER TABLE [dbo].[UserProfiles]  WITH CHECK ADD  CONSTRAINT [FK__UserProfi__user___6383C8BA] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[UserProfiles] CHECK CONSTRAINT [FK__UserProfi__user___6383C8BA]
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD CHECK  (([status]='cancelled' OR [status]='rejected' OR [status]='approved' OR [status]='pending'))
GO
ALTER TABLE [dbo].[Coupons]  WITH CHECK ADD CHECK  (([type]='fixed' OR [type]='percentage'))
GO
ALTER TABLE [dbo].[Payments]  WITH CHECK ADD CHECK  (([payment_status]='refunded' OR [payment_status]='failed' OR [payment_status]='success' OR [payment_status]='pending'))
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD CHECK  (([rating]>=(1) AND [rating]<=(5)))
GO
ALTER TABLE [dbo].[Rooms]  WITH CHECK ADD  CONSTRAINT [CK__Rooms__status__6A30C649] CHECK  (([status]='ban' OR [status]='booked' OR [status]='inactive' OR [status]='active'))
GO
ALTER TABLE [dbo].[Rooms] CHECK CONSTRAINT [CK__Rooms__status__6A30C649]
GO
ALTER TABLE [dbo].[Users]  WITH CHECK ADD  CONSTRAINT [CK__Users__role__5DCAEF64] CHECK  (([role]='manager' OR [role]='admin' OR [role]='host' OR [role]='customer' OR [role]='guest'))
GO
ALTER TABLE [dbo].[Users] CHECK CONSTRAINT [CK__Users__role__5DCAEF64]
GO
ALTER DATABASE [RoomBooking] SET  READ_WRITE 
GO
