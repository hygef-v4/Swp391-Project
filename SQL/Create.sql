USE [master]
GO
/****** Object:  Database [RoomBooking]    Script Date: 5/22/2025 11:45:05 PM ******/
CREATE DATABASE [RoomBooking]
GO
CREATE TABLE [dbo].[Amenities](
	[amenity_id] [int] IDENTITY(1,1) NOT NULL,
	[name] [varchar](100) NOT NULL,
	[category] [varchar](50) NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[amenity_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Bookings]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Coupons]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Favorites]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Hotels]    Script Date: 5/22/2025 11:45:05 PM ******/
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
	[total_room] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[hotel_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Messages]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Notifications]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Payments]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Reports]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Reviews]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomAmenities]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomImages]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Rooms]    Script Date: 5/22/2025 11:45:05 PM ******/
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
PRIMARY KEY CLUSTERED 
(
	[room_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserCoupons]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserNotifications]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserProfiles]    Script Date: 5/22/2025 11:45:05 PM ******/
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Users]    Script Date: 5/22/2025 11:45:05 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Users](
	[user_id] [int] IDENTITY(1,1) NOT NULL,
	[full_name] [nvarchar](100) NOT NULL,
	[email] [nvarchar](100) NOT NULL,
	[password_hash] [nvarchar](255) NOT NULL,
	[phone] [nvarchar](20) NULL,
	[role] [nvarchar](20) NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[Hotels] ON 
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [total_room]) VALUES (1, 2, N'Sunrise Hotel', N'123 Beach Road, Da Nang', N'Beautiful hotel with sea view', 10)
GO
INSERT [dbo].[Hotels] ([hotel_id], [host_id], [hotel_name], [address], [description], [total_room]) VALUES (2, 2, N'Mountain View Inn', N'456 Hill Street, Sapa', N'Quiet and cozy in the mountains', 5)
GO
SET IDENTITY_INSERT [dbo].[Hotels] OFF
GO
SET IDENTITY_INSERT [dbo].[Rooms] ON 
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at]) VALUES (1, 1, N'Deluxe Room', N'Spacious room with balcony', CAST(120.00 AS Decimal(10, 2)), 2, N'active', CAST(N'2025-05-22T23:35:45.000' AS DateTime), CAST(N'2025-05-22T23:35:45.000' AS DateTime))
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at]) VALUES (2, 1, N'Standard Room', N'Affordable and comfortable', CAST(80.00 AS Decimal(10, 2)), 2, N'active', CAST(N'2025-05-22T23:35:45.000' AS DateTime), CAST(N'2025-05-22T23:35:45.000' AS DateTime))
GO
INSERT [dbo].[Rooms] ([room_id], [hotel_id], [title], [description], [price], [max_guests], [status], [created_at], [updated_at]) VALUES (3, 2, N'Suite Room', N'Luxury room with mountain view', CAST(200.00 AS Decimal(10, 2)), 3, N'active', CAST(N'2025-05-22T23:35:45.000' AS DateTime), CAST(N'2025-05-22T23:35:45.000' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Rooms] OFF
GO
SET IDENTITY_INSERT [dbo].[Users] ON 
GO
INSERT [dbo].[Users] ([user_id], [full_name], [email], [password_hash], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (1, N'Alice Nguyen', N'alice@example.com', N'hashedpw1', N'0123456789', N'customer', 1, CAST(N'2025-05-22T23:35:44.987' AS DateTime), CAST(N'2025-05-22T23:35:44.987' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [email], [password_hash], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (2, N'Bob Tran', N'bob@example.com', N'hashedpw2', N'0987654321', N'host', 1, CAST(N'2025-05-22T23:35:44.987' AS DateTime), CAST(N'2025-05-22T23:35:44.987' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [email], [password_hash], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (3, N'Charlie Le', N'charlie@example.com', N'hashedpw3', N'0111222333', N'guest', 1, CAST(N'2025-05-22T23:35:44.987' AS DateTime), CAST(N'2025-05-22T23:35:44.987' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [email], [password_hash], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (4, N'Admin Vu', N'admin@example.com', N'hashedpw4', N'0223344556', N'admin', 1, CAST(N'2025-05-22T23:35:44.987' AS DateTime), CAST(N'2025-05-22T23:35:44.987' AS DateTime))
GO
INSERT [dbo].[Users] ([user_id], [full_name], [email], [password_hash], [phone], [role], [is_active], [created_at], [updated_at]) VALUES (5, N'Manager Kim', N'manager@example.com', N'hashedpw5', N'0334455667', N'manager', 1, CAST(N'2025-05-22T23:35:44.987' AS DateTime), CAST(N'2025-05-22T23:35:44.987' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[Users] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Amenitie__72E12F1B26CB5EB8]    Script Date: 5/22/2025 11:45:05 PM ******/
ALTER TABLE [dbo].[Amenities] ADD UNIQUE NONCLUSTERED 
(
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Coupons__357D4CF978752238]    Script Date: 5/22/2025 11:45:05 PM ******/
ALTER TABLE [dbo].[Coupons] ADD UNIQUE NONCLUSTERED 
(
	[code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Users__AB6E616474B9845B]    Script Date: 5/22/2025 11:45:05 PM ******/
ALTER TABLE [dbo].[Users] ADD UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Amenities] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[Amenities] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Amenities] ADD  DEFAULT (getdate()) FOR [updated_at]
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
ALTER TABLE [dbo].[Rooms] ADD  DEFAULT ('active') FOR [status]
GO
ALTER TABLE [dbo].[Rooms] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Rooms] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD FOREIGN KEY([coupon_id])
REFERENCES [dbo].[Coupons] ([coupon_id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD FOREIGN KEY([customer_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([room_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Favorites]  WITH CHECK ADD FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([room_id])
GO
ALTER TABLE [dbo].[Favorites]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Hotels]  WITH CHECK ADD FOREIGN KEY([host_id])
REFERENCES [dbo].[Users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Messages]  WITH CHECK ADD FOREIGN KEY([receiver_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Messages]  WITH CHECK ADD FOREIGN KEY([sender_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Payments]  WITH CHECK ADD FOREIGN KEY([booking_id])
REFERENCES [dbo].[Bookings] ([booking_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Reports]  WITH CHECK ADD FOREIGN KEY([reporter_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Reports]  WITH CHECK ADD FOREIGN KEY([reported_user_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD FOREIGN KEY([booking_id])
REFERENCES [dbo].[Bookings] ([booking_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD FOREIGN KEY([reviewer_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[RoomAmenities]  WITH CHECK ADD FOREIGN KEY([amenity_id])
REFERENCES [dbo].[Amenities] ([amenity_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[RoomAmenities]  WITH CHECK ADD FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([room_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[RoomImages]  WITH CHECK ADD FOREIGN KEY([room_id])
REFERENCES [dbo].[Rooms] ([room_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Rooms]  WITH CHECK ADD FOREIGN KEY([hotel_id])
REFERENCES [dbo].[Hotels] ([hotel_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[UserCoupons]  WITH CHECK ADD FOREIGN KEY([coupon_id])
REFERENCES [dbo].[Coupons] ([coupon_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[UserCoupons]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[UserNotifications]  WITH CHECK ADD FOREIGN KEY([notification_id])
REFERENCES [dbo].[Notifications] ([notification_id])
GO
ALTER TABLE [dbo].[UserNotifications]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[UserProfiles]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Bookings]  WITH CHECK ADD CHECK  (([status]='cancelled' OR [status]='rejected' OR [status]='approved' OR [status]='pending'))
GO
ALTER TABLE [dbo].[Coupons]  WITH CHECK ADD CHECK  (([type]='fixed' OR [type]='percentage'))
GO
ALTER TABLE [dbo].[Payments]  WITH CHECK ADD CHECK  (([payment_status]='refunded' OR [payment_status]='failed' OR [payment_status]='success' OR [payment_status]='pending'))
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD CHECK  (([rating]>=(1) AND [rating]<=(5)))
GO
ALTER TABLE [dbo].[Rooms]  WITH CHECK ADD CHECK  (([status]='ban' OR [status]='booked' OR [status]='inactive' OR [status]='active'))
GO
ALTER TABLE [dbo].[Users]  WITH CHECK ADD CHECK  (([role]='manager' OR [role]='admin' OR [role]='host' OR [role]='customer' OR [role]='guest'))
GO
USE [master]
GO
ALTER DATABASE [RoomBooking] SET  READ_WRITE 
GO
