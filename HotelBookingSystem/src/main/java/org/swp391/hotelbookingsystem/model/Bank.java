package org.swp391.hotelbookingsystem.model;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bank {
    private int bankId;
    private String bankName;
    private String bankCode;
    private String logo;
    private String icon;
    private boolean defaultAccount;
}
