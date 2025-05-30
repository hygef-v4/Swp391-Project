﻿ALTER TABLE RoomTypes
ADD description NVARCHAR(MAX) NULL;
UPDATE RoomTypes SET description = N'Phòng tiêu chuẩn, phù hợp với hầu hết khách du lịch.' WHERE room_type_id = 1;
UPDATE RoomTypes SET description = N'Phòng dành cho 2 người, thiết kế đơn giản và tiện nghi.' WHERE room_type_id = 2;
UPDATE RoomTypes SET description = N'Phòng Suite cao cấp, rộng rãi với tiện nghi đặc biệt.' WHERE room_type_id = 3;
UPDATE RoomTypes SET description = N'Biệt thự riêng biệt, không gian rộng và sang trọng.' WHERE room_type_id = 4;
UPDATE RoomTypes SET description = N'Phòng lớn phù hợp cho gia đình với nhiều tiện ích.' WHERE room_type_id = 5;
UPDATE RoomTypes SET description = N'Phòng nhỏ gọn như cabin, tiết kiệm và tiện lợi.' WHERE room_type_id = 6;
UPDATE RoomTypes SET description = N'Phòng sang trọng với nội thất cao cấp.' WHERE room_type_id = 7;
UPDATE RoomTypes SET description = N'Phòng gần thiên nhiên, có vườn bao quanh.' WHERE room_type_id = 8;
UPDATE RoomTypes SET description = N'Phòng phong cách cổ điển, nội thất mang nét xưa.' WHERE room_type_id = 9;
UPDATE RoomTypes SET description = N'Phòng VIP với đầy đủ dịch vụ cao cấp.' WHERE room_type_id = 10;


CREATE TABLE AmenityCategories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) UNIQUE NOT NULL
);
go
INSERT INTO AmenityCategories (name)
SELECT DISTINCT category
FROM Amenities
WHERE category IS NOT NULL;
go
ALTER TABLE Amenities
ADD category_id INT;
go
UPDATE a
SET category_id = ac.category_id
FROM Amenities a
JOIN AmenityCategories ac ON a.category = ac.name;
go
ALTER TABLE Amenities
DROP COLUMN category;
go
ALTER TABLE Amenities
ADD CONSTRAINT FK_Amenities_Categories
FOREIGN KEY (category_id) REFERENCES AmenityCategories(category_id);
