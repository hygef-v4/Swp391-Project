package org.swp391.hotelbookingsystem.model;

import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Coupon {
    private int couponId;
    private String code;
    private String type; // 'percentage' or 'fixed'
    private double amount;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Integer usageLimit;
    private int usedCount;
    private boolean isActive;
    private double minTotalPrice;
}
