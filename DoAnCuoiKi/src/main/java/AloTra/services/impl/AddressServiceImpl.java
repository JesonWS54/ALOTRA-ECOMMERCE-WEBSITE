package AloTra.services.impl;

import AloTra.Model.AddressDTO;
import AloTra.entity.Account;
import AloTra.entity.Addresses; // Sửa tên entity
import AloTra.repository.AccountRepository;
import AloTra.repository.AddressRepository;
import AloTra.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AccountRepository accountRepository; // Cần để lấy Account entity

    @Override
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        return addressRepository.findByAccount_IdOrderByIsDefaultDesc(userId) // Sửa tên hàm
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDTO addAddress(Long userId, AddressDTO addressDTO) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + userId));

        Addresses address = new Addresses(); // Sửa tên entity
        address.setAccount(account);
        address.setFullName(addressDTO.getFullName());
        address.setPhone(addressDTO.getPhone());
        address.setStreet(addressDTO.getStreet());
        address.setWardCode(addressDTO.getWardCode());
        address.setDistrictCode(addressDTO.getDistrictCode());
        address.setProvinceCode(addressDTO.getProvinceCode());
        address.setFullAddressText(addressDTO.getFullAddressText());
        // Mặc định không phải là default khi thêm mới
        address.setIsDefault(false);

        Addresses savedAddress = addressRepository.save(address);

        // Nếu đây là địa chỉ đầu tiên của user, tự động đặt làm mặc định
        List<Addresses> userAddresses = addressRepository.findByAccount_IdOrderByIsDefaultDesc(userId);
        if (userAddresses.size() == 1) {
             setDefaultAddress(savedAddress.getId(), userId);
             savedAddress.setIsDefault(true); // Cập nhật lại trạng thái sau khi set default
        }


        return convertToDTO(savedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        Addresses address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // Kiểm tra quyền sở hữu
        if (!address.getAccount().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa địa chỉ này.");
        }

        // Nếu xóa địa chỉ mặc định, cần xử lý (ví dụ: chọn địa chỉ khác làm mặc định nếu có)
        if (address.getIsDefault() != null && address.getIsDefault()) {
           // Có thể thêm logic ở đây, ví dụ tìm địa chỉ khác và đặt làm mặc định
           // Hoặc đơn giản là không cho xóa địa chỉ mặc định nếu chỉ còn 1 địa chỉ
            List<Addresses> remainingAddresses = addressRepository.findByAccount_IdOrderByIsDefaultDesc(userId);
            if(remainingAddresses.size() <= 1) {
                 throw new RuntimeException("Không thể xóa địa chỉ mặc định duy nhất.");
            }
           // Nếu còn địa chỉ khác, chọn cái đầu tiên (không phải cái đang xóa) làm mặc định mới
            if (remainingAddresses.size() > 1) {
                Addresses nextDefault = remainingAddresses.stream()
                                          .filter(a -> !a.getId().equals(addressId))
                                          .findFirst()
                                          .orElse(null);
                if (nextDefault != null) {
                    setDefaultAddress(nextDefault.getId(), userId);
                }
            }
        }


        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long addressId, Long userId) {
        Addresses address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // Kiểm tra quyền sở hữu
        if (!address.getAccount().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền đặt địa chỉ này làm mặc định.");
        }

        // 1. Bỏ hết default cũ của user này
        addressRepository.clearDefaultForUser(userId);

        // 2. Đặt default mới
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    // Hàm chuyển đổi Entity sang DTO
    private AddressDTO convertToDTO(Addresses address) { // Sửa tên entity
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setAccountId(address.getAccount().getId());
        dto.setAccountUsername(address.getAccount().getUsername()); // Lấy username từ Account
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setStreet(address.getStreet());
        dto.setWardCode(address.getWardCode());
        dto.setDistrictCode(address.getDistrictCode());
        dto.setProvinceCode(address.getProvinceCode());
        dto.setFullAddressText(address.getFullAddressText());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
}
