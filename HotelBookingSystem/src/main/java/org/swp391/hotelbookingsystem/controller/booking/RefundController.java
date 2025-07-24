package org.swp391.hotelbookingsystem.controller.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.swp391.hotelbookingsystem.config.VNPayConfig;
import org.swp391.hotelbookingsystem.model.Booking;
import org.swp391.hotelbookingsystem.service.BookingService;
import org.swp391.hotelbookingsystem.service.VNPayService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;


@RestController
public class RefundController{
    @Autowired
    BookingService bookingService;
    @Autowired
    VNPayService vnpayService;

    @PostMapping("/refund")
    public String refund(
        @RequestParam("id") int id, 
        
        @RequestParam("trantype") String trantype,
        @RequestParam("amount") long amount,
        @RequestParam("refundRole") String refundRole,
        @RequestParam("orderInfo") String orderInfo,
        HttpServletRequest request){
        try{
            Booking booking = bookingService.findById(id);
            if(booking.refundAmount() == 0) return "99";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String refundBody = vnpayService.refundPayment(trantype, booking.getOrderCode(), amount, booking.getTransactionNo(), booking.getCreatedAt(), refundRole, orderInfo, request);
            HttpEntity<String> http = new HttpEntity<>(refundBody, headers);

            String apiUrl = VNPayConfig.vnp_ApiUrl;
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, http, String.class);
            System.out.println(response. getBody());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());
            String responseCode = json.get("vnp_ResponseCode").asText();

            return responseCode;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
}
