package org.swp391.hotelbookingsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swp391.hotelbookingsystem.service.CloudinaryService;

import java.io.IOException;
import java.util.Map;

@RestController           // returns JSON responses
@RequestMapping("/api/files")                 //prefixed with /api/files.
public class FileUploadController {


    private final CloudinaryService cloudinaryService;

    public FileUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,             // Accepts the uploaded file (from a form field named "file").
            @RequestParam(value = "folder", required = false) String folderName) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }


        Map result = cloudinaryService.uploadImage(file, folderName);
        String imageUrl = (String) result.get("secure_url"); // get image url

        return ResponseEntity.ok(Map.of(
                "message", "Upload successful",
                "url", imageUrl
        ));
    }

}
