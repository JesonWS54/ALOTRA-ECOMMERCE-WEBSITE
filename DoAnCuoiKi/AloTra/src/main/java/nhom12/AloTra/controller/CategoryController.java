package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.Category;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.request.CategoryRequest;
import nhom12.AloTra.request.ShippingCarrierRequest;
import nhom12.AloTra.service.CategoryService;
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
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StorageService storageService; // Tiêm StorageService

    @GetMapping
    public String listCategories(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Boolean status,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 Model model) {
        Page<Category> categoryPage = categoryService.searchAndFilter(keyword, status, page, size);
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new CategoryRequest());
        }
        return "admin/products/categories";
    }

    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute CategoryRequest categoryRequest,
                               BindingResult bindingResult,
                               @RequestParam("hinhAnhFile") MultipartFile hinhAnhFile,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Boolean status,
                               RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("category", categoryRequest);
        }
        else {
            try {
                if (!hinhAnhFile.isEmpty()) {
                    String fileName = storageService.storeFile(hinhAnhFile, "categories");
                    categoryRequest.setHinhAnh(fileName);
                }
                categoryService.save(categoryRequest);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công!");
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("category", categoryRequest);
            }
        }

        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        if (keyword != null) redirectAttributes.addAttribute("keyword", keyword);
        if (status != null) redirectAttributes.addAttribute("status", status);
        return "redirect:/admin/category";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable int id,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Boolean status,
                                 RedirectAttributes redirectAttributes){
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        if (keyword != null) redirectAttributes.addAttribute("keyword", keyword);
        if (status != null) redirectAttributes.addAttribute("status", status);
        return "redirect:/admin/category";
    }
}
