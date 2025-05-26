USE master

IF EXISTS (SELECT NAME FROM sys.databases WHERE NAME = 'RoomBooking')
BEGIN
	ALTER DATABASE RoomBooking SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
	DROP DATABASE RoomBooking;
END
GO

CREATE DATABASE hi
GO

USE RoomBooking

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
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ__Amenitie__72E12F1B346A14D8] UNIQUE NONCLUSTERED 
(
	[name] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Bookings]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Coupons]    Script Date: 05/26/25 10:16:01 PM ******/
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
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[code] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Favorites]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Hotels]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Locations]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Messages]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Notifications]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Payments]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Reports]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Reviews]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[RoomAmenities]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[RoomImages]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Rooms]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[RoomTypes]    Script Date: 05/26/25 10:16:01 PM ******/
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
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[name] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserCoupons]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[UserNotifications]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[UserProfiles]    Script Date: 05/26/25 10:16:01 PM ******/
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
/****** Object:  Table [dbo].[Users]    Script Date: 05/26/25 10:16:01 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Users](
	[user_id] [int] IDENTITY(1,1) NOT NULL,
	[full_name] [nvarchar](100) NOT NULL,
	[password_hash] [nvarchar](255) NOT NULL,
	[email] [nvarchar](100) NOT NULL,
	[phone] [nvarchar](20) NOT NULL,
	[role] [nvarchar](20) NULL,
	[is_active] [bit] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
 CONSTRAINT [PK__Users__B9BE370FBC933F6B] PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ__Users__AB6E61646E4790FC] UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
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
ALTER TABLE [dbo].[Users] ADD  CONSTRAINT [DF_Users_full_name]  DEFAULT ('') FOR [full_name]
GO
ALTER TABLE [dbo].[Users] ADD  CONSTRAINT [DF_Users_phone]  DEFAULT ('') FOR [phone]
GO
ALTER TABLE [dbo].[Users] ADD  CONSTRAINT [DF_Users_Role]  DEFAULT ('CUSTOMER') FOR [role]
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
