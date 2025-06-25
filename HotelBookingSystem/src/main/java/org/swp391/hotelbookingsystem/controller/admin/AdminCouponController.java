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
    public String viewCoupons(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "status", required = false) Boolean status,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              Model model) {
        couponService.deactivateUsedUpCoupons();
        couponService.deactivateExpiredCoupons();
        String trimmedSearch = (search != null) ? search.trim().replaceAll("\\s+", " ") : null;

        List<Coupon> filteredCoupons = couponService.getFilteredCoupons(status, trimmedSearch);

        int pageSize = 10;
        int totalCoupons = filteredCoupons.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCoupons);
        List<Coupon> currentCoupons = (startIndex < totalCoupons) ? filteredCoupons.subList(startIndex, endIndex) : List.of();

        model.addAttribute("search", trimmedSearch);
        model.addAttribute("statusFilter", status);
        model.addAttribute("coupons", currentCoupons);
        model.addAttribute("page", page);
        model.addAttribute("pagination", (int) Math.ceil((double) totalCoupons / pageSize));
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalCoupons", totalCoupons);

        return "admin/admin-coupon";
    }


    @PostMapping("/admin/coupons/create")
    public String createCoupon(@ModelAttribute Coupon coupon, RedirectAttributes redirectAttributes) {
        // Validation
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã giảm giá không được để trống.");
            return "redirect:/admin/coupons";
        }
        if (!"percentage".equals(coupon.getType()) && !"fixed".equals(coupon.getType())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loại mã giảm giá không hợp lệ.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getAmount() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giá trị mã giảm giá phải lớn hơn 0.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getValidFrom() != null && coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(coupon.getValidFrom())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ngày kết thúc phải sau ngày bắt đầu.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giới hạn sử dụng phải lớn hơn hoặc bằng 0.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getMinTotalPrice() < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tối thiểu tổng giá phải lớn hơn hoặc bằng 0.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getValidFrom() != null && (coupon.getValidFrom().getYear() < 2000 || coupon.getValidFrom().getYear() > 2100)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Năm bắt đầu phải nằm trong khoảng 2000-2100.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getValidUntil() != null && (coupon.getValidUntil().getYear() < 2000 || coupon.getValidUntil().getYear() > 2100)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Năm kết thúc phải nằm trong khoảng 2000-2100.");
            return "redirect:/admin/coupons";
        }
        if (couponService.isCouponCodeExists(coupon.getCode())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã giảm giá đã tồn tại. Vui lòng chọn mã khác.");
            return "redirect:/admin/coupons";
        }

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
        // Prevent editing outdated coupon
        Coupon existing = couponService.getCouponById(coupon.getCouponId());
        if (existing != null && existing.getValidUntil() != null &&
                existing.getValidUntil().isBefore(LocalDate.now()) &&
                existing.getUsedCount() > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể chỉnh sửa mã đã hết hạn và đã được sử dụng.");
            return "redirect:/admin/coupons";
        }
        // Validation (reuse from create)
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã giảm giá không được để trống.");
            return "redirect:/admin/coupons";
        }
        if (!"percentage".equals(coupon.getType()) && !"fixed".equals(coupon.getType())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loại mã giảm giá không hợp lệ.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getAmount() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giá trị mã giảm giá phải lớn hơn 0.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getValidFrom() != null && coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(coupon.getValidFrom())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ngày kết thúc phải sau ngày bắt đầu.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giới hạn sử dụng phải lớn hơn hoặc bằng 0.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getMinTotalPrice() < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tối thiểu tổng giá phải lớn hơn hoặc bằng 0.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getValidFrom() != null && (coupon.getValidFrom().getYear() < 2000 || coupon.getValidFrom().getYear() > 2100)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Năm bắt đầu phải nằm trong khoảng 2000-2100.");
            return "redirect:/admin/coupons";
        }
        if (coupon.getValidUntil() != null && (coupon.getValidUntil().getYear() < 2000 || coupon.getValidUntil().getYear() > 2100)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Năm kết thúc phải nằm trong khoảng 2000-2100.");
            return "redirect:/admin/coupons";
        }
        try {
            couponService.updateCoupon(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @GetMapping("/admin/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        // Prevent deleting outdated coupon
        Coupon existing = couponService.getCouponById(id);
        if (existing != null && existing.getValidUntil() != null &&
                existing.getValidUntil().isBefore(LocalDate.now()) &&
                existing.getUsedCount() > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa mã giảm giá đã hết hạn và đã được sử dụng.");
            return "redirect:/admin/coupons";
        }
        try {
            couponService.deleteCoupon(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa mã giảm giá.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @GetMapping("/admin/coupons/{id}")
    @ResponseBody
    public Coupon getCouponById(@PathVariable("id") int id) {
        return couponService.getCouponById(id);
    }
}
