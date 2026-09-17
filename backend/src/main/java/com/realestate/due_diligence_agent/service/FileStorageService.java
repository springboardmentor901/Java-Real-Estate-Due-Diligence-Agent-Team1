package com.realestate.due_diligence_agent.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {

    private final Path storageDirectory;

    public FileStorageService() {

        this.storageDirectory = Paths.get("reports")
                .toAbsolutePath()
                .normalize();

        try {

            Files.createDirectories(storageDirectory);

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Could not create report storage directory.",
                    ex
            );
        }
    }

    public String saveFile(
            byte[] fileBytes,
            String fileName) {

        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException(
                    "File content cannot be empty."
            );
        }

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "File name cannot be empty."
            );
        }

        try {

            Path targetPath = storageDirectory
                    .resolve(fileName)
                    .normalize();

            if (!targetPath.startsWith(storageDirectory)) {
                throw new IllegalArgumentException(
                        "Invalid file name."
                );
            }

            Files.write(targetPath, fileBytes);

            return targetPath.toString();

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Could not save file: " + fileName,
                    ex
            );
        }
    }

    public Resource loadFile(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "File path cannot be empty."
            );
        }

        try {

            Path path = Paths.get(filePath)
                    .toAbsolutePath()
                    .normalize();

            if (!path.startsWith(storageDirectory)) {
                throw new IllegalArgumentException(
                        "Invalid file path."
                );
            }

            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException(
                        "File not found: " + filePath
                );
            }

            return resource;

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Could not load file: " + filePath,
                    ex
            );
        }
    }

    public String getFilePath(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "File name cannot be empty."
            );
        }

        Path targetPath = storageDirectory
                .resolve(fileName)
                .normalize();

        if (!targetPath.startsWith(storageDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid file name."
            );
        }

        return targetPath.toString();
    }
}
