package AloTra.controller;

import AloTra.Model.AccountDTO;
import AloTra.Model.CategoryDTO;
import AloTra.Model.ProductDTO;
import AloTra.Model.ShopDTO; // Import ShopDTO (có thể cần sau)
import AloTra.services.AccountService;
import AloTra.services.CategoryService;
import AloTra.services.ProductService;
import AloTra.services.ShopService; // Import ShopService (có thể cần sau)
// Import các Service khác nếu cần (ShippingCarrier, AppCommission, Voucher)
// import AloTra.services.ShippingCarrierService;
// import AloTra.services.AppCommissionService;
// import AloTra.services.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin") // Mapping gốc cho khu vực Admin
public class AdminController {

    @Autowired private AccountService accountService;
    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    // Inject các service khác khi cần
    // @Autowired private ShippingCarrierService shippingCarrierService;
    // @Autowired private AppCommissionService appCommissionService;
    // @Autowired private VoucherService voucherService;
    // @Autowired private ShopService shopService; // Có thể cần để lấy list shop

    // Giả lập Admin ID (sau này sẽ lấy từ Security Context)
    private static final Long MOCK_ADMIN_ID = 5L;

    @GetMapping("/dashboard")
    public String showAdminDashboard(
            @RequestParam(name = "tab", defaultValue = "accounts") String activeTab,
            // Params cho Accounts
            @RequestParam(name = "accountPage", defaultValue = "0") int accountPageNum,
            @RequestParam(name = "accountSize", defaultValue = "10") int accountPageSize,
            @RequestParam(name = "accountSort", defaultValue = "id,asc") String accountSort,
            // Params cho Products
            @RequestParam(name = "productPage", defaultValue = "0") int productPageNum,
            @RequestParam(name = "productSize", defaultValue = "10") int productPageSize,
            @RequestParam(name = "productSort", defaultValue = "id,asc") String productSort,
            // Params cho Categories
            @RequestParam(name = "categoryPage", defaultValue = "0") int categoryPageNum,
            @RequestParam(name = "categorySize", defaultValue = "10") int categoryPageSize,
            @RequestParam(name = "categorySort", defaultValue = "id,asc") String categorySort,
            // TODO: Thêm params cho các tab khác
            Model model, HttpServletRequest request
    ) {
        model.addAttribute("currentUri", request.getServletPath());
        model.addAttribute("activeTab", activeTab);

        // --- Dữ liệu cho tab Tài Khoản ---
        if (!model.containsAttribute("newAccount")) { model.addAttribute("newAccount", new AccountDTO()); }
        if (!model.containsAttribute("editAccount")) { model.addAttribute("editAccount", new AccountDTO()); }
        if ("accounts".equals(activeTab) || !model.containsAttribute("accountsPage")) {
            String[] sortParams = accountSort.split(",");
            Sort sort = Sort.by(Sort.Direction.fromString(sortParams.length > 1 ? sortParams[1] : "asc"), sortParams[0]);
            Pageable pageable = PageRequest.of(accountPageNum, accountPageSize, sort);
            Page<AccountDTO> accountsPage = accountService.getAllAccounts(pageable);
            model.addAttribute("accountsPage", accountsPage);
            model.addAttribute("currentAccountSort", accountSort);
        }

        // --- Dữ liệu cho tab Sản Phẩm ---
        if (!model.containsAttribute("editProductAdmin")) { model.addAttribute("editProductAdmin", new ProductDTO()); } // DTO cho modal edit admin
        if ("products".equals(activeTab) || !model.containsAttribute("allProductsPage")) {
            String[] sortParams = productSort.split(",");
            Sort sort = Sort.by(Sort.Direction.fromString(sortParams.length > 1 ? sortParams[1] : "asc"), sortParams[0]);
            Pageable pageable = PageRequest.of(productPageNum, productPageSize, sort);
            Page<ProductDTO> productsPage = productService.getAllProductsAdmin(pageable);
            model.addAttribute("allProductsPage", productsPage);
            model.addAttribute("currentProductSort", productSort);
            // Cần categories cho modal edit product
            List<Category> categories = categoryService.getActiveCategories();
            model.addAttribute("categories", categories);
        }

        // --- Dữ liệu cho tab Danh Mục ---
        if (!model.containsAttribute("newCategory")) { model.addAttribute("newCategory", new CategoryDTO()); }
        if (!model.containsAttribute("editCategory")) { model.addAttribute("editCategory", new CategoryDTO()); }
        if ("categories".equals(activeTab) || !model.containsAttribute("allCategoriesPage")) { // Sửa tên attribute
            String[] sortParams = categorySort.split(",");
            Sort sort = Sort.by(Sort.Direction.fromString(sortParams.length > 1 ? sortParams[1] : "asc"), sortParams[0]);
            Pageable pageable = PageRequest.of(categoryPageNum, categoryPageSize, sort);
            Page<CategoryDTO> categoriesPage = categoryService.getAllCategoriesAdmin(pageable);
            model.addAttribute("allCategoriesPage", categoriesPage); // Sửa tên attribute
            model.addAttribute("currentCategorySort", categorySort);
            // Lấy danh sách root categories cho dropdown parent
            List<CategoryDTO> rootCategories = categoryService.getRootCategoriesAdmin();
            model.addAttribute("rootCategories", rootCategories);
        }

        // --- Dữ liệu cho tab Nhà Vận Chuyển (Placeholder) ---
        if ("carriers".equals(activeTab)) {
            // TODO: Lấy dữ liệu nhà vận chuyển
        }

        // --- Dữ liệu cho tab Chiết Khấu (Placeholder) ---
        if ("commissions".equals(activeTab)) {
            // TODO: Lấy dữ liệu chiết khấu
        }

        // --- Dữ liệu cho tab Khuyến Mãi Admin (Placeholder) ---
        if ("vouchers".equals(activeTab)) {
            // TODO: Lấy dữ liệu khuyến mãi admin
        }

        // --- Logic mở modal khi lỗi redirect ---
        model.addAttribute("openAddAccountModal", model.containsAttribute("newAccountError"));
        model.addAttribute("openEditAccountModal", model.containsAttribute("accountIdWithError"));
        model.addAttribute("openEditProductAdminModal", model.containsAttribute("productIdWithError"));
        model.addAttribute("openAddCategoryModal", model.containsAttribute("newCategoryError"));
        model.addAttribute("openEditCategoryModal", model.containsAttribute("categoryIdWithError"));

        // Gửi lại DTO lỗi cho form tương ứng (nếu có)
        if (model.containsAttribute("newAccountError")) { model.addAttribute("newAccount", model.getAttribute("newAccountError")); }
        if (model.containsAttribute("editAccountError")) { model.addAttribute("editAccount", model.getAttribute("editAccountError")); }
        if (model.containsAttribute("editProductAdminError")) { model.addAttribute("editProductAdmin", model.getAttribute("editProductAdminError")); }
        if (model.containsAttribute("newCategoryError")) { model.addAttribute("newCategory", model.getAttribute("newCategoryError")); }
        if (model.containsAttribute("editCategoryError")) { model.addAttribute("editCategory", model.getAttribute("editCategoryError")); }


        return "admin/admin-dashboard"; // Trỏ về view dashboard
    }

    // --- ACTIONS FOR ACCOUNTS ---
    @PostMapping("/accounts/add")
    public String handleAddAccount(@Valid @ModelAttribute("newAccount") AccountDTO accountDTO,
                                   BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // Thêm validate username/email unique
        if (!bindingResult.hasErrors()) {
            if (accountService.existsByUsername(accountDTO.getUsername())) {
                bindingResult.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại.");
            }
            if (accountService.existsByEmail(accountDTO.getEmail())) {
                bindingResult.rejectValue("email", "email.exists", "Email đã tồn tại.");
            }
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newAccount", bindingResult);
            redirectAttributes.addFlashAttribute("newAccountError", accountDTO); // Gửi lại DTO lỗi
            return "redirect:/admin/dashboard?tab=accounts&error=addAccount";
        }
        try {
            accountService.addAccount(accountDTO); // Lưu plain text password (tạm thời)
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tài khoản thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm tài khoản: " + e.getMessage());
            redirectAttributes.addFlashAttribute("newAccountError", accountDTO); // Gửi lại DTO lỗi
            return "redirect:/admin/dashboard?tab=accounts&error=addAccount";
        }
        return "redirect:/admin/dashboard?tab=accounts";
    }

    @GetMapping("/accounts/{id}/edit")
    @ResponseBody
    public ResponseEntity<AccountDTO> getAccountForEdit(@PathVariable Long id) {
        Optional<AccountDTO> accountOpt = accountService.getAccountById(id);
        return accountOpt.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/accounts/update/{id}")
    public String handleUpdateAccount(@PathVariable Long id,
                                      @Valid @ModelAttribute("editAccount") AccountDTO accountDTO,
                                      BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // Không cho sửa username/email ở đây
        if (bindingResult.hasFieldErrors("fullName") || bindingResult.hasFieldErrors("phone") ||
            bindingResult.hasFieldErrors("role") || bindingResult.hasFieldErrors("isActive") ||
            bindingResult.hasFieldErrors("isLocked"))
        {
             // Chỉ kiểm tra lỗi các trường cho phép sửa
        } else {
             bindingResult.clear(); // Xóa lỗi các trường readonly nếu lỡ có
        }


        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editAccount", bindingResult);
            accountDTO.setId(id); // Đặt lại ID
            redirectAttributes.addFlashAttribute("editAccountError", accountDTO);
            redirectAttributes.addFlashAttribute("accountIdWithError", id);
            return "redirect:/admin/dashboard?tab=accounts&error=editAccount";
        }
        try {
            accountService.updateAccountAdmin(id, accountDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
            accountDTO.setId(id); // Đặt lại ID
            redirectAttributes.addFlashAttribute("editAccountError", accountDTO);
            redirectAttributes.addFlashAttribute("accountIdWithError", id);
            return "redirect:/admin/dashboard?tab=accounts&error=editAccount";
        }
        return "redirect:/admin/dashboard?tab=accounts";
    }

    @PostMapping("/accounts/lock/{id}")
    public String handleLockAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.lockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản #" + id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=accounts";
    }

