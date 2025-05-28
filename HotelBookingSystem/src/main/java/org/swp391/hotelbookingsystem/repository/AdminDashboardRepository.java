package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Hotel;

import java.util.*;

@Repository
public class AdminDashboardRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Hotel> HOTEL_MAPPER = new BeanPropertyRowMapper<>(Hotel.class);

    // SQL Queries
    private static final String COUNT_HOTEL = """
            select count(hotel_id) as numberOfHotels
            from Hotels
            """;

    private static final String COUNT_ROOMS = "SELECT SUM(quantity) FROM Rooms";

    private static final String COUNT_BOOKED_ROOMS = """
            SELECT COUNT(*)
            FROM bookings
            WHERE status = 'approved'
              AND check_in <= GETDATE()
              AND check_out > GETDATE()
            """;

    private static final String TOP_POPULAR_HOTELS = """
                SELECT h.hotel_id, h.hotel_name, h.address, h.hotel_image_url, 
                       MIN(r.price) AS minPrice
                FROM Hotels h
                JOIN Rooms r ON h.hotel_id = r.hotel_id
                GROUP BY h.hotel_id, h.hotel_name, h.address, h.hotel_image_url
                ORDER BY minPrice DESC
                OFFSET 0 ROWS FETCH NEXT 4 ROWS ONLY
            """;

    private static final List<String> WEEKDAYS = List.of("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");

    public List<Integer> getCheckInCountsByWeekday() {
        String sql = """
            SELECT DATENAME(WEEKDAY, check_in) AS day_name, COUNT(*) AS count
            FROM bookings
            WHERE status = 'approved'
            GROUP BY DATENAME(WEEKDAY, check_in)
        """;

        Map<String, Integer> resultMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            resultMap.put(rs.getString("day_name"), rs.getInt("count"));
        });

        List<Integer> result = new ArrayList<>();
        for (String day : WEEKDAYS) {
            result.add(resultMap.getOrDefault(day, 0));
        }
        return result;
    }

    public List<Integer> getCheckOutCountsByWeekday() {
        String sql = """
            SELECT DATENAME(WEEKDAY, check_out) AS day_name, COUNT(*) AS count
            FROM bookings
            WHERE status = 'approved'
            GROUP BY DATENAME(WEEKDAY, check_out)
        """;

        Map<String, Integer> resultMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            resultMap.put(rs.getString("day_name"), rs.getInt("count"));
        });

        List<Integer> result = new ArrayList<>();
        for (String day : WEEKDAYS) {
            result.add(resultMap.getOrDefault(day, 0));
        }
        return result;
    }

    // Methods
    public int getNumberOfHotels() {
        return jdbcTemplate.queryForObject(COUNT_HOTEL, Integer.class);
    }

    public int getTotalRooms() {
        Integer count = jdbcTemplate.queryForObject(COUNT_ROOMS, Integer.class);
        return count != null ? count : 0;
    }

    public int getTotalBookedRooms() {
        Integer count = jdbcTemplate.queryForObject(COUNT_BOOKED_ROOMS, Integer.class);
        return count != null ? count : 0;
    }
    public List<Hotel> getTopPopularHotels() {
        return jdbcTemplate.query(TOP_POPULAR_HOTELS, HOTEL_MAPPER);
    }
}
