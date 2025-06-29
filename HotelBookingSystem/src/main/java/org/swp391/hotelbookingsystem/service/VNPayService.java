package org.swp391.hotelbookingsystem.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.config.VNPayConfig;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class VNPayService {
    @Autowired
    VNPayConfig vnPayConfig;

    public String createPayment(long total, String orderInfo, String url, HttpServletRequest request) throws UnsupportedEncodingException{
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        String version = "2.1.0";
        String command = "pay";
        String tmnCode = VNPayConfig.vnp_TmnCode;
        String amount = String.valueOf(total * 100);
        String currCode = "VND";
        String bankCode = "VNBANK";
        String txnRef = VNPayConfig.getRandomNumber(8);
        String orderType = "other";
        String locale = "vn";
        String returnUrl = vnPayConfig.vnp_ReturnUrl + url;
        String ipAddr = VNPayConfig.getIpAddress(request);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 10);
        String expireDate = formatter.format(cld.getTime());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", amount);
        params.put("vnp_CurrCode", currCode);
        params.put("vnp_BankCode", bankCode);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", orderType);
        params.put("vnp_Locale", locale);
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        List fieldNames = new ArrayList(params.keySet());
        Collections.sort(fieldNames);
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String payUrl = VNPayConfig.vnp_PayUrl;
        String secretKey = VNPayConfig.vnp_HashSecret;
        String secureHash = VNPayConfig.hmacSHA512(secretKey, hashData.toString());

        query.append("&vnp_SecureHash=");
        query.append(secureHash);
        return payUrl + "?" + query;
    }

    public boolean returnPayment(HttpServletRequest request) throws UnsupportedEncodingException{
        Map<String, String> fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldKey = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(request.getParameter(fieldKey), StandardCharsets.US_ASCII.toString());
            if ((fieldKey.startsWith("vnp_")) && (fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldKey, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        String signValue = VNPayConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                return true;
            }
        } return false;
    }
}
