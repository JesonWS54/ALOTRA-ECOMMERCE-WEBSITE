package AloTra.services;

import AloTra.Model.AddressDTO;
import java.util.List;

public interface AddressService {

    /**
     * Lấy danh sách tất cả địa chỉ của một user.
     * Địa chỉ mặc định sẽ nằm ở đầu danh sách.
     * @param userId ID của user.
     * @return Danh sách AddressDTO.
     */
    List<AddressDTO> getAddressesByUserId(Long userId);

    /**
     * Thêm một địa chỉ mới cho user.
     * @param userId ID của user.
     * @param addressDTO Thông tin địa chỉ mới.
     * @return AddressDTO đã được tạo.
     */
    AddressDTO addAddress(Long userId, AddressDTO addressDTO);

    /**
     * Xóa một địa chỉ.
     * @param addressId ID của địa chỉ cần xóa.
     * @param userId ID của user sở hữu địa chỉ (để kiểm tra quyền).
     * @throws RuntimeException Nếu không tìm thấy địa chỉ hoặc không có quyền xóa.
     */
    void deleteAddress(Long addressId, Long userId);

    /**
     * Đặt một địa chỉ làm mặc định.
     * @param addressId ID của địa chỉ cần đặt làm mặc định.
     * @param userId ID của user sở hữu địa chỉ.
     * @throws RuntimeException Nếu không tìm thấy địa chỉ hoặc không có quyền.
     */
    void setDefaultAddress(Long addressId, Long userId);

    // (Optional: Thêm hàm updateAddress nếu cần)
    // AddressDTO updateAddress(Long addressId, Long userId, AddressDTO addressDTO);
}