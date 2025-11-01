package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.ShippingCarrier;
import nhom12.AloTra.entity.ShippingFee;
import nhom12.AloTra.exception.NotFoundException;
import nhom12.AloTra.repository.ShippingCarrierRepository;
import nhom12.AloTra.request.ShippingFeeRequest;
import nhom12.AloTra.service.ShippingCarrierService;
import nhom12.AloTra.service.ShippingFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/shipping-carrier/{carrierId}/fees")
public class ShippingFeeController {

    @Autowired
    private ShippingFeeService shippingFeeService;
    @Autowired
    private ShippingCarrierService shippingCarrierService;

    @GetMapping
    public String list(@PathVariable int carrierId, Model model) {
        ShippingCarrier carrier = shippingCarrierService.findById(carrierId);
        model.addAttribute("carrier", carrier);
        List<ShippingFee> feeList = shippingFeeService.findAllByProvider(carrierId);
        model.addAttribute("feeList", feeList);
        return "admin/system/shipping-fees";
    }

    @GetMapping({"/add", "/edit/{feeId}"})
    public String showForm(@PathVariable int carrierId,
                           @PathVariable(required = false) Integer feeId,
                           Model model) {
        ShippingFeeRequest request = new ShippingFeeRequest();
        if (feeId != null) {
            ShippingFee entity = shippingFeeService.findById(feeId);
            request.setMaChiPhiVC(entity.getMaChiPhiVC());
            request.setTenGoiCuoc(entity.getTenGoiCuoc());
            request.setMaNVC(entity.getNhaVanChuyen().getMaNVC());
            request.setPhuongThucVanChuyen(entity.getPhuongThucVanChuyen());
            request.setChiPhi(entity.getChiPhi());
            request.setNgayGiaoSomNhat(entity.getNgayGiaoSomNhat());
            request.setNgayGiaoMuonNhat(entity.getNgayGiaoMuonNhat());
            request.setDonViThoiGian(entity.getDonViThoiGian());
            request.setCacTinhApDung(entity.getCacTinhApDung().stream()
                    .map(tinh -> tinh.getId().getTenTinhThanh())
                    .collect(Collectors.toList()));
        } else {
            request.setMaNVC(carrierId);
        }

        model.addAttribute("shippingFeeRequest", request);
        return "admin/system/shipping-fee-form";
    }

    @PostMapping("/save")
    public String save(@PathVariable int carrierId,
                       @Valid @ModelAttribute("shippingFeeRequest") ShippingFeeRequest request,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        request.setMaNVC(carrierId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "admin/system/shipping-fee-form";
        }
        try {
            shippingFeeService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu gói phí vận chuyển thành công!");
            return "redirect:/admin/shipping-carrier/" + carrierId + "/fees";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/system/shipping-fee-form";
        }
    }

    @GetMapping("/delete/{feeId}")
    public String delete(@PathVariable int carrierId,
                         @PathVariable int feeId,
                         RedirectAttributes redirectAttributes) {
        try {
            shippingFeeService.delete(feeId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa gói phí thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipping-carrier/" + carrierId + "/fees";
    }
}
