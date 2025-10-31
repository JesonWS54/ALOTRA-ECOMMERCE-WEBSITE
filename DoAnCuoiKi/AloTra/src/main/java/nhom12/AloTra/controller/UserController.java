package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.User;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.repository.MembershipTierRepository;
import nhom12.AloTra.repository.RoleRepository;
import nhom12.AloTra.request.UserRequest;
import nhom12.AloTra.service.StorageService;
import nhom12.AloTra.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired private
    RoleRepository roleRepository;
    @Autowired private
    MembershipTierRepository membershipTierRepository;
    @Autowired
    private StorageService storageService;

    @GetMapping
    public String listUsers(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Integer roleId,
                            @RequestParam(required = false) Integer tierId,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "5") int size,
                            Model model) {
        Page<User> userPage = userService.findAll(keyword, roleId, tierId, status, page, size);
        model.addAttribute("userPage", userPage);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("tiers", membershipTierRepository.findAll());

        // Giữ lại state cho bộ lọc
        model.addAttribute("keyword", keyword);
        model.addAttribute("roleId", roleId);
        model.addAttribute("tierId", tierId);
        model.addAttribute("status", status);

        return "admin/user/users";
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable int id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin/user";
        }
        model.addAttribute("user", user);
        return "admin/user/userDetail";
    }

    @GetMapping({"/add", "/edit/{id}"})
    public String showUserForm(@PathVariable(name = "id", required = false) Integer id,
                               Model model,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer roleId,
                               @RequestParam(required = false) Integer tierId,
                               @RequestParam(required = false) Integer status) {
        UserRequest userRequest = new UserRequest();
        if (id != null) { // Chế độ sửa
            User user = userService.findById(id);
            // Chuyển đổi Entity sang DTO
            userRequest.setMaNguoiDung(user.getMaNguoiDung());
            userRequest.setHoTen(user.getHoTen());
            userRequest.setEmail(user.getEmail());
            userRequest.setTenDangNhap(user.getTenDangNhap());
            userRequest.setSoDienThoai(user.getSoDienThoai());
            userRequest.setTrangThai(user.getTrangThai());
            userRequest.setMaVaiTro(user.getVaiTro().getMaVaiTro());
            userRequest.setAnhDaiDien(user.getAnhDaiDien());
            if(user.getHangThanhVien() != null){
                userRequest.setMaHangThanhVien(user.getHangThanhVien().getMaHangThanhVien());
            }
        }

        model.addAttribute("userRequest", userRequest);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("tiers", membershipTierRepository.findAll());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("roleId", roleId);
        model.addAttribute("tierId", tierId);
        model.addAttribute("status", status);
        return "admin/user/addOrEditUser";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("userRequest") UserRequest userRequest,
                           BindingResult bindingResult,
                           @RequestParam("anhDaiDienFile") MultipartFile anhDaiDienFile,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "5") int size,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Integer roleId,
                           @RequestParam(required = false) Integer tierId,
                           @RequestParam(required = false) Integer status) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("tiers", membershipTierRepository.findAll());
            return "admin/user/addOrEditUser";
        }
        try {
            if (!anhDaiDienFile.isEmpty()) {
                String fileName = storageService.storeFile(anhDaiDienFile, "avatars");
                userRequest.setAnhDaiDien(fileName);
            }

            userService.save(userRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu người dùng thành công!");
            redirectAttributes.addAttribute("page", page)
                    .addAttribute("size", size)
                    .addAttribute("keyword", keyword)
                    .addAttribute("roleId", roleId)
                    .addAttribute("tierId", tierId)
                    .addAttribute("status", status);
            return "redirect:/admin/user";
        }
        catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("tiers", membershipTierRepository.findAll());
            return "admin/user/addOrEditUser";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer roleId,
                             @RequestParam(required = false) Integer tierId,
                             @RequestParam(required = false) Integer status) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("page", page)
                .addAttribute("size", size)
                .addAttribute("keyword", keyword)
                .addAttribute("roleId", roleId)
                .addAttribute("tierId", tierId)
                .addAttribute("status", status);
        return "redirect:/admin/user";
    }
}
