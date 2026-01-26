package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImplementation implements FileService{
    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // File names of current/ original file
        String originalFileName = file.getOriginalFilename();

        // In real time application, we will upload the image to file system or to cloud storage like AWS S3.
        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();
        // ex. name = a.jpg, randomId = 1234-5678-9101, fileName = 1234-5678-9101.jpg
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf(".")));
        String filePath = path + File.separator + fileName;

        // check if path exist and create the path if not exist
        File folder = new File(path);
        if (!folder.exists())
            folder.mkdir();

        // upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));

        // return the file name
        return fileName;
    }
}