    @PostMapping("/accounts/unlock/{id}")
    public String handleUnlockAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.unlockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản #" + id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=accounts";
    }

    @PostMapping("/accounts/delete/{id}")
    public String handleDeleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản #" + id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=accounts";
    }

    // --- ACTIONS FOR PRODUCTS (ADMIN) ---
    @GetMapping("/products/{id}/edit-admin") // Endpoint riêng cho admin edit
    @ResponseBody
    public ResponseEntity<ProductDTO> getProductForEditAdmin(@PathVariable Long id) {
        // Admin có thể xem chi tiết bất kỳ sản phẩm nào
        Optional<ProductDTO> productOpt = productService.getProductDetails(id); // Dùng hàm get chi tiết
        return productOpt.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/products/update-admin/{id}")
    public String handleUpdateProductAdmin(@PathVariable Long id,
                                           @Valid @ModelAttribute("editProductAdmin") ProductDTO productDTO,
                                           BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // Validate thủ công
        validateProductDTOManual(productDTO, bindingResult); // Hàm validate dùng chung

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editProductAdmin", bindingResult);
            productDTO.setId(id); // Đặt lại ID
            redirectAttributes.addFlashAttribute("editProductAdminError", productDTO);
            redirectAttributes.addFlashAttribute("productIdWithError", id); // ID để mở modal
            return "redirect:/admin/dashboard?tab=products&error=editProductAdmin";
        }
        try {
            // Gọi hàm update của Admin (chỉ cập nhật thông tin cơ bản, không xử lý ảnh)
            productService.updateProductAdmin(id, productDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
            productDTO.setId(id); // Đặt lại ID
            redirectAttributes.addFlashAttribute("editProductAdminError", productDTO);
            redirectAttributes.addFlashAttribute("productIdWithError", id);
            return "redirect:/admin/dashboard?tab=products&error=editProductAdmin";
        }
        return "redirect:/admin/dashboard?tab=products";
    }

    @PostMapping("/products/delete-admin/{id}")
    public String handleDeleteProductAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductAdmin(id); // Gọi hàm xóa của Admin
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=products";
    }


    // --- ACTIONS FOR CATEGORIES ---
    @PostMapping("/categories/add")
    public String handleAddCategory(@Valid @ModelAttribute("newCategory") CategoryDTO categoryDTO,
                                    BindingResult bindingResult,
                                    @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newCategory", bindingResult);
            redirectAttributes.addFlashAttribute("newCategoryError", categoryDTO);
            return "redirect:/admin/dashboard?tab=categories&error=addCategory";
        }
        try {
            categoryService.addCategoryAdmin(categoryDTO, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi upload ảnh: " + e.getMessage());
            redirectAttributes.addFlashAttribute("newCategoryError", categoryDTO);
            return "redirect:/admin/dashboard?tab=categories&error=addCategory";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm danh mục: " + e.getMessage());
            redirectAttributes.addFlashAttribute("newCategoryError", categoryDTO);
            return "redirect:/admin/dashboard?tab=categories&error=addCategory";
        }
        return "redirect:/admin/dashboard?tab=categories";
    }

    @GetMapping("/categories/{id}/edit")
    @ResponseBody
    public ResponseEntity<CategoryDTO> getCategoryForEdit(@PathVariable Long id) {
        Optional<CategoryDTO> categoryOpt = categoryService.getCategoryByIdAdmin(id);
        return categoryOpt.map(ResponseEntity::ok)
                          .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/categories/update/{id}")
    public String handleUpdateCategory(@PathVariable Long id,
                                       @Valid @ModelAttribute("editCategory") CategoryDTO categoryDTO,
                                       BindingResult bindingResult,
                                       @RequestParam(name = "editImageFile", required = false) MultipartFile imageFile,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editCategory", bindingResult);
            categoryDTO.setId(id); // Đặt lại ID
            redirectAttributes.addFlashAttribute("editCategoryError", categoryDTO);
            redirectAttributes.addFlashAttribute("categoryIdWithError", id);
            return "redirect:/admin/dashboard?tab=categories&error=editCategory";
        }
        try {
            categoryService.updateCategoryAdmin(id, categoryDTO, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi upload ảnh: " + e.getMessage());
            categoryDTO.setId(id);
            redirectAttributes.addFlashAttribute("editCategoryError", categoryDTO);
            redirectAttributes.addFlashAttribute("categoryIdWithError", id);
            return "redirect:/admin/dashboard?tab=categories&error=editCategory";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
            categoryDTO.setId(id);
            redirectAttributes.addFlashAttribute("editCategoryError", categoryDTO);
            redirectAttributes.addFlashAttribute("categoryIdWithError", id);
            return "redirect:/admin/dashboard?tab=categories&error=editCategory";
        }
        return "redirect:/admin/dashboard?tab=categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String handleDeleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategoryAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=categories";
    }


    // --- TODO: ACTIONS FOR Shipping Carriers ---
    // --- TODO: ACTIONS FOR App Commissions ---
    // --- TODO: ACTIONS FOR Admin Vouchers ---


    // --- HÀM VALIDATE THỦ CÔNG (Dùng chung cho Product) ---
    private void validateProductDTOManual(ProductDTO productDTO, BindingResult bindingResult) {
        if (productDTO.getName() == null || productDTO.getName().isBlank()) { bindingResult.rejectValue("name", "NotBlank", "Tên sản phẩm không được để trống."); }
        if (productDTO.getCategoryId() == null) { bindingResult.rejectValue("categoryId", "NotNull", "Vui lòng chọn danh mục."); }
        if (productDTO.getBasePrice() == null || productDTO.getBasePrice() <= 0) { bindingResult.rejectValue("basePrice", "Positive", "Giá bán phải lớn hơn 0."); }
        if (productDTO.getStockQuantity() == null || productDTO.getStockQuantity() < 0) { bindingResult.rejectValue("stockQuantity", "Min", "Số lượng tồn kho không được âm."); }
    }
}

