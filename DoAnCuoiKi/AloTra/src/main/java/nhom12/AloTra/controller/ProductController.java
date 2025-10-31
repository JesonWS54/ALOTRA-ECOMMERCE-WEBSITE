package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.Rating;
import nhom12.AloTra.entity.Product;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.repository.BrandRepository;
import nhom12.AloTra.repository.CategoryRepository;
import nhom12.AloTra.repository.RatingRepository;
import nhom12.AloTra.request.ProductRequest;
import nhom12.AloTra.service.ProductService;
import nhom12.AloTra.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private RatingRepository danhGiaRepository;

    @GetMapping
    public String listProducts(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Boolean status,
                               @RequestParam(required = false) Integer categoryId,
                               @RequestParam(required = false) Integer brandId,
                               @RequestParam(required = false) String sort,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        // ✅ Truyền các tham số mới vào service
        Page<Product> productPage = productService.searchProducts(keyword, status, categoryId, brandId, sort, page, size);

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());

        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sort", sort);
        return "admin/products/products";
    }

    @GetMapping("/{id}")
    public String viewProductDetail(@PathVariable("id") int id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/admin/product";
        }
        List<Rating> reviews = danhGiaRepository.findBySanPham_MaSanPhamOrderByNgayTaoDesc(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        return "admin/products/productDetail";
    }

    @GetMapping({"/add", "/edit/{id}"})
    public String showProductForm(@PathVariable(name = "id", required = false) Integer id, Model model,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Boolean status,
                                  @RequestParam(required = false) Integer categoryId,
                                  @RequestParam(required = false) Integer brandId,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "5") int size) {
        ProductRequest productRequest = new ProductRequest();
        if (id != null) { // Nếu có ID -> Chế độ Sửa
            Product product = productService.findById(id);
            // Chuyển đổi Entity sang DTO
            productRequest.setMaSanPham(product.getMaSanPham());
            productRequest.setTenSanPham(product.getTenSanPham());
            productRequest.setMoTa(product.getMoTa());
            productRequest.setGiaBan(product.getGiaBan());
            productRequest.setGiaNiemYet(product.getGiaNiemYet());
            productRequest.setHanSuDung(product.getHanSuDung());
            productRequest.setHinhAnh(product.getHinhAnh());
            productRequest.setKichHoat(product.isKichHoat());
            productRequest.setMaDanhMuc(product.getDanhMuc().getMaDanhMuc());
            productRequest.setMaThuongHieu(product.getThuongHieu().getMaThuongHieu());
        }

        model.addAttribute("product", productRequest);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());

        // Giữ lại state của trang danh sách
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sort", sort);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/products/addOrEditProduct";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") ProductRequest productRequest,
                              BindingResult bindingResult,
                              @RequestParam("hinhAnhFile") MultipartFile hinhAnhFile,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Boolean status,
                              @RequestParam(required = false) Integer categoryId,
                              @RequestParam(required = false) Integer brandId,
                              @RequestParam(required = false) String sort,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            return "admin/products/addOrEditProduct";
        }

        try {
            if (!hinhAnhFile.isEmpty()) {
                String fileName = storageService.storeFile(hinhAnhFile, "products");
                productRequest.setHinhAnh(fileName);
            }
            productService.save(productRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu sản phẩm thành công!");
            redirectAttributes.addAttribute("keyword", keyword);
            redirectAttributes.addAttribute("status", status);
            redirectAttributes.addAttribute("categoryId", categoryId);
            redirectAttributes.addAttribute("brandId", brandId);
            redirectAttributes.addAttribute("sort", sort);
            redirectAttributes.addAttribute("page", page);
            redirectAttributes.addAttribute("size", size);
            return "redirect:/admin/product";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            return "admin/products/addOrEditProduct";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id, RedirectAttributes redirectAttributes,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Boolean status,
                                @RequestParam(required = false) Integer categoryId,
                                @RequestParam(required = false) Integer brandId,
                                @RequestParam(required = false) String sort,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("status", status);
        redirectAttributes.addAttribute("categoryId", categoryId);
        redirectAttributes.addAttribute("brandId", brandId);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/product";
    }
}
