package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.Supplier;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.request.ShippingCarrierRequest;
import nhom12.AloTra.request.SupplierRequest;
import nhom12.AloTra.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/supplier")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public String listSuppliers(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size,
                                Model model) {
        Page<Supplier> supplierPage = supplierService.search(keyword, page, size);
        model.addAttribute("supplierPage", supplierPage);
        model.addAttribute("keyword", keyword);
        if (!model.containsAttribute("supplier")) {
            model.addAttribute("supplier", new SupplierRequest());
        }
        return "admin/warehouse/suppliers";
    }

    @PostMapping("/save")
    public String saveSupplier(@Valid @ModelAttribute SupplierRequest supplierRequest,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("supplier", supplierRequest);
        }
        else {
            try {
                supplierService.save(supplierRequest);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu nhà cung cấp thành công!");
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("supplier", supplierRequest);
            }
        }
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/supplier";
    }

    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable int id,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "5") int size) {
        try {
            supplierService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhà cung cấp thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/supplier";
    }
}
