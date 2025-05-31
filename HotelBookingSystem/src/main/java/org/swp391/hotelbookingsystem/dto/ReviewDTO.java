package org.swp391.hotelbookingsystem.dto;

import lombok.*;

@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewDTO {
    public Long id;
    public String roomName;
    public String imageUrl;
    public int starCount;
    public String starsHtml;
}