package org.swp391.hotelbookingsystem.model;

import lombok.*;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Favorites {
    private int userId;
    private int hotelId;
    private Hotel hotel; // Thêm quan hệ với Hotel để lưu thông tin khách sạn
}