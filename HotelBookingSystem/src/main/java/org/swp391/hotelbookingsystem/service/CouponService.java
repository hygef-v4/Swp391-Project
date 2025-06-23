// --- CouponService.java
package org.swp391.hotelbookingsystem.service;

import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.Coupon;
import org.swp391.hotelbookingsystem.repository.CouponRepository;

import java.util.List;

@Service
public class CouponService {
    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.getAllCoupons();
    }

    public void createCoupon(Coupon coupon) {
        couponRepository.insertCoupon(coupon);
    }

    public void updateCoupon(Coupon coupon) {
        couponRepository.updateCoupon(coupon);
    }

    public void deleteCoupon(int id) {
        couponRepository.deleteCoupon(id);
    }

    public Coupon getCouponById(int id) {
        return couponRepository.getCouponById(id);
    }
}