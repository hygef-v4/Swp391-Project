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

    private String bankNumber;
    private String userName;
    private boolean defaultAccount;

    public String maskedCode(){
        return "*".repeat(bankNumber.length()-4) + bankNumber.substring(bankNumber.length()-4);
    }
}
