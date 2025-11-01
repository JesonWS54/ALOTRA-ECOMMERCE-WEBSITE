package nhom12.AloTra.controller;

import jakarta.validation.Valid;
import nhom12.AloTra.entity.ShippingCarrier;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.request.ShippingCarrierRequest;
import nhom12.AloTra.service.ShippingCarrierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/shipping-carrier")
public class ShippingCarrierController {

    @Autowired
    private ShippingCarrierService shippingCarrierService;

    @GetMapping
    public String listCarriers(@RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model) {
        Page<ShippingCarrier> carrierPage = shippingCarrierService.search(keyword, page, size);
        model.addAttribute("carrierPage", carrierPage);
        model.addAttribute("keyword", keyword);
        if (!model.containsAttribute("carrier")) {
            model.addAttribute("carrier", new ShippingCarrierRequest());
        }
        return "admin/system/shipping-carriers";
    }

    @PostMapping("/save")
    public String saveCarrier(@Valid @ModelAttribute ShippingCarrierRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "5") int size) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("carrier", request);
        }
        else {
            try {
                shippingCarrierService.save(request);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu nhà vận chuyển thành công!");
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                redirectAttributes.addFlashAttribute("carrier", request);
            }
        }
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/shipping-carrier";
    }

    @GetMapping("/delete/{id}")
    public String deleteCarrier(@PathVariable int id,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size) {
        shippingCarrierService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa nhà vận chuyển thành công!");
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/shipping-carrier";
    }
}
