package AloTra.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {

    /**
     * Upload file lên Cloudinary.
     * @param file File cần upload (từ form).
     * @return Map chứa thông tin kết quả upload từ Cloudinary (bao gồm 'url', 'public_id', etc.).
     * @throws IOException Nếu có lỗi đọc file hoặc lỗi kết nối.
     */
    Map uploadFile(MultipartFile file) throws IOException; // <--- Thêm dòng này

    /**
     * Xóa file khỏi Cloudinary dựa trên public_id.
     * @param publicId ID công khai của file trên Cloudinary.
     * @return Map chứa kết quả xóa từ Cloudinary.
     * @throws IOException Nếu có lỗi kết nối.
     */
    Map deleteFile(String publicId) throws IOException;

}