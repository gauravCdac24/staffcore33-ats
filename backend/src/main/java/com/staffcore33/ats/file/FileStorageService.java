package com.staffcore33.ats.file;

import com.staffcore33.ats.common.BadRequestException;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.config.AppProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final AppProperties appProperties;
    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(appProperties.getFile().getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            log.info("Upload directory ready at {}", rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory", e);
        }
    }

    public String store(MultipartFile file, String subdirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        if (original.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }
        String extension = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            extension = original.substring(dot);
        }
        String storedName = UUID.randomUUID() + extension;
        try {
            Path targetDir = rootLocation;
            if (subdirectory != null && !subdirectory.isBlank()) {
                targetDir = rootLocation.resolve(subdirectory).normalize();
                Files.createDirectories(targetDir);
            }
            Path destination = targetDir.resolve(storedName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return subdirectory == null || subdirectory.isBlank()
                    ? storedName
                    : subdirectory + "/" + storedName;
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    public Resource loadAsResource(String storedName) {
        try {
            Path file = rootLocation.resolve(storedName).normalize();
            if (!file.startsWith(rootLocation)) {
                throw new BadRequestException("Invalid file path");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + storedName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + storedName);
        }
    }

    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        try {
            Path file = rootLocation.resolve(storedName).normalize();
            if (file.startsWith(rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", storedName, e.getMessage());
        }
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}
