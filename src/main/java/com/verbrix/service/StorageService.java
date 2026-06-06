package com.verbrix.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file);

    void confirmFile(String fileUrl);

    int deleteOrphanedFiles();
}