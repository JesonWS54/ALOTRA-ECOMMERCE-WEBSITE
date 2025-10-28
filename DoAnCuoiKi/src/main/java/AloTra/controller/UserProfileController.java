package AloTra.controller;

import AloTra.Model.AccountDTO;
import AloTra.Model.AddressDTO; // Import AddressDTO
import AloTra.services.AccountService;
import AloTra.services.AddressService; // Import AddressService
import AloTra.services.CloudinaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List; // Import List
import java.util.Map;
import java.util.Optional;

@Controller
public class UserProfileController {

    private static final Long MOCK_USER_ID = 7L;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AddressService addressService; // Tiêm AddressService

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/user/profile")
    public String viewProfile(Model model, HttpServletRequest request) {
        Optional<AccountDTO> accountOpt = accountService.getAccountById(MOCK_USER_ID);

        if (accountOpt.isPresent()) {
            model.addAttribute("account", accountOpt.get());
            // Lấy danh sách địa chỉ và thêm vào model
            List<AddressDTO> addresses = addressService.getAddressesByUserId(MOCK_USER_ID);
            model.addAttribute("addresses", addresses);
            model.addAttribute("newAddress", new AddressDTO()); // Thêm đối tượng rỗng cho form thêm mới
            model.addAttribute("currentUri", request.getServletPath());
            return "user/profile";
        } else {
            // Xử lý trường hợp không tìm thấy user (ví dụ: redirect về trang login hoặc home)
            // Tạm thời redirect về home của user
             return "redirect:/user/home"; // Nên có trang lỗi hoặc xử lý khác
        }
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam String fullName, @RequestParam String phone, RedirectAttributes redirectAttributes) {
         try {
            AccountDTO updatedAccount = accountService.updateAccountInfo(MOCK_USER_ID, fullName, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật thông tin: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/user/profile/update-avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile avatarFile, RedirectAttributes redirectAttributes) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một file ảnh.");
            return "redirect:/user/profile";
        }
         if (avatarFile.getSize() > 10 * 1024 * 1024) { // Kiểm tra kích thước file (ví dụ: 10MB)
            redirectAttributes.addFlashAttribute("errorMessage", "Kích thước file ảnh không được vượt quá 10MB.");
            return "redirect:/user/profile";
        }


        try {
            Map uploadResult = cloudinaryService.uploadFile(avatarFile);
            String newAvatarUrl = (String) uploadResult.get("secure_url");
            if (newAvatarUrl == null) {
                throw new RuntimeException("Upload ảnh thất bại, không nhận được URL.");
            }
            accountService.updateAccountAvatar(MOCK_USER_ID, newAvatarUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ảnh đại diện thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đọc file ảnh: " + e.getMessage());
        } catch (RuntimeException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật avatar: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }

    // --- THÊM CÁC HÀM XỬ LÝ ĐỊA CHỈ ---

    @PostMapping("/user/profile/address/add")
    public String addAddress(@ModelAttribute AddressDTO newAddress, RedirectAttributes redirectAttributes) {
        try {
             // Validate thông tin địa chỉ ở đây nếu cần
             if (newAddress.getFullName() == null || newAddress.getFullName().isBlank() ||
                 newAddress.getPhone() == null || newAddress.getPhone().isBlank() ||
                 newAddress.getFullAddressText() == null || newAddress.getFullAddressText().isBlank()) {
                throw new IllegalArgumentException("Vui lòng điền đầy đủ thông tin bắt buộc (Tên, SĐT, Địa chỉ).");
            }

            addressService.addAddress(MOCK_USER_ID, newAddress);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ mới thành công!");
        } catch (IllegalArgumentException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm địa chỉ: " + e.getMessage());
        } catch (RuntimeException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi thêm địa chỉ.");
             // Log lỗi e ra console hoặc file log
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/user/profile/address/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long addressId, RedirectAttributes redirectAttributes) {
        try {
            addressService.deleteAddress(addressId, MOCK_USER_ID);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa địa chỉ thành công!");
        } catch (RuntimeException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa địa chỉ: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/user/profile/address/set-default/{id}")
    public String setDefaultAddress(@PathVariable("id") Long addressId, RedirectAttributes redirectAttributes) {
        try {
            addressService.setDefaultAddress(addressId, MOCK_USER_ID);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt địa chỉ mặc định thành công!");
        } catch (RuntimeException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đặt địa chỉ mặc định: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }

}