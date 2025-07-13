 package org.swp391.hotelbookingsystem.controller.chatbot;

 import org.swp391.hotelbookingsystem.service.GeminiService;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RestController;

 import java.util.List;
 import java.util.Map;

 @RestController
 public class DialogflowWebhookController {

     private final GeminiService geminiService;

     public DialogflowWebhookController(GeminiService geminiService) {
         this.geminiService = geminiService;
     }

     @PostMapping("/webhook")
     public Map<String, Object> handleDialogflow(@RequestBody Map<String, Object> payload) {
//         try {
//             String userMessage = ((Map<String, Object>) payload.get("queryResult")).get("queryText").toString();
//             String response = geminiService.generateReply(userMessage);
//             return Map.of("fulfillmentText", response);
//         } catch (Exception e) {
//             return Map.of("fulfillmentText", "Đã xảy ra lỗi khi xử lý webhook.");
//         }
         try {
             System.out.println("=== Webhook Received ===");
             System.out.println("Payload: " + payload);

             Map<String, Object> queryResult = (Map<String, Object>) payload.get("queryResult");
             String intentName = ((Map<String, Object>) queryResult.get("intent")).get("displayName").toString();
             Map<String, Object> parameters = (Map<String, Object>) queryResult.get("parameters");

             System.out.println("Intent: " + intentName);
             System.out.println("Parameters: " + parameters);

             if ("Default Fallback Intent".equals(intentName) || parameters.isEmpty() || !parameters.containsKey("location")) {
                 return Map.of("fulfillmentText", "Xin lỗi, bạn có thể nói rõ địa điểm bạn muốn đặt phòng không?");
             }

             // Trích thông tin
             String location = parameters.get("location").toString();
             String checkin = parameters.getOrDefault("checkin-date", "").toString().split("T")[0];
             String checkout = parameters.getOrDefault("checkout-date", "").toString().split("T")[0];

             String adultsRaw = parameters.getOrDefault("adults", "1").toString();
             String childrenRaw = parameters.getOrDefault("children", "").toString();
             String roomsRaw = parameters.getOrDefault("rooms", "").toString();

             int adults = adultsRaw.isEmpty() ? 1 : (int) Double.parseDouble(adultsRaw);
             int children = childrenRaw.isEmpty() ? 0 : (int) Double.parseDouble(childrenRaw);
             int rooms = roomsRaw.isEmpty() ? 1 : (int) Double.parseDouble(roomsRaw);


             // TODO: Ánh xạ location -> locationId (Hà Nội => 1)
             int locationId = mapLocationToId(location);

             // Build URL
             String url = String.format("http://localhost:8386/hotel-list?locationId=%d&dateRange=%s => %s&adults=%d&children=%d&rooms=%d",
                     locationId, checkin, checkout, adults, children, rooms);

             String softPrompt = String.format("Người dùng đang tìm khách sạn ở %s từ %s đến %s cho %d người lớn và %d trẻ em. Viết câu phản hồi lịch sự, mời họ bấm nút để xem danh sách.",
                     location, checkin, checkout, adults, children);
             String geminiResponse = geminiService.generateReply(softPrompt);


             // Phản hồi Dialogflow với button
             return Map.of(
                     "fulfillmentMessages", List.of(
                             Map.of("text", Map.of("text", List.of(geminiResponse))),
                             Map.of("text", Map.of("text", List.of("Dưới đây là khách sạn phù hợp với yêu cầu của bạn:"))),
                             Map.of("payload", Map.of(
                                     "richContent", List.of(List.of(
                                             Map.of(
                                                     "type", "button",
                                                     "icon", Map.of("type", "chevron_right", "color", "#FF9800"),
                                                     "text", "Xem danh sách khách sạn",
                                                     "link", url
                                             )
                                     ))
                             ))
                     )
             );

         } catch (Exception e) {
             System.err.println("Webhook error: " + e.getMessage());
             e.printStackTrace(); // ⚠️ In toàn bộ lỗi
             return Map.of("fulfillmentText", "Đã xảy ra lỗi khi xử lý webhook.");
         }
     }

     private int mapLocationToId(String locationName) {
         if (locationName.toLowerCase().contains("hà nội")) return 1;
         if (locationName.toLowerCase().contains("sài gòn")) return 2;
         return 0;
     }
 }
