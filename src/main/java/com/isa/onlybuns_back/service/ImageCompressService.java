package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.repository.PostRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ImageCompressService {

    private final PostRepository postRepository;

    public ImageCompressService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageCompressService.class);

    //scheduled to run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 */1 * * * ?")
    public void compressDailyImages() throws IOException {
        logger.info("Image compressing started...");
        LocalDateTime oneMonthAgo = LocalDateTime.now().minus(1, ChronoUnit.MONTHS);

        //LocalDateTime oneMinuteAgo = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);
        logger.info("Retrieving images older than one minute...");

        // Define the relative base path for image storage
        Path basePath = Paths.get("uploads", "images");

        // Retrieve images older than one month that are not compressed
        List<Post> posts = postRepository.findImagesToCompress(oneMonthAgo);
        logger.info("Number of images to compress: " + posts.size());

        for (Post post : posts) {
            String imagePathString = post.getImagePaths();
            logger.info("Processing image for post ID: " + post.getId() + ", Image Path: " + imagePathString);

            // Combine base path with image path to get the full path
            Path imagePath = basePath.resolve(imagePathString);

            try {
                compressImage(imagePath);
                post.setCompressed(true);
                postRepository.save(post);
                logger.info("Compressed and saved image for post ID: " + post.getId() + ", Path: " + imagePath);
            } catch (IOException e) {
                logger.error("Failed to compress image for post ID: " + post.getId() + ", Path: " + imagePath, e);
            }
        }
        logger.info("Image compressing process completed.");
    }

    private void compressImage(Path imagePath) throws IOException {
        File originalImageFile = imagePath.toFile();
        //samo linija ispod i onda thumbnails ako su slike samo jpg
        //Path compressedImagePath = Paths.get(imagePath.toString().replace(".jpg", "_compressed.jpg"));
        String originalFileName = originalImageFile.getName();
        // izvlacenje ekstenzije
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

        // pravljenje nove putanje za kompresovanu sliku (dodavanje "_compressed" u ime)
        String compressedFileName = originalFileName.replace("." + extension, "_compressed." + extension);
        Path compressedImagePath = imagePath.resolveSibling(compressedFileName);

        // kompresija slike i cuvanje kompresovane verzije
        Thumbnails.of(originalImageFile)
                .size(800, 600)
                .outputQuality(0.7)
                .toFile(compressedImagePath.toFile());
    }

}
