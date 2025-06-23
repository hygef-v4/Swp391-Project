// --- AdminCouponController.java
package org.swp391.hotelbookingsystem.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.swp391.hotelbookingsystem.model.Coupon;
import org.swp391.hotelbookingsystem.service.CouponService;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/admin/coupons")
    public String viewCoupons(Model model, HttpSession session) {
        List<Coupon> coupons = couponService.getAllCoupons();
        model.addAttribute("couponList", coupons);
        return "admin/admin-coupon";
    }

    @PostMapping("/admin/coupons/create")
    public String createCoupon(@ModelAttribute Coupon coupon, RedirectAttributes redirectAttributes) {
        try {
            couponService.createCoupon(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/admin/coupons/update")
    public String updateCoupon(@ModelAttribute Coupon coupon, RedirectAttributes redirectAttributes) {
        try {
            couponService.updateCoupon(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/admin/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            couponService.deleteCoupon(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa mã giảm giá.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }
}
