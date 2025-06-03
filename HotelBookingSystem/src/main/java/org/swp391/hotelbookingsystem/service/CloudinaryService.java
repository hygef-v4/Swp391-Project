package org.swp391.hotelbookingsystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<?, ?> uploadImage(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,           //Tells Cloudinary which folder to store the file in.
                "resource_type", "image" ,          //tells Cloudinary this is an image file.

                // Apply compression at upload
                "transformation", new com.cloudinary.Transformation().quality("auto")  // auto compress
                        .fetchFormat("auto")       // auto convert to webp/avif to minimize storage
        ));



    }


    public Map<?, ?> uploadVideo(MultipartFile file, String folder) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "video"
        ));
    }

}
