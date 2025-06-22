 package org.swp391.hotelbookingsystem.service;

 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import okhttp3.*;
 import org.springframework.stereotype.Service;

 @Service
 public class GeminiService {

     private static final String API_KEY = "AIzaSyDjQbBkZOiOz2P7dB1Vb9bxUF8lk4u6-ZI";
     private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
     private final OkHttpClient client = new OkHttpClient();
     private final ObjectMapper mapper = new ObjectMapper();

     public String generateReply(String userMessage) {
         try {
             String safeText = mapper.writeValueAsString(userMessage);

             String jsonBody = """
                 {
                   "contents": [{
                     "parts": [{"text": %s}]
                   }]
                 }
                 """.formatted(safeText);

             Request request = new Request.Builder()
                     .url(ENDPOINT)
                     .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                     .addHeader("Content-Type", "application/json")
                     .build();

             try (Response response = client.newCall(request).execute()) {
                 if (!response.isSuccessful()) {
                     return "Gemini API error: " + response.code();
                 }

                 String jsonResponse = response.body().string();
                 JsonNode root = mapper.readTree(jsonResponse);

                 return root.path("candidates")
                         .get(0)
                         .path("content")
                         .path("parts")
                         .get(0)
                         .path("text")
                         .asText();
             }

         } catch (Exception e) {
             e.printStackTrace();
             return "Lỗi gọi Gemini: " + e.getMessage();
         }
     }
 }
