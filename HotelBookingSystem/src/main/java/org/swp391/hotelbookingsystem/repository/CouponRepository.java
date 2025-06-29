// --- Updated CouponRepository.java ---
package org.swp391.hotelbookingsystem.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.swp391.hotelbookingsystem.model.Coupon;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CouponRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<Coupon> COUPON_MAPPER = new BeanPropertyRowMapper<>(Coupon.class);

    public CouponRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Coupon> getAllCoupons() {
        String sql = "SELECT * FROM Coupons ORDER BY coupon_id DESC";
        return jdbcTemplate.query(sql, COUPON_MAPPER);
    }

    public void insertCoupon(Coupon coupon) {
        String sql = """
                INSERT INTO Coupons (code, type, amount, valid_from, valid_until, usage_limit, used_count, is_active, min_total_price)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""";
        jdbcTemplate.update(sql,
                coupon.getCode(),
                coupon.getType(),
                coupon.getAmount(),
                Date.valueOf(coupon.getValidFrom()),
                Date.valueOf(coupon.getValidUntil()),
                coupon.getUsageLimit(),
                coupon.getUsedCount(),
                coupon.isActive(),
                coupon.getMinTotalPrice()
        );
    }

    public void updateCoupon(Coupon coupon) {
        String sql = """
                UPDATE Coupons
                SET code = ?, type = ?, amount = ?, valid_from = ?, valid_until = ?, usage_limit = ?, used_count = ?, is_active = ?, min_total_price = ?
                WHERE coupon_id = ?""";
        jdbcTemplate.update(sql,
                coupon.getCode(),
                coupon.getType(),
                coupon.getAmount(),
                Date.valueOf(coupon.getValidFrom()),
                Date.valueOf(coupon.getValidUntil()),
                coupon.getUsageLimit(),
                coupon.getUsedCount(),
                coupon.isActive(),
                coupon.getMinTotalPrice(),
                coupon.getCouponId()
        );
    }

    public void deleteCoupon(int id) {
        String sql = "DELETE FROM Coupons WHERE coupon_id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Coupon getCouponById(int id) {
        String sql = "SELECT * FROM Coupons WHERE coupon_id = ?";
        return jdbcTemplate.queryForObject(sql, COUPON_MAPPER, id);
    }

    public List<Coupon> searchCouponsByCode(String code) {
        String sql = "SELECT * FROM Coupons WHERE code LIKE ? ORDER BY coupon_id DESC";
        return jdbcTemplate.query(sql, COUPON_MAPPER, "%" + code + "%");
    }

    public boolean existsByCode(String code) {
        String sql = "SELECT COUNT(*) FROM Coupons WHERE code = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code);
        return count != null && count > 0;
    }

    public List<Coupon> getFilteredCoupons(Boolean isActive, String search) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Coupons WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (isActive != null) {
            if (isActive) {
                sql.append(" AND is_active = 1 AND valid_until >= CONVERT(date, GETDATE())");
            } else {
                sql.append(" AND (is_active = 0 OR valid_until < CONVERT(date, GETDATE()))");
            }
        }


        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND code LIKE ?");
            params.add("%" + search.trim() + "%");
        }

        sql.append(" ORDER BY coupon_id DESC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), COUPON_MAPPER);
    }

    public void deactivateExpiredCoupons() {
        String sql = "UPDATE Coupons SET is_active = 0 WHERE valid_until < CONVERT(date, GETDATE())";
        jdbcTemplate.update(sql);
    }



}
