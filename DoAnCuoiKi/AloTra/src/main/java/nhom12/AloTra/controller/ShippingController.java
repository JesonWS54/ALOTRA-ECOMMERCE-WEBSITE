package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.Shipping;
import nhom12.AloTra.entity.ShippingCarrier;
import nhom12.AloTra.repository.ShippingCarrierRepository;
import nhom12.AloTra.repository.ShippingFeeRepository;
import nhom12.AloTra.request.ShippingRequest;
import nhom12.AloTra.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/shipping")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;
    @Autowired
    private ShippingCarrierRepository shippingCarrierRepository;
    @Autowired
    private ShippingFeeRepository shippingFeeRepository;

    @GetMapping
    public String listShippings(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer carrierId,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String shippingMethod,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size, // Tăng size mặc định
                                Model model) {
        Page<Shipping> shippingPage = shippingService.search(keyword, carrierId, status, shippingMethod, page, size);
        List<String> shippingMethods = shippingFeeRepository.findDistinctPhuongThucVanChuyen();
        model.addAttribute("shippingPage", shippingPage);
        model.addAttribute("carriers", shippingCarrierRepository.findAll());
        if (!model.containsAttribute("shippingRequest")) {
            model.addAttribute("shippingRequest", new ShippingRequest());
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("carrierId", carrierId);
        model.addAttribute("status", status);
        model.addAttribute("shippingMethod", shippingMethod); // <-- THÊM VÀO
        model.addAttribute("shippingMethods", shippingMethods);

        return "admin/orders/shippings";
    }

    @PostMapping("/save")
    public String saveShipping(@Valid @ModelAttribute ShippingRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer carrierId,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String shippingMethod,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("shippingRequest", request);
        }
        else {
            try {
                shippingService.save(request);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin vận chuyển thành công!");
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("shippingRequest", request);
            }
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("carrierId", carrierId);
        redirectAttributes.addAttribute("status", status);
        redirectAttributes.addAttribute("shippingMethod", shippingMethod);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/shipping";
    }

    @PostMapping("/saveFromOrder")
    public String saveFromOrder(@ModelAttribute ShippingRequest request,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String paymentMethod,
                                @RequestParam(required = false) String paymentStatus,
                                @RequestParam(required = false) String shippingMethod,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        try {
            shippingService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo đơn vận chuyển cho đơn hàng #" + request.getMaDonHang());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect về trang Orders với đầy đủ state
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("status", status);
        redirectAttributes.addAttribute("paymentMethod", paymentMethod);
        redirectAttributes.addAttribute("paymentStatus", paymentStatus);
        redirectAttributes.addAttribute("shippingMethod", shippingMethod);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/order";
    }

    @GetMapping("/delete/{id}")
    public String deleteShipping(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer carrierId,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String shippingMethod,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        try {
            shippingService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đơn vận chuyển thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("carrierId", carrierId);
        redirectAttributes.addAttribute("status", status);
        redirectAttributes.addAttribute("shippingMethod", shippingMethod);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/shipping";
    }
}
