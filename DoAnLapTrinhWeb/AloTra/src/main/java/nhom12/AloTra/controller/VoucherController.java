package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.Voucher;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.request.VoucherRequest;
import nhom12.AloTra.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public String listVouchers(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false) Integer filterKieuApDung,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        Page<Voucher> voucherPage = voucherService.findAll(keyword, status, filterKieuApDung, page, size);
        model.addAttribute("voucherPage", voucherPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("filterKieuApDung", filterKieuApDung);
        return "admin/orders/vouchers";
    }

    @GetMapping("/{id}")
    public String viewVoucher(@PathVariable String id, Model model) {
        model.addAttribute("voucher", voucherService.findById(id));
        return "admin/orders/voucherDetail";
    }

    @GetMapping({"/add", "/edit/{id}"})
    public String showVoucherForm(@PathVariable(required = false) String id,
                                  Model model,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) Integer filterKieuApDung) {
        VoucherRequest request = new VoucherRequest();
        boolean isEditMode = false;

        if (id != null) {
            isEditMode = true;
            Voucher voucher = voucherService.findById(id);
            request.setMaKhuyenMai(voucher.getMaKhuyenMai());
            request.setTenChienDich(voucher.getTenChienDich());
            request.setKieuApDung(voucher.getKieuApDung());
            request.setGiaTri(voucher.getGiaTri());
            request.setBatDauLuc(voucher.getBatDauLuc());
            request.setKetThucLuc(voucher.getKetThucLuc());
            request.setTongTienToiThieu(voucher.getTongTienToiThieu());
            request.setGiamToiDa(voucher.getGiamToiDa());
            request.setGioiHanTongSoLan(voucher.getGioiHanTongSoLan());
            request.setGioiHanMoiNguoi(voucher.getGioiHanMoiNguoi());
            request.setTrangThai(voucher.getTrangThai());
        }
        model.addAttribute("isEditMode", isEditMode);
        model.addAttribute("voucherRequest", request);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("filterKieuApDung", filterKieuApDung);
        return "admin/orders/addOrEditVoucher";
    }

    @PostMapping("/save")
    public String saveVoucher(@Valid @ModelAttribute("voucherRequest") VoucherRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(defaultValue = "false") boolean isEditMode,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer status,
                              @RequestParam(required = false) Integer filterKieuApDung) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("isEditMode", isEditMode);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("filterKieuApDung", filterKieuApDung);
            return "admin/orders/addOrEditVoucher";
        }

        try {
            voucherService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu khuyến mãi thành công!");
            redirectAttributes.addAttribute("page", page)
                    .addAttribute("size", size)
                    .addAttribute("keyword", keyword)
                    .addAttribute("status", status)
                    .addAttribute("filterKieuApDung", filterKieuApDung);
            return "redirect:/admin/voucher";
        }
        catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEditMode", isEditMode);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("filterKieuApDung", filterKieuApDung);
            return "admin/orders/addOrEditVoucher";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable String id, RedirectAttributes redirectAttributes,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer status,
                                @RequestParam(required = false) Integer filterKieuApDung) {
        try {
            voucherService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa khuyến mãi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("page", page)
                .addAttribute("size", size)
                .addAttribute("keyword", keyword)
                .addAttribute("status", status)
                .addAttribute("filterKieuApDung", filterKieuApDung);
        return "redirect:/admin/voucher";
    }
}