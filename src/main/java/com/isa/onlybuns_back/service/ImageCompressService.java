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
import java.time.LocalDate;
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
    public void compressDailyImages() throws IOException {
        logger.info("Image compressing started...");
        LocalDate oneMonthAgo = LocalDate.now().minus(1, ChronoUnit.MONTHS);

        //retrieve images older than one month that are not compressed
        List<Post> posts = postRepository.findImagesToCompress(oneMonthAgo);

        for(Post post : posts) {
            Path imagePath = Path.of(post.getImagePaths());
            compressImage(imagePath);
            post.setCompressed(true);
            postRepository.save(post);
            logger.info("Compressed image: " + post.getImagePaths());
        }
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
