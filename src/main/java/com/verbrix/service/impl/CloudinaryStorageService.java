package com.verbrix.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.verbrix.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Profile("prod")
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    @Override
    public String store(MultipartFile file) {
        try {
            // 1. Create unique filename: UUID + Timestamp
            String timestamp = String.valueOf(Instant.now().toEpochMilli());
            String fileName = UUID.randomUUID().toString() + "_" + timestamp;

            // 2. Upload with "temp_interpreter" tag
            Map params = ObjectUtils.asMap(
                    "public_id", fileName,
                    "resource_type", "auto",
                    "tags", "temp_interpreter" // <--- Important: Marks as temporary
            );

            Map uploadResult = this.cloudinary.uploader().upload(file.getBytes(), params);

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    @Override
    public void confirmFile(String fileUrl) {
        try {
            // 1. Extract the public_id from the URL
            String publicId = extractPublicIdFromUrl(fileUrl);

            // 2. Remove the "temp_interpreter" tag
            // This effectively "saves" the file from the garbage collector
            cloudinary.uploader().removeTag("temp_interpreter", new String[]{publicId}, ObjectUtils.emptyMap());

        } catch (Exception e) {
            // Log this error, but don't fail the user request because of it.
            // The file is safe, it just might get deleted by cleanup if not fixed.
            System.err.println("Failed to confirm file: " + fileUrl + " Error: " + e.getMessage());
        }
    }

    @Override
    public int deleteOrphanedFiles() {
        try {
            // 1. Search for files:
            // - Tagged "temp_interpreter"
            // - Created more than 1 day ago (created_at<1d)
            // - Limit to 100 to prevent timeouts (the job runs hourly, so it will catch up)
            var result = cloudinary.search()
                    .expression("tags:temp_interpreter AND created_at<1d")
                    .maxResults(100)
                    .execute();

            java.util.List<java.util.Map> resources = (java.util.List<java.util.Map>) result.get("resources");

            if (resources == null || resources.isEmpty()) {
                return 0;
            }

            // 2. Extract public_ids to delete
            java.util.List<String> publicIds = new java.util.ArrayList<>();
            for (java.util.Map res : resources) {
                publicIds.add((String) res.get("public_id"));
            }

            // 3. Delete them in batch
            if (!publicIds.isEmpty()) {
                cloudinary.api().deleteResources(publicIds, ObjectUtils.emptyMap());
            }

            return publicIds.size();

        } catch (Exception e) {
            // Log error but return -1 or throw to let the controller know
            System.err.println("Cloudinary cleanup failed: " + e.getMessage());
            return -1;
        }
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;

        // Find the last '/'
        int lastSlashIndex = url.lastIndexOf('/');
        // Find the last '.' (extension)
        int lastDotIndex = url.lastIndexOf('.');

        if (lastSlashIndex == -1 || lastDotIndex == -1) {
            throw new IllegalArgumentException("Invalid Cloudinary URL format");
        }

        // Cloudinary URLs usually have version numbers, we just want the filename part
        return url.substring(lastSlashIndex + 1, lastDotIndex);
    }
}