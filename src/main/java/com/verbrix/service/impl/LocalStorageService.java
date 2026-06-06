package com.verbrix.service.impl;

import com.verbrix.service.StorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Transactional
@Profile("dev")
public class LocalStorageService implements StorageService {

    private final Path fileStorageLocation;

    public LocalStorageService() {
        // Create a directory called "uploads" in the project root
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String store(MultipartFile file) {
        // Normalize file name
        String originalFileName = file.getOriginalFilename();
        // Generate unique name to prevent overwrite (uuid_filename.jpg)
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Generate the Download URL (e.g., http://localhost:8080/uploads/uuid_image.jpg)
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public void confirmFile(String fileUrl) {

    }

    @Override
    public int deleteOrphanedFiles() {
        return 0;
    }
}