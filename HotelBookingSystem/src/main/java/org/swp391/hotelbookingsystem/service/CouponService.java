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

    public boolean checkCoupon(Integer couponId){
        if(couponId == null) return false;
        return couponRepository.checkCoupon(couponId);
    }

    public void applyCoupon(int couponId, int userId) {
        couponRepository.applyCoupon(couponId, userId);
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

    public Coupon searchCouponsByCode(String code) {
        try{
            if(code == null) return null;
            return couponRepository.searchCouponsByCode(code);
        }catch(Exception e){
            return null;
        }
    }

    public boolean isCouponCodeExists(String code) {
        return couponRepository.existsByCode(code);
    }

    public List<Coupon> getFilteredCoupons(Boolean status, String search) {
        return couponRepository.getFilteredCoupons(status, search);
    }

    public void deactivateExpiredCoupons() {
        couponRepository.deactivateExpiredCoupons();
    }

    public void deactivateUsedUpCoupons() {
        List<Coupon> allCoupons = couponRepository.getAllCoupons();
        for (Coupon coupon : allCoupons) {
            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit() && coupon.isActive()) {
                coupon.setActive(false);
                couponRepository.updateCoupon(coupon); // or a lightweight status-only update query
            }
        }
    }

}