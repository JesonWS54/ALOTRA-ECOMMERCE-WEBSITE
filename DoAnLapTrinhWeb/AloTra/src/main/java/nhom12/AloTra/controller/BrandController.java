package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.Brand;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.request.BrandRequest;
import nhom12.AloTra.service.BrandService;
import nhom12.AloTra.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @Autowired
    private StorageService storageService;

    @GetMapping
    public String listBrands(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Boolean status,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "5") int size,
                             Model model) {
        Page<Brand> brandPage = brandService.searchAndFilter(keyword, status, page, size);
        model.addAttribute("brandPage", brandPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        if (!model.containsAttribute("brand")) {
            model.addAttribute("brand", new BrandRequest());
        }
        return "admin/products/brands";
    }

    @PostMapping("/save")
    public String saveBrand(@Valid @ModelAttribute BrandRequest brandRequest,
                            BindingResult bindingResult,
                            @RequestParam("hinhAnhFile") MultipartFile hinhAnhFile,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "5") int size,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Boolean status,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("brand", brandRequest);
        }

        else {
            try {
                if (!hinhAnhFile.isEmpty()) {
                    String fileName = storageService.storeFile(hinhAnhFile, "brands");
                    brandRequest.setHinhAnh(fileName);
                }
                brandService.save(brandRequest);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu Cửa Hàng thành công!");
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("brand", brandRequest);
            }
        }

        // Giữ lại tham số phân trang và filter khi redirect
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        if (keyword != null) redirectAttributes.addAttribute("keyword", keyword);
        if (status != null) redirectAttributes.addAttribute("status", status);
        return "redirect:/admin/brand";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable int id,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Boolean status,
                              RedirectAttributes redirectAttributes) {
        try {
            brandService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa Cửa Hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        if (keyword != null) redirectAttributes.addAttribute("keyword", keyword);
        if (status != null) redirectAttributes.addAttribute("status", status);
        return "redirect:/admin/brand";
    }
}
