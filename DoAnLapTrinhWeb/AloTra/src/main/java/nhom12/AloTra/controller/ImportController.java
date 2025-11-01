package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.ImportDetail;
import nhom12.AloTra.entity.Import;
import nhom12.AloTra.repository.ProductRepository;
import nhom12.AloTra.repository.SupplierRepository;
import nhom12.AloTra.request.ImportDetailRequest;
import nhom12.AloTra.request.ImportRequest;
import nhom12.AloTra.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/import") // Sử dụng "/import" như bạn yêu cầu
public class ImportController {

    @Autowired
    private ImportService importService;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public String listImports(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer supplierId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size,
                              Model model) {
        Page<Import> importPage = importService.findAll(keyword, supplierId, page, size);
        model.addAttribute("importPage", importPage);
        model.addAttribute("suppliers", supplierRepository.findAll(Sort.by("tenNCC")));
        model.addAttribute("keyword", keyword);
        model.addAttribute("supplierId", supplierId);
        return "admin/warehouse/imports"; // Trả về file view mới
    }

    @GetMapping("/{id}")
    public String viewImportDetail(@PathVariable int id, Model model) {
        Import phieuNhap = importService.findById(id);
        if (phieuNhap == null) {
            return "redirect:/admin/import";
        }
        model.addAttribute("receipt", phieuNhap);
        return "admin/warehouse/importDetail";
    }

    // ✅ Hiển thị form Thêm mới
    @GetMapping("/add")
    public String showAddForm(Model model,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer supplierId,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size) {
        model.addAttribute("importRequest", new ImportRequest());
        model.addAttribute("suppliers", supplierRepository.findAll(Sort.by("tenNCC")));
        model.addAttribute("products", productRepository.findAll(Sort.by("tenSanPham")));
        model.addAttribute("keyword", keyword);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/warehouse/importForm"; // Trỏ đến form chung
    }

    // ✅ Hiển thị form Sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer supplierId,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size) {
        Import phieuNhap = importService.findById(id);

        // Chuyển đổi Entity sang DTO để hiển thị
        ImportRequest request = new ImportRequest();
        request.setMaPhieuNhap(phieuNhap.getMaPhieuNhap());
        request.setMaNCC(phieuNhap.getNhaCungCap().getMaNCC());

        List<ImportDetailRequest> detailRequests = new ArrayList<>();
        for (ImportDetail detail : phieuNhap.getChiTietPhieuNhapList()) {
            ImportDetailRequest detailDto = new ImportDetailRequest();
            detailDto.setMaSanPham(detail.getSanPham().getMaSanPham());
            detailDto.setSoLuong(detail.getSoLuong());
            detailDto.setGiaNhap(detail.getGiaNhap());
            detailRequests.add(detailDto);
        }
        request.setChiTietPhieuNhapList(detailRequests);

        model.addAttribute("importRequest", request);
        model.addAttribute("suppliers", supplierRepository.findAll(Sort.by("tenNCC")));
        model.addAttribute("products", productRepository.findAll(Sort.by("tenSanPham")));
        model.addAttribute("keyword", keyword);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/warehouse/importForm"; // Dùng chung form
    }

    @PostMapping("/save")
    public String saveImport(@Valid @ModelAttribute ImportRequest importRequest,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer supplierId,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "5") int size) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();

            model.addAttribute("errorMessage", errorMessage);

            model.addAttribute("suppliers", supplierRepository.findAll(Sort.by("tenNCC")));
            model.addAttribute("products", productRepository.findAll(Sort.by("tenSanPham")));
            model.addAttribute("keyword", keyword);
            model.addAttribute("supplierId", supplierId);
            model.addAttribute("page", page);
            model.addAttribute("size", size);

            return "admin/warehouse/importForm";
        }

        try {
            importService.save(importRequest);

            redirectAttributes.addFlashAttribute("successMessage", "Lưu phiếu nhập thành công!");
            redirectAttributes.addAttribute("keyword", keyword);
            redirectAttributes.addAttribute("supplierId", supplierId);
            redirectAttributes.addAttribute("page", page);
            redirectAttributes.addAttribute("size", size);
            return "redirect:/admin/import";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("suppliers", supplierRepository.findAll(Sort.by("tenNCC")));
            model.addAttribute("products", productRepository.findAll(Sort.by("tenSanPham")));
            model.addAttribute("keyword", keyword);
            model.addAttribute("supplierId", supplierId);
            model.addAttribute("page", page);
            model.addAttribute("size", size);

            return "admin/warehouse/importForm";
        }
    }

    // ✅ Thêm phương thức Xóa
    @GetMapping("/delete/{id}")
    public String deleteImport(@PathVariable int id,
                               RedirectAttributes redirectAttributes,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer supplierId,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size) {
        importService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa phiếu nhập thành công!");
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("supplierId", supplierId);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/import";
    }
}
