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
    private final SessionContextCache sessionContextCache;

    public DialogflowWebhookController(GeminiService geminiService, LocationService locationService, SessionContextCache sessionContextCache) {
        this.geminiService = geminiService;
        this.locationService = locationService;
        this.sessionContextCache = sessionContextCache;
    }

    private String stripMarkdown(String text) {
        return text
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")     // bỏ **text**
                .replaceAll("__(.*?)__", "$1")             // bỏ __text__
                .replaceAll("\\*(.*?)\\*", "$1")           // bỏ *text*
                .replaceAll("(?m)^\\s*\\*\\s+", "- ")       // dòng bắt đầu bằng * => gạch đầu dòng
                .replaceAll("\\s*-\\s+", "\n- ")
                .replaceAll("(?<!\\n)\\n", "\n");
    }

    @PostMapping("/webhook")
    public Map<String, Object> handleDialogflow(@RequestBody Map<String, Object> payload) {
        try {
            String sessionPath = (String) payload.get("session");
            String sessionId = sessionPath != null ? sessionPath.substring(sessionPath.lastIndexOf('/') + 1) : "default";

            Map<String, Object> queryResult = (Map<String, Object>) payload.get("queryResult");
            String intentName = ((Map<String, Object>) queryResult.get("intent")).get("displayName").toString();
            Map<String, Object> parameters = (Map<String, Object>) queryResult.get("parameters");
            String userQuery = (String) queryResult.get("queryText");

            // Ghi nhớ tên người dùng nếu có
            if (parameters.containsKey("myName")) {
                String name = parameters.get("myName").toString().trim();
                sessionContextCache.setUserName(sessionId, name);
                sessionContextCache.addMessage(sessionId, "User: Tôi tên là " + name);
            }

            // Lưu user message vào session
            sessionContextCache.addMessage(sessionId, "User: " + userQuery);

            // Tạo prompt có lịch sử
            List<String> history = sessionContextCache.getSessionHistory(sessionId);
            String userName = sessionContextCache.getUserName(sessionId);
            StringBuilder promptBuilder = new StringBuilder("Bạn là trợ lý khách sạn tên Hamora. Hãy trả lời thân thiện, xưng hô với người dùng là " + userName + ".\nLịch sử trò chuyện:\n");
            for (String msg : history) {
                promptBuilder.append(msg).append("\n");
            }
            promptBuilder.append("Assistant:");
            String reply = geminiService.generateReply(promptBuilder.toString());
            sessionContextCache.addMessage(sessionId, "Assistant: " + reply);

            System.out.println("Intent: " + intentName);

            if (!List.of("GetAvailableLocations", "Tìm phòng").contains(intentName)) {
                return Map.of("fulfillmentText", stripMarkdown(reply));
            }

            if ("GetAvailableLocations".equals(intentName)) {
                List<String> locations = locationService.getAllLocationNames();
                if (locations.isEmpty()) {
                    String emptyReply = "Hiện tại chưa có địa điểm nào khả dụng.";
                    sessionContextCache.addMessage(sessionId, "Assistant: " + emptyReply);
                    return Map.of("fulfillmentText", emptyReply);
                }

                // Tạo prompt sử dụng tên người dùng + lịch sử + danh sách địa điểm
                userName = sessionContextCache.getUserName(sessionId);
                StringBuilder prompt = new StringBuilder("""
                    Bạn là Hamora - trợ lý đặt phòng khách sạn thông minh.
                    - Hãy xưng hô thân thiện với người dùng là %s.
                    - Dưới đây là danh sách địa điểm có khách sạn: %s
                    - Hãy gợi ý lịch sự để người dùng chọn địa điểm tiếp theo.
            
                    Lịch sử hội thoại:
                    """.formatted(userName, String.join(", ", locations)));

                for (String msg : sessionContextCache.getSessionHistory(sessionId)) {
                    prompt.append(msg).append("\n");
                }
                prompt.append("Assistant:");

                reply = stripMarkdown(geminiService.generateReply(prompt.toString()));
                sessionContextCache.addMessage(sessionId, "Assistant: " + reply);

                return Map.of("fulfillmentText", reply);
            }

            if ("Tìm phòng".equals(intentName)) {
                // Lấy tên người dùng (nếu có) để xưng hô
                userName = sessionContextCache.getUserName(sessionId);

                // Kiểm tra thiếu thông tin
                boolean missing = false;
                StringBuilder softPrompt = new StringBuilder("""
                Bạn là Hamora - trợ lý đặt phòng khách sạn.
                Hãy xưng hô thân thiện với người dùng là %s.
        
                Người dùng đang muốn đặt phòng nhưng còn thiếu thông tin:
                """.formatted(userName));

                // Lấy và kiểm tra từng parameter
                String rawLocation = parameters.getOrDefault("location", "").toString().trim();
                String checkinDate = parameters.getOrDefault("checkin-date", "").toString().trim();
                String checkoutDate = parameters.getOrDefault("checkout-date", "").toString().trim();
                String guestsRaw = parameters.getOrDefault("guests", "").toString().trim();
                String roomsRaw = parameters.getOrDefault("rooms", "").toString().trim();

                if (rawLocation.isBlank()) {
                    softPrompt.append("- Địa điểm.\n");
                    missing = true;
                }
                if (checkinDate.isBlank()) {
                    softPrompt.append("- Ngày nhận phòng.\n");
                    missing = true;
                }
                if (checkoutDate.isBlank()) {
                    softPrompt.append("- Ngày trả phòng.\n");
                    missing = true;
                }

                // Nếu thiếu => hỏi tiếp bằng Gemini
                if (missing) {
                    history = sessionContextCache.getSessionHistory(sessionId);
                    softPrompt.append("\nLịch sử hội thoại:\n");
                    for (String msg : history) {
                        softPrompt.append(msg).append("\n");
                    }
                    softPrompt.append("Assistant:");

                    reply = stripMarkdown(geminiService.generateReply(softPrompt.toString()));
                    sessionContextCache.addMessage(sessionId, "Assistant: " + reply);
                    return Map.of("fulfillmentText", reply);
                }

                // ✅ Đủ thông tin, xử lý tiếp
                // Chuẩn hóa location (có thể gây lỗi nếu không check độ dài)
                String locationClean = rawLocation.toLowerCase().replace("thành phố", "").trim();
                String location = locationClean.isBlank()
                        ? rawLocation // fallback
                        : locationClean.length() == 1
                        ? locationClean.toUpperCase()
                        : locationClean.substring(0, 1).toUpperCase() + locationClean.substring(1);
                int guests = guestsRaw.isBlank() ? 1 : (int) Double.parseDouble(guestsRaw);
                int rooms = roomsRaw.isBlank() ? 1 : (int) Double.parseDouble(roomsRaw);

                String checkin = checkinDate.split("T")[0];
                String checkout = checkoutDate.split("T")[0];

                Integer locationId = locationService.getLocationIdByCityName(location);
                if (locationId == null) {
                    reply = "Xin lỗi, hiện tại chúng tôi chưa hỗ trợ khu vực " + location + ".";
                    sessionContextCache.addMessage(sessionId, "Assistant: " + reply);
                    return Map.of("fulfillmentText", reply);
                }

                String url = String.format("http://localhost:8386/hotel-list?locationId=%d&dateRange=%s => %s&guests=%d&rooms=%d",
                        locationId, checkin, checkout, guests, rooms);

                String softPrompt2 = String.format("""
                    Người dùng đang tìm khách sạn ở %s từ %s đến %s cho %d người.
                    Viết phản hồi lịch sự, mời người dùng bấm nút để xem danh sách.
                    (Lưu ý: nút đã hiển thị bên dưới)
                    """, location, checkin, checkout, guests);

                String geminiResponse = stripMarkdown(geminiService.generateReply(softPrompt2));
                sessionContextCache.addMessage(sessionId, "Assistant: " + geminiResponse);

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
            }
            return Map.of("fulfillmentText", "Xin lỗi, tôi chưa hiểu rõ yêu cầu của bạn.");
        } catch (Exception e) {
            System.err.println("Webhook error: " + e.getMessage());
            e.printStackTrace();
            return Map.of("fulfillmentText", "Đã xảy ra lỗi khi xử lý webhook.");
        }
    }
}
