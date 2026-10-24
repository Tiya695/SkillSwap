package com.skillswap.service;

import com.skillswap.exception.BadRequestException;
import com.skillswap.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;
    FileStorageService storage;

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F'};

    @BeforeEach
    void setUp() {
        storage = new FileStorageService(tempDir.toString());
    }

    @Test
    void savesPngAndJpgAndCanLoadThemAgain() throws Exception {
        String png = storage.saveAvatar(new MockMultipartFile("file", "me.png", "image/png", PNG));
        String jpg = storage.saveAvatar(new MockMultipartFile("file", "me.jpg", "image/jpeg", JPG));

        assertThat(png).endsWith(".png");
        assertThat(jpg).endsWith(".jpg");
        assertThat(Files.exists(tempDir.resolve("avatars").resolve(png))).isTrue();
        assertThat(storage.loadAvatar(png).exists()).isTrue();
    }

    @Test
    void rejectsFilesThatAreNotImagesEvenWithAnImageName() {
        MockMultipartFile fake = new MockMultipartFile("file", "virus.png", "image/png", "<script>alert(1)</script>".getBytes());

        assertThatThrownBy(() -> storage.saveAvatar(fake)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsEmptyAndTooLargeFiles() {
        assertThatThrownBy(() -> storage.saveAvatar(new MockMultipartFile("file", "a.png", "image/png", new byte[0])))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> storage.saveAvatar(
                new MockMultipartFile("file", "a.png", "image/png", new byte[3 * 1024 * 1024])))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("too large");
    }

    @Test
    void refusesPathTricksWhenLoading() {
        assertThatThrownBy(() -> storage.loadAvatar("../../application.properties"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesTheStoredFile() {
        String name = storage.saveAvatar(new MockMultipartFile("file", "me.png", "image/png", PNG));

        storage.deleteByUrl(FileStorageService.AVATAR_URL_PREFIX + name);

        assertThat(Files.exists(tempDir.resolve("avatars").resolve(name))).isFalse();
    }
}
