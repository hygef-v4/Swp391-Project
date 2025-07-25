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

    public String backGround(){
        switch (bankCode) {
            case "CIMB":
            case "COOPB":
            case "HLB":
            case "KLB":
            case "MBV":
            case "MSB":
            case "SEAB":
            case "TCB":
            case "TIMO":
            case "TPB":
            case "VARB":
            case "VRB":
            case "VTLMONEY":
                return "bg-danger";                
        
            case "BAB":
            case "IVB":
            case "KB":
            case "LPB":
            case "NAB":
            case "PVCB":
            case "SHB":
                return "bg-warning";

            case "ACB":
            case "BVB":
            case "CBB":
            case "DAB":
            case "EIB":
            case "GPB":
            case "HDB":
            case "MB":
            case "NCB":
            case "PGB":
            case "SCB":
            case "SCBVN":
            case "SGB":
            case "SGCB":
            case "SHBVN":
            case "UOB":
            case "VAB":
            case "VCCB":
            case "VIB":
            case "VTB":
            case "WOO":
                return "bg-info";

            case "ABB":
            case "BIDV":
            case "OCB":
            case "PBVN":
            case "VB":
            case "VCB":
            case "VPB":
                return "bg-success";
            
            default:
                return "bg-secondary";
        }
    }
}
