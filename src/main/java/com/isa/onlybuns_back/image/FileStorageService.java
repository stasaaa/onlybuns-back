package com.isa.onlybuns_back.image;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    public FileStorageService() {

    }

    public String storeFile(MultipartFile file) throws IOException {
        // Get the root directory of the project
        String projectRoot = System.getProperty("user.dir");
        String FOLDER_PATH = projectRoot + "\\uploads\\images";

        // Ensure the directory exists
        File dir = new File(FOLDER_PATH);
        if (!dir.exists()) {
            dir.mkdirs(); // Create the directory if it doesn't exist
        }

        // Convert MultipartFile to BufferedImage
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IOException("Failed to read the image.");
        }

        // Determine file extension based on the image format
        String fileExtension = getFileExtension(file);
        if (fileExtension == null) {
            throw new IOException("Unsupported image format.");
        }

        // Generate a unique file name
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        String filePath = FOLDER_PATH + "\\" + fileName;

        // Write the BufferedImage to the file in the corresponding format
        if (ImageIO.write(image, fileExtension, new File(filePath))) {
            return fileName;  // Return the file name (or path if needed)
        } else {
            throw new IOException("Failed to save the image.");
        }
    }

    private String getFileExtension(MultipartFile file) throws IOException {
        // Extract image format based on the file content
        String contentType = file.getContentType();

        if (contentType == null) {
            return null; // Return null if content type is not found
        }

        // Check the content type and return the appropriate file extension
        if (contentType.equals("image/jpeg")) {
            return "jpg";
        } else if (contentType.equals("image/png")) {
            return "png";
        } else if (contentType.equals("image/gif")) {
            return "gif";
        } else if (contentType.equals("image/bmp")) {
            return "bmp";
        } else {
            return null; // If it's an unsupported format
        }
    }

    public byte[] getImage(String fileName) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        String FOLDER_PATH = projectRoot + "\\uploads\\images";
        String filePath = FOLDER_PATH + "\\" + fileName;
        return Files.readAllBytes(Paths.get(filePath));
    }
}