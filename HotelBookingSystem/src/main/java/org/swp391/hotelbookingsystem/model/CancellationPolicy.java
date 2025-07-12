package org.swp391.hotelbookingsystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancellationPolicy {
    private int hotelId;
    private int partialRefundDays;
    private int partialRefundPercent;
    private int noRefundWithinDays;
} 