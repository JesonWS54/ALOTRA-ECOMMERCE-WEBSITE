package AloTra.services.impl;

import AloTra.Model.AppCommissionDTO;
import AloTra.entity.Account;
import AloTra.entity.AppCommission;
import AloTra.entity.Category;
import AloTra.repository.AccountRepository;
import AloTra.repository.AppCommissionRepository;
import AloTra.repository.CategoryRepository;
import AloTra.services.AppCommissionService;
import jakarta.persistence.EntityNotFoundException; // Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppCommissionServiceImpl implements AppCommissionService {

    @Autowired
    private AppCommissionRepository commissionRepository;

    @Autowired
    private CategoryRepository categoryRepository; // Cần để lấy Category

    @Autowired
    private AccountRepository accountRepository; // Cần để lấy Admin

    @Override
    public List<AppCommissionDTO> getAllCommissions() {
        // Sử dụng query đã tạo trong repository để lấy đủ thông tin
        return commissionRepository.findAllWithDetails().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AppCommissionDTO> getCommissionByCategoryId(Long categoryId) {
        return commissionRepository.findByCategory_Id(categoryId)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public AppCommissionDTO saveOrUpdateCommission(AppCommissionDTO commissionDTO, Long adminId) {
        if (commissionDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID không được để trống.");
        }
        if (commissionDTO.getCommissionRate() == null || commissionDTO.getCommissionRate() < 0 || commissionDTO.getCommissionRate() > 1) {
            throw new IllegalArgumentException("Tỷ lệ chiết khấu phải từ 0 đến 1.");
        }

        Category category = categoryRepository.findById(commissionDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với ID: " + commissionDTO.getCategoryId()));

        // TODO: Kiểm tra xem adminId có phải là Admin thực sự không
        Account admin = accountRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản admin với ID: " + adminId));

        // Tìm xem đã có chiết khấu cho category này chưa
        Optional<AppCommission> existingCommissionOpt = commissionRepository.findByCategory_Id(category.getId());

        AppCommission commission;
        if (existingCommissionOpt.isPresent()) {
            // Cập nhật chiết khấu cũ
            commission = existingCommissionOpt.get();
        } else {
            // Tạo chiết khấu mới
            commission = new AppCommission();
            commission.setCategory(category);
        }

        // Cập nhật thông tin
        commission.setCommissionRate(commissionDTO.getCommissionRate());
        commission.setAdmin(admin); // Lưu lại admin đã cập nhật cuối cùng

        AppCommission savedCommission = commissionRepository.save(commission);
        return convertToDTO(savedCommission);
    }

    // --- Helper Method ---
    private AppCommissionDTO convertToDTO(AppCommission commission) {
        if (commission == null) return null;
        AppCommissionDTO dto = new AppCommissionDTO();
        dto.setId(commission.getId());
        dto.setCategoryId(commission.getCategory() != null ? commission.getCategory().getId() : null);
        dto.setCategoryName(commission.getCategory() != null ? commission.getCategory().getName() : "N/A");
        dto.setCommissionRate(commission.getCommissionRate());
        dto.setAdminId(commission.getAdmin() != null ? commission.getAdmin().getId() : null);
        dto.setAdminUsername(commission.getAdmin() != null ? commission.getAdmin().getUsername() : "N/A");
        return dto;
    }
}
