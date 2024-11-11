package com.isa.onlybuns_back.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);  // Create the directory if it doesn't exist
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory.", e);
        }
    }

    public String storeFile(MultipartFile file) {
        // Clean the file name to prevent any malicious characters
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            // Resolve the path where the file will be stored
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            // Copy the file to the target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/images/" + fileName;  // Return the relative URL of the stored file
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + fileName, e);
        }
    }
}