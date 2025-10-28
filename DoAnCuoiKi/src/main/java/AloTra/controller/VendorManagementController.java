package AloTra.controller;

import AloTra.Model.OrderDTO;
import AloTra.Model.ProductDTO;
import AloTra.Model.ShopDTO;
import AloTra.Model.VoucherDTO;
import AloTra.entity.Category;
import AloTra.services.*; // Import tất cả services
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid; // Import @Valid
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/vendor/management")
public class VendorManagementController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private ShopService shopService;
    @Autowired private OrderService orderService;
    @Autowired private VoucherService voucherService;

    // --- USER ID GIẢ LẬP ---
    private static final Long MOCK_USER_ID = 8L; // ID của vendor1

    @GetMapping
    public String showManagementPage(@RequestParam(name = "tab", defaultValue = "info") String activeTab,
                                     // Params cho phân trang Sản phẩm
                                     @RequestParam(name = "page", defaultValue = "0") int productPageNum,
                                     @RequestParam(name = "size", defaultValue = "10") int productPageSize,
                                     @RequestParam(name = "sort", defaultValue = "createdAt,desc") String productSort,
                                     // Params cho phân trang Đơn hàng
                                     @RequestParam(name = "orderStatus", required = false) String orderStatusFilter,
                                     @RequestParam(name = "orderPage", defaultValue = "0") int orderPageNum,
                                     @RequestParam(name = "orderSize", defaultValue = "10") int orderPageSize,
                                     // Params cho phân trang Voucher
                                     @RequestParam(name = "voucherPage", defaultValue = "0") int voucherPageNum,
                                     @RequestParam(name = "voucherSize", defaultValue = "10") int voucherPageSize,
                                     Model model, HttpServletRequest request) {

        model.addAttribute("currentUri", request.getServletPath());

        // Luôn thêm shopDTO (dù trống) vào model cho form đăng ký
        if (!model.containsAttribute("shopDTO")) {
            model.addAttribute("shopDTO", new ShopDTO());
        }

        Optional<ShopDTO> shopOpt = shopService.getShopByUserId(MOCK_USER_ID);

        // --- TRƯỜNG HỢP CHƯA CÓ SHOP ---
        if (shopOpt.isEmpty()) {
            model.addAttribute("hasShop", false);
            return "vendor/management"; // Hiển thị form đăng ký
        }

        // --- TRƯỜNG HỢP ĐÃ CÓ SHOP ---
        ShopDTO shopInfo = shopOpt.get();
        // Luôn thêm shopInfo vào model khi đã có shop (cho modal sửa và hiển thị)
        // Dùng tên shopInfoError nếu có lỗi redirect từ form sửa shop
        if (!model.containsAttribute("shopInfoError") && !model.containsAttribute("shopInfo")) {
             model.addAttribute("shopInfo", shopInfo);
        } else if (model.containsAttribute("shopInfoError")) {
             model.addAttribute("shopInfo", model.getAttribute("shopInfoError")); // Ưu tiên hiển thị lại dữ liệu lỗi
        }

        model.addAttribute("currentShopId", shopInfo.getId()); // Cho WebSocket
        model.addAttribute("hasShop", true);
        model.addAttribute("activeTab", activeTab);

        // Lấy danh mục chung cho các form
        List<Category> categories = categoryService.getActiveCategories();
        model.addAttribute("categories", categories);

        // --- Dữ liệu cho tab Sản Phẩm ---
        // Chuẩn bị DTO trống (chỉ khi chưa có từ redirect lỗi)
        if (!model.containsAttribute("newProduct")) { model.addAttribute("newProduct", new ProductDTO()); }
        if (!model.containsAttribute("editProduct")) { model.addAttribute("editProduct", new ProductDTO()); }
        // Lấy dữ liệu trang sản phẩm
        if ("products".equals(activeTab) || !model.containsAttribute("productsPage")) {
            String[] sortParams = productSort.split(",");
            String sortField = sortParams.length > 0 ? sortParams[0] : "createdAt";
            Sort.Direction sortDirection = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort productSortOrder = Sort.by(sortDirection, sortField);
            model.addAttribute("currentProductSort", productSort);
            Pageable productsPageable = PageRequest.of(productPageNum, productPageSize, productSortOrder);
            Page<ProductDTO> productsPage = productService.getProductsByShopId(shopInfo.getId(), productsPageable);
            model.addAttribute("productsPage", productsPage);
        }

        // --- Dữ liệu cho tab Đơn Hàng ---
        if ("orders".equals(activeTab) || !model.containsAttribute("ordersPage")) {
             model.addAttribute("currentOrderStatus", orderStatusFilter);
             Pageable ordersPageable = PageRequest.of(orderPageNum, orderPageSize, Sort.by("createdAt").descending());
             Page<OrderDTO> ordersPage = orderService.findShopOrders(shopInfo.getId(), orderStatusFilter, ordersPageable);
             model.addAttribute("ordersPage", ordersPage);
             List<String> orderStatuses = List.of("PENDING", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED", "RETURNED");
             model.addAttribute("orderStatuses", orderStatuses);
        }

        // --- Dữ liệu cho tab Khuyến Mãi (Voucher) ---
        if (!model.containsAttribute("newVoucher")) { model.addAttribute("newVoucher", new VoucherDTO()); }
        if (!model.containsAttribute("editVoucher")) { model.addAttribute("editVoucher", new VoucherDTO()); }
        if ("vouchers".equals(activeTab) || !model.containsAttribute("vouchersPage")) {
            Pageable vouchersPageable = PageRequest.of(voucherPageNum, voucherPageSize, Sort.by("endDate").ascending());
            Page<VoucherDTO> vouchersPage = voucherService.findShopVouchers(shopInfo.getId(), vouchersPageable);
            model.addAttribute("vouchersPage", vouchersPage);
        }

        // --- Dữ liệu cho tab Doanh Thu ---
        if ("revenue".equals(activeTab) || !model.containsAttribute("revenueData")) {
            Map<String, Double> revenueData = new HashMap<>();
            double totalRevenue = orderService.getTotalRevenueForShop(shopInfo.getId());
            revenueData.put("totalRevenue", totalRevenue);
            model.addAttribute("revenueData", revenueData);
        }

        // --- Logic đánh dấu mở modal khi có lỗi redirect ---
        model.addAttribute("openEditModal", model.containsAttribute("shopInfoError")); // Edit Shop
        model.addAttribute("openEditProductModal", model.containsAttribute("productIdWithError") ? model.getAttribute("productIdWithError") : false); // Edit Product
        model.addAttribute("openAddVoucherModal", model.containsAttribute("newVoucherError")); // Add Voucher
        model.addAttribute("openEditVoucherModal", model.containsAttribute("voucherIdWithError") ? model.getAttribute("voucherIdWithError") : false); // Edit Voucher

        // Gửi lại DTO lỗi cho form tương ứng (nếu có)
        if (model.containsAttribute("newVoucherError")) { model.addAttribute("newVoucher", model.getAttribute("newVoucherError")); }
        if (model.containsAttribute("editVoucherError")) { model.addAttribute("editVoucher", model.getAttribute("editVoucherError")); }

        return "vendor/management";
    }


    // --- CÁC HÀM XỬ LÝ ĐĂNG KÝ SHOP ---
    @PostMapping("/shop/register")
    public String handleShopRegistration(
            @Valid @ModelAttribute("shopDTO") ShopDTO shopDTO,
            BindingResult bindingResult,
            @RequestParam(name = "logoFile", required = false) MultipartFile logoFile,
            @RequestParam(name = "bannerFile", required = false) MultipartFile bannerFile,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
             redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.shopDTO", bindingResult);
             redirectAttributes.addFlashAttribute("shopDTO", shopDTO);
             return "redirect:/vendor/management?error=register";
        }
        try {
            shopService.registerShop(MOCK_USER_ID, shopDTO, logoFile, bannerFile);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký cửa hàng thành công! Vui lòng chờ duyệt.");
            return "redirect:/vendor/management";
        } catch (IOException | RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn.");
        }
        redirectAttributes.addFlashAttribute("shopDTO", shopDTO);
        return "redirect:/vendor/management?error=register";
    }


    // --- CÁC HÀM XỬ LÝ UPDATE SHOP ---
    @PostMapping("/shop/update-info")
    public String handleUpdateShopInfo(
            @Valid @ModelAttribute("shopInfo") ShopDTO shopDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
        if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
        Long shopId = currentShopOpt.get().getId();

        if (bindingResult.hasErrors()) {
             redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.shopInfo", bindingResult);
             redirectAttributes.addFlashAttribute("shopInfoError", shopDTO); // Gửi lại dữ liệu lỗi
             // model.addAttribute("openEditModal", true); // Không cần set trực tiếp ở đây
             return "redirect:/vendor/management?tab=info&error=editShop";
        }
        try {
            shopService.updateShopInfo(shopId, MOCK_USER_ID, shopDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin shop thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
            redirectAttributes.addFlashAttribute("shopInfoError", shopDTO); // Gửi lại dữ liệu lỗi
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.shopInfo", bindingResult); // Gửi lại lỗi binding nếu có
            // model.addAttribute("openEditModal", true); // Không cần set trực tiếp ở đây
             return "redirect:/vendor/management?tab=info&error=editShop";
        }
        return "redirect:/vendor/management?tab=info";
    }

    @PostMapping("/shop/update-logo")
    public String handleUpdateShopLogo(@RequestParam("logoFile") MultipartFile logoFile,
                                        RedirectAttributes redirectAttributes) {
        Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
        if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
        Long shopId = currentShopOpt.get().getId();

        if (logoFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file logo.");
            redirectAttributes.addFlashAttribute("shopInfoError", currentShopOpt.get()); // Gửi lại info hiện tại
            // model.addAttribute("openEditModal", true);
            return "redirect:/vendor/management?tab=info&error=editShop";
        }

        try {
            shopService.updateShopLogo(shopId, MOCK_USER_ID, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật logo thành công!");
        } catch (IOException | RuntimeException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật logo: " + e.getMessage());
             redirectAttributes.addFlashAttribute("shopInfoError", currentShopOpt.get());
             // model.addAttribute("openEditModal", true);
             return "redirect:/vendor/management?tab=info&error=editShop";
        }
        return "redirect:/vendor/management?tab=info";
    }

    @PostMapping("/shop/update-banner")
    public String handleUpdateShopBanner(@RequestParam("bannerFile") MultipartFile bannerFile,
                                          RedirectAttributes redirectAttributes) {
         Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
         if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
         Long shopId = currentShopOpt.get().getId();

         if (bannerFile.isEmpty()) {
             redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file banner.");
             redirectAttributes.addFlashAttribute("shopInfoError", currentShopOpt.get());
             // model.addAttribute("openEditModal", true);
             return "redirect:/vendor/management?tab=info&error=editShop";
         }

        try {
            shopService.updateShopBanner(shopId, MOCK_USER_ID, bannerFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật banner thành công!");
        } catch (IOException | RuntimeException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật banner: " + e.getMessage());
             redirectAttributes.addFlashAttribute("shopInfoError", currentShopOpt.get());
             // model.addAttribute("openEditModal", true);
             return "redirect:/vendor/management?tab=info&error=editShop";
        }
        return "redirect:/vendor/management?tab=info";
    }


    // --- CÁC HÀM XỬ LÝ SẢN PHẨM ---

    @PostMapping("/products/add")
    public String handleAddProduct(
            @Valid @ModelAttribute("newProduct") ProductDTO productDTO,
            BindingResult bindingResult,
            @RequestParam("imageFiles") List<MultipartFile> imageFiles,
            RedirectAttributes redirectAttributes) {

        Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
        if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
        Long shopId = currentShopOpt.get().getId();

        validateProductDTOManual(productDTO, bindingResult);

        if (bindingResult.hasErrors()) {
             redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newProduct", bindingResult);
             redirectAttributes.addFlashAttribute("newProduct", productDTO); // Gửi lại DTO lỗi
             return "redirect:/vendor/management?tab=products&error=add";
        }

        try {
            productService.addProduct(shopId, productDTO, imageFiles);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return "redirect:/vendor/management?tab=products";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi upload ảnh: " + e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm sản phẩm: " + e.getMessage());
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn.");
        }

        redirectAttributes.addFlashAttribute("newProduct", productDTO); // Giữ lại dữ liệu khi lỗi
        return "redirect:/vendor/management?tab=products&error=add";
    }

     @GetMapping("/products/{id}/edit")
     @ResponseBody
     public ResponseEntity<ProductDTO> getProductForEdit(@PathVariable Long id) {
         Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
         if (currentShopOpt.isEmpty()) { return ResponseEntity.status(403).build(); }
         Long shopId = currentShopOpt.get().getId();

         Optional<ProductDTO> productOpt = productService.getEditProductDetails(id, shopId);
         return productOpt.map(ResponseEntity::ok)
                          .orElseGet(() -> ResponseEntity.notFound().build());
     }


     @PostMapping("/products/update/{id}")
     public String handleUpdateProduct(
             @PathVariable Long id,
             @Valid @ModelAttribute("editProduct") ProductDTO productDTO,
             BindingResult bindingResult,
             @RequestParam("editImageFiles") List<MultipartFile> imageFiles,
             RedirectAttributes redirectAttributes) {

         Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
         if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
         Long shopId = currentShopOpt.get().getId();

         validateProductDTOManual(productDTO, bindingResult);

         if (bindingResult.hasErrors()) {
             redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editProduct", bindingResult);
             productDTO.setId(id); // **Quan trọng: Đặt lại ID**
             redirectAttributes.addFlashAttribute("editProduct", productDTO);
             redirectAttributes.addFlashAttribute("productIdWithError", id);
             return "redirect:/vendor/management?tab=products&error=edit";
         }

         try {
             System.out.println("--- DEBUG: Calling updateProduct --- | ProductId: " + id + " | ShopId: " + shopId); // Log ID
             productService.updateProduct(id, shopId, productDTO, imageFiles);
             redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
             return "redirect:/vendor/management?tab=products";
         } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi upload ảnh: " + e.getMessage());
             System.err.println("--- IO ERROR in handleUpdateProduct ---"); e.printStackTrace();
         } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
             System.err.println("--- RUNTIME ERROR in handleUpdateProduct ---"); e.printStackTrace();
         } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định khi cập nhật sản phẩm.");
             System.err.println("--- UNEXPECTED ERROR in handleUpdateProduct ---"); e.printStackTrace();
         }

         // Xử lý lỗi đầy đủ
         productDTO.setId(id); // **Quan trọng: Đặt lại ID**
         redirectAttributes.addFlashAttribute("editProduct", productDTO);
         redirectAttributes.addFlashAttribute("productIdWithError", id);
         if(bindingResult.hasErrors()){
             redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editProduct", bindingResult);
         }
         return "redirect:/vendor/management?tab=products&error=edit";
     }

     @PostMapping("/products/delete/{id}")
     public String handleDeleteProduct(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
         Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
         if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
         Long shopId = currentShopOpt.get().getId();

         try {
             productService.deleteProduct(id, shopId);
             redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
         } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa sản phẩm: " + e.getMessage());
         } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi xóa.");
         }
         return "redirect:/vendor/management?tab=products";
     }


    // --- CÁC HÀM XỬ LÝ VOUCHER ---
    @PostMapping("/vouchers/add")
    public String handleAddVoucher(
            @Valid @ModelAttribute("newVoucher") VoucherDTO voucherDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
        if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
        Long shopId = currentShopOpt.get().getId();

        if (voucherDTO.getEndDate() != null && voucherDTO.getStartDate() != null && voucherDTO.getEndDate().isBefore(voucherDTO.getStartDate())) {
             bindingResult.rejectValue("endDate", "endDate.before.startDate", "Ngày kết thúc phải sau ngày bắt đầu.");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newVoucher", bindingResult);
            redirectAttributes.addFlashAttribute("newVoucherError", voucherDTO); // Gửi lại DTO lỗi
            // model.addAttribute("openAddVoucherModal", true); // Không dùng model trực tiếp
            return "redirect:/vendor/management?tab=vouchers&error=addVoucher";
        }

        try {
             voucherService.createShopVoucher(shopId, voucherDTO);
             redirectAttributes.addFlashAttribute("successMessage", "Tạo voucher thành công!");
             return "redirect:/vendor/management?tab=vouchers";
         } catch (RuntimeException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo voucher: " + e.getMessage());
             redirectAttributes.addFlashAttribute("newVoucherError", voucherDTO);
             redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newVoucher", bindingResult);
             // model.addAttribute("openAddVoucherModal", true);
             return "redirect:/vendor/management?tab=vouchers&error=addVoucher";
         } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi tạo voucher.");
             redirectAttributes.addFlashAttribute("newVoucherError", voucherDTO);
             // model.addAttribute("openAddVoucherModal", true);
             return "redirect:/vendor/management?tab=vouchers&error=addVoucher";
         }
    }

    @GetMapping("/vouchers/{id}/edit")
    @ResponseBody
    public ResponseEntity<VoucherDTO> getVoucherForEdit(@PathVariable Long id) {
        Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
        if (currentShopOpt.isEmpty()) { return ResponseEntity.status(403).build(); }
        Long shopId = currentShopOpt.get().getId();

        Optional<VoucherDTO> voucherOpt = voucherService.getShopVoucherForEdit(id, shopId);
        return voucherOpt.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/vouchers/update/{id}")
    public String handleUpdateVoucher(
            @PathVariable Long id,
            @Valid @ModelAttribute("editVoucher") VoucherDTO voucherDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
        if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
        Long shopId = currentShopOpt.get().getId();

        if (voucherDTO.getEndDate() != null && voucherDTO.getStartDate() != null && voucherDTO.getEndDate().isBefore(voucherDTO.getStartDate())) {
             bindingResult.rejectValue("endDate", "endDate.before.startDate", "Ngày kết thúc phải sau ngày bắt đầu.");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editVoucher", bindingResult);
            voucherDTO.setId(id); // **Quan trọng: Đặt lại ID**
            redirectAttributes.addFlashAttribute("editVoucherError", voucherDTO);
            redirectAttributes.addFlashAttribute("voucherIdWithError", id);
            // model.addAttribute("openEditVoucherModal", true);
            return "redirect:/vendor/management?tab=vouchers&error=editVoucher";
        }

        try {
            System.out.println("--- DEBUG: Calling updateShopVoucher --- | VoucherId: " + id + " | ShopId: " + shopId); // Log ID
            voucherService.updateShopVoucher(id, shopId, voucherDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
            return "redirect:/vendor/management?tab=vouchers";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật voucher: " + e.getMessage());
            System.err.println("--- RUNTIME ERROR in handleUpdateVoucher ---"); e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi cập nhật voucher.");
             System.err.println("--- UNEXPECTED ERROR in handleUpdateVoucher ---"); e.printStackTrace();
        }

        // Xử lý lỗi đầy đủ
        voucherDTO.setId(id); // **Quan trọng: Đặt lại ID**
        redirectAttributes.addFlashAttribute("editVoucherError", voucherDTO);
        redirectAttributes.addFlashAttribute("voucherIdWithError", id);
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.editVoucher", bindingResult);
        }
        // model.addAttribute("openEditVoucherModal", true);
        return "redirect:/vendor/management?tab=vouchers&error=editVoucher";
    }

    @PostMapping("/vouchers/delete/{id}")
    public String handleDeleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<ShopDTO> currentShopOpt = shopService.getShopByUserId(MOCK_USER_ID);
        if (currentShopOpt.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Shop không tồn tại."); return "redirect:/vendor/management"; }
        Long shopId = currentShopOpt.get().getId();

        try {
            voucherService.deleteShopVoucher(id, shopId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa voucher thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa voucher: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi xóa voucher.");
        }
        return "redirect:/vendor/management?tab=vouchers";
    }


    // --- HÀM VALIDATE THỦ CÔNG ---
    private void validateProductDTOManual(ProductDTO productDTO, BindingResult bindingResult) {
        if (productDTO.getName() == null || productDTO.getName().isBlank()) { bindingResult.rejectValue("name", "NotBlank", "Tên sản phẩm không được để trống."); }
        if (productDTO.getCategoryId() == null) { bindingResult.rejectValue("categoryId", "NotNull", "Vui lòng chọn danh mục."); }
        if (productDTO.getBasePrice() == null || productDTO.getBasePrice() <= 0) { bindingResult.rejectValue("basePrice", "Positive", "Giá bán phải lớn hơn 0."); }
        if (productDTO.getStockQuantity() == null || productDTO.getStockQuantity() < 0) { bindingResult.rejectValue("stockQuantity", "Min", "Số lượng tồn kho không được âm."); }
    }

}

