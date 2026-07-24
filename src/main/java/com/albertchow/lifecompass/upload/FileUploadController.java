package com.albertchow.lifecompass.upload;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Image uploads for posts (requirement 4). Files are saved to a project-root
 * {@code uploads/} directory and served back at {@code /uploads/<file>} via
 * {@link UploadResourceConfig} — no object storage needed for local dev.
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    /**
     * Deliberately outside the classpath (not src/main/resources/static): files
     * written there at runtime are never copied into target/classes, so Spring's
     * classpath-backed static handler would 404 on anything uploaded after
     * startup. This directory is served instead via {@link UploadResourceConfig}.
     */
    static final Path UPLOAD_DIR = Paths.get("uploads");

    /** Validates the file is a non-empty, allowed image type, saves it under a random name, and returns its public URL path. */
    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("No file provided");
        }
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException("Only JPEG, PNG, WEBP or GIF images are allowed");
        }

        try {
            Files.createDirectories(UPLOAD_DIR);
            String filename = UUID.randomUUID() + extension;
            file.transferTo(UPLOAD_DIR.resolve(filename));
            return Result.ok("/uploads/" + filename);
        } catch (IOException e) {
            log.error("Failed to save uploaded file", e);
            throw new BusinessException("Could not save the uploaded file");
        }
    }
}
