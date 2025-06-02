package org.swp391.hotelbookingsystem.repository;

import org.swp391.hotelbookingsystem.model.Favorites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Hotel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserWishlistRepository {
    @Autowired
    private JdbcTemplate jdbc;

    public List<Favorites> findFavoritesByUserId(int userId) {
        String sql = """
            SELECT f.user_id, f.hotel_id, 
                   h.hotel_name, h.address, h.hotel_image_url
            FROM favorites f
            INNER JOIN hotels h ON f.hotel_id = h.hotel_id
            WHERE f.user_id = ?;
        """;

        return jdbc.query(sql, (rs, rowNum) -> mapFavoritesWithHotel(rs), userId);
    }

    private Favorites mapFavoritesWithHotel(ResultSet rs) throws SQLException {
        Hotel hotel = Hotel.builder()
                .hotelId(rs.getInt("hotel_id"))
                .hotelName(rs.getString("hotel_name"))
                .address(rs.getString("address"))
                .hotelImageUrl(rs.getString("hotel_image_url"))
                .build();

        return Favorites.builder()
                .userId(rs.getInt("user_id"))
                .hotelId(rs.getInt("hotel_id"))
                .hotel(hotel)
                .build();
    }

}
