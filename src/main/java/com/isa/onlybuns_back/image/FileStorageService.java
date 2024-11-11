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

        // Generate a unique file path with .jpg extension
        String fileName = UUID.randomUUID().toString() + ".jpg";
        String filePath = FOLDER_PATH + "\\" + fileName ;

        // Write the BufferedImage to the file as a JPG
        ImageIO.write(image, "jpg", new File(filePath));

        return fileName; // Return the file path of the saved image
    }

    public byte[] getImage(String fileName) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        String FOLDER_PATH = projectRoot + "\\uploads\\images";
        String filePath = FOLDER_PATH + "\\" + fileName;
        return Files.readAllBytes(Paths.get(filePath));
    }
}