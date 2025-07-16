 package org.swp391.hotelbookingsystem.controller.chatbot;

 import org.swp391.hotelbookingsystem.service.GeminiService;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RestController;
 import org.swp391.hotelbookingsystem.service.LocationService;

 import java.util.List;
 import java.util.Map;

 @RestController
 public class DialogflowWebhookController {

     private final GeminiService geminiService;
     private final LocationService locationService;

     public DialogflowWebhookController(GeminiService geminiService, LocationService locationService) {
         this.geminiService = geminiService;
         this.locationService = locationService;
     }

     @PostMapping("/webhook")
     public Map<String, Object> handleDialogflow(@RequestBody Map<String, Object> payload) {
         try {
             System.out.println("=== Webhook Received ===");
             System.out.println("Payload: " + payload);

             Map<String, Object> queryResult = (Map<String, Object>) payload.get("queryResult");
             String intentName = ((Map<String, Object>) queryResult.get("intent")).get("displayName").toString();
             Map<String, Object> parameters = (Map<String, Object>) queryResult.get("parameters");

             System.out.println("Intent: " + intentName);

             if ("GetAvailableLocations".equals(intentName)) {
                 List<String> locations = locationService.getAllLocationNames();
                 if (locations.isEmpty()) {
                     return Map.of("fulfillmentText", "Hiện tại chưa có địa điểm nào khả dụng.");
                 }

                 String reply = "Chúng tôi có khách sạn tại các địa điểm sau: " + String.join(", ", locations) + ".";
                 return Map.of("fulfillmentText", reply);
             }

             if ("Default Fallback Intent".equals(intentName) || parameters.isEmpty() || !parameters.containsKey("location")) {
                 return Map.of("fulfillmentText", "Xin lỗi, bạn có thể nói rõ địa điểm bạn muốn đặt phòng không?");
             }

             // Trích thông tin
             String location = parameters.get("location").toString().toLowerCase().replace("thành phố", "").trim();
             location = location.substring(0, 1).toUpperCase() + location.substring(1);

             String checkin = parameters.getOrDefault("checkin-date", "").toString().split("T")[0];
             String checkout = parameters.getOrDefault("checkout-date", "").toString().split("T")[0];

             String guestsRaw = parameters.getOrDefault("guests", "1").toString();
             String roomsRaw = parameters.getOrDefault("rooms", "").toString();

             int guests = guestsRaw.isEmpty() ? 1 : (int) Double.parseDouble(guestsRaw);
             int rooms = roomsRaw.isEmpty() ? 1 : (int) Double.parseDouble(roomsRaw);

             // Lấy locationId từ DB
             Integer locationId = locationService.getLocationIdByCityName(location);
             if (locationId == null) {
                 return Map.of("fulfillmentText", "Xin lỗi, hiện tại chúng tôi chưa hỗ trợ khu vực " + location + ".");
             }

             // Build URL
             String url = String.format("http://localhost:8386/hotel-list?locationId=%d&dateRange=%s => %s&guests=%d&rooms=%d",
                     locationId, checkin, checkout, guests, rooms);

             String softPrompt = String.format("Người dùng đang tìm khách sạn ở %s từ %s đến %s cho %d người. Viết câu phản hồi lịch sự, mời họ bấm nút để xem danh sách.(lưu ý tôi đã có nút ở dưới sẵn rồi)",
                     location, checkin, checkout, guests);
             String geminiResponse = geminiService.generateReply(softPrompt);


             // Phản hồi Dialogflow với button
             return Map.of(
                     "fulfillmentMessages", List.of(
                             Map.of("text", Map.of("text", List.of(geminiResponse))),
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
 }
