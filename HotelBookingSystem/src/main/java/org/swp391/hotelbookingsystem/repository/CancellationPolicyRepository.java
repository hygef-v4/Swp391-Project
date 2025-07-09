package org.swp391.hotelbookingsystem.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.CancellationPolicy;

@Repository
public class CancellationPolicyRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<CancellationPolicy> POLICY_MAPPER = 
            new BeanPropertyRowMapper<>(CancellationPolicy.class);

    @Autowired
    public CancellationPolicyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertCancellationPolicy(CancellationPolicy policy) {
        String sql = """
                INSERT INTO CancellationPolicies (hotel_id, partial_refund_days, partial_refund_percent, no_refund_within_days)
                VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, 
                policy.getHotelId(),
                policy.getPartialRefundDays(),
                policy.getPartialRefundPercent(),
                policy.getNoRefundWithinDays()
        );
    }

    public CancellationPolicy findByHotelId(int hotelId) {
        String sql = """
                SELECT hotel_id AS hotelId,
                       partial_refund_days AS partialRefundDays,
                       partial_refund_percent AS partialRefundPercent,
                       no_refund_within_days AS noRefundWithinDays
                FROM CancellationPolicies
                WHERE hotel_id = ?
                """;
        try {
            return jdbcTemplate.queryForObject(sql, POLICY_MAPPER, hotelId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void updateCancellationPolicy(CancellationPolicy policy) {
        String sql = """
                UPDATE CancellationPolicies 
                SET partial_refund_days = ?, 
                    partial_refund_percent = ?, 
                    no_refund_within_days = ?
                WHERE hotel_id = ?
                """;
        jdbcTemplate.update(sql,
                policy.getPartialRefundDays(),
                policy.getPartialRefundPercent(),
                policy.getNoRefundWithinDays(),
                policy.getHotelId()
        );
    }

    public void deleteCancellationPolicy(int hotelId) {
        String sql = "DELETE FROM CancellationPolicies WHERE hotel_id = ?";
        jdbcTemplate.update(sql, hotelId);
    }
} 