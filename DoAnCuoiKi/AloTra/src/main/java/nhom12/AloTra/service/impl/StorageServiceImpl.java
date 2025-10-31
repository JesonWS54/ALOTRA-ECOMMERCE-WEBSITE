package nhom17.OneShop.service.impl;

import nhom17.OneShop.service.StorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {
    // ✅ Hardcode the root path here instead of injecting it
    private final Path rootStorageFolder = Paths.get("./uploads");

    // ✅ Use a simple, no-argument constructor
    public StorageServiceImpl() {
        try {
            Files.createDirectories(this.rootStorageFolder);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize storage", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subFolder) {
        if (file.isEmpty()) {
            return null;
        }
        // This logic remains the same
        Path destinationFolder = this.rootStorageFolder.resolve(subFolder).normalize();
        try {
            Files.createDirectories(destinationFolder);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create subfolder for storage", e);
        }
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        String generatedFileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExtension;
        Path destinationFilePath = destinationFolder.resolve(generatedFileName).normalize();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
        return subFolder + "/" + generatedFileName;
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        try {
            Path file = this.rootStorageFolder.resolve(filePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Could not delete file: " + filePath + ". Error: " + e.getMessage());
        }
    }
}
