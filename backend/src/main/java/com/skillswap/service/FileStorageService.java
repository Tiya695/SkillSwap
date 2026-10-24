package com.skillswap.service;

import com.skillswap.exception.BadRequestException;
import com.skillswap.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FileStorageService {

    public static final String AVATAR_URL_PREFIX = "/api/files/avatars/";
    private static final long MAX_BYTES = 2 * 1024 * 1024;
    private static final Pattern SAFE_NAME = Pattern.compile("^[a-f0-9-]{36}\\.(png|jpg|webp)$");

    private final Path avatarDir;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.avatarDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("avatars");
        try {
            Files.createDirectories(avatarDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create upload folder " + avatarDir, e);
        }
    }

    public String saveAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw photoError("Please choose a photo");
        }
        if (file.getSize() > MAX_BYTES) {
            throw photoError("The photo is too large. Please choose one under 2 MB.");
        }
        try {
            byte[] bytes = file.getBytes();
            String extension = detectExtension(bytes);
            if (extension == null) {
                throw photoError("Only PNG, JPG or WEBP photos are allowed");
            }
            String name = UUID.randomUUID() + "." + extension;
            Files.write(avatarDir.resolve(name), bytes);
            return name;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the photo", e);
        }
    }

    public Resource loadAvatar(String name) {
        Path file = resolve(name);
        if (file == null || !Files.exists(file)) {
            throw new ResourceNotFoundException("Photo not found");
        }
        return new PathResource(file);
    }

    public void deleteByUrl(String url) {
        if (url == null || !url.startsWith(AVATAR_URL_PREFIX)) {
            return;
        }
        Path file = resolve(url.substring(AVATAR_URL_PREFIX.length()));
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {

            }
        }
    }

    public static String contentTypeOf(String name) {
        if (name.endsWith(".png")) {
            return "image/png";
        }
        return name.endsWith(".webp") ? "image/webp" : "image/jpeg";
    }

    private Path resolve(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) {
            return null;
        }
        return avatarDir.resolve(name);
    }

    private String detectExtension(byte[] b) {
        if (b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "png";
        }
        if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    private BadRequestException photoError(String message) {
        return new BadRequestException(message, Map.of("photo", message));
    }
}
