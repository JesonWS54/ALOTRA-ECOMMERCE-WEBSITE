package AloTra.services;

import AloTra.Model.AppCommissionDTO;

import java.util.List;
import java.util.Optional;

public interface AppCommissionService {

    /**
     * Lấy tất cả các cài đặt chiết khấu hiện có.
     * @return Danh sách AppCommissionDTO.
     */
    List<AppCommissionDTO> getAllCommissions();

    /**
     * Lấy chiết khấu theo ID danh mục.
     * @param categoryId ID của danh mục.
     * @return Optional chứa AppCommissionDTO nếu tìm thấy.
     */
    Optional<AppCommissionDTO> getCommissionByCategoryId(Long categoryId);

    /**
     * Cập nhật (hoặc thêm mới nếu chưa có) chiết khấu cho một danh mục.
     * @param commissionDTO DTO chứa categoryId và commissionRate mới.
     * @param adminId ID của admin thực hiện thay đổi.
     * @return AppCommissionDTO đã được cập nhật/tạo mới.
     * @throws RuntimeException Nếu không tìm thấy danh mục hoặc admin.
     */
    AppCommissionDTO saveOrUpdateCommission(AppCommissionDTO commissionDTO, Long adminId);

    // Không cần hàm xóa vì thường chỉ cập nhật rate

}
