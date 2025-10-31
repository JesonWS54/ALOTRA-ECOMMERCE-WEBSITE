package nhom12.AloTra.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String storeFile(MultipartFile file, String subFolder);
    void deleteFile(String filePath);
}
