package AloTra.services.impl;

import AloTra.services.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID; // Dùng để tạo public_id ngẫu nhiên

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary; // Bean được tạo từ CloudinaryConfig

    @Override
    public Map uploadFile(MultipartFile file) throws IOException {
        // Kiểm tra file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Tạo public_id ngẫu nhiên để tránh trùng tên file
        // Bạn có thể tùy chỉnh lại cách đặt tên nếu muốn (vd: theo userId, productId...)
        String publicId = "alotra_uploads/" + UUID.randomUUID().toString();

        // Upload file và trả về kết quả
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "auto" // Tự động nhận diện loại file (image, video, raw)
                       //,"folder", "avatars" // Có thể thêm thư mục nếu muốn
                ));
    }

    @Override
    public Map deleteFile(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            throw new IllegalArgumentException("Public ID cannot be empty");
        }
        // Xóa file dựa trên publicId và trả kết quả
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
