 package org.swp391.hotelbookingsystem.controller.chatbot;

 import org.swp391.hotelbookingsystem.service.GeminiService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RestController;

 import java.util.Map;

 @RestController
 public class DialogflowWebhookController {

     @Autowired
     private GeminiService geminiService;

     @PostMapping("/webhook")
     public Map<String, Object> handleDialogflow(@RequestBody Map<String, Object> payload) {
         try {
             String userMessage = ((Map<String, Object>) payload.get("queryResult")).get("queryText").toString();
             String response = geminiService.generateReply(userMessage);
             return Map.of("fulfillmentText", response);
         } catch (Exception e) {
             return Map.of("fulfillmentText", "Đã xảy ra lỗi khi xử lý webhook.");
         }
     }

 }
