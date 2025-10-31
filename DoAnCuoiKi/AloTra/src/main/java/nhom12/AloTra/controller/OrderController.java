package nhom12.AloTra.controller;

import nhom12.AloTra.entity.*;
import nhom12.AloTra.repository.OrderStatusHistoryRepository;
import nhom12.AloTra.repository.ShippingCarrierRepository;
import nhom12.AloTra.repository.ShippingFeeRepository;
import nhom12.AloTra.request.OrderUpdateRequest;
import nhom12.AloTra.request.ShippingRequest;
import nhom12.AloTra.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/order") // Controller này chỉ dành cho admin
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private OrderStatusHistoryRepository historyRepository;
    @Autowired private ShippingCarrierRepository shippingCarrierRepository;
    @Autowired
    private ShippingFeeRepository shippingFeeRepository;


    @GetMapping
    public String listOrders(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String paymentMethod,
                             @RequestParam(required = false) String paymentStatus,
                             @RequestParam(required = false) String shippingMethod,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {

        Page<Order> orderPage = orderService.findAll(keyword, status, paymentMethod, paymentStatus, shippingMethod, page, size);
        Map<Long, List<ShippingFee>> carriersWithFeesByOrder = orderService.getCarriersWithFeesByOrder(orderPage.getContent());
        List<String> shippingMethods = shippingFeeRepository.findDistinctPhuongThucVanChuyen();
        model.addAttribute("carriersWithFeesByOrder", carriersWithFeesByOrder);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("shippingMethods", shippingMethods);
        model.addAttribute("shippingRequest", new ShippingRequest());
        return "admin/orders/orders";
    }

    @GetMapping("/{id}")
    public String showOrderDetail(@PathVariable long id, Model model,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String paymentMethod,
                                  @RequestParam(required = false) String paymentStatus,
                                  @RequestParam(required = false) String shippingMethod,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Order order = orderService.findById(id);
        if (order == null) {
            return "redirect:/admin/order";
        }
        OrderUpdateRequest updateRequest = new OrderUpdateRequest();
        updateRequest.setTrangThai(order.getTrangThai());
        updateRequest.setPhuongThucThanhToan(order.getPhuongThucThanhToan());
        updateRequest.setTrangThaiThanhToan(order.getTrangThaiThanhToan());

        model.addAttribute("order", order);
        model.addAttribute("orderUpdateRequest", updateRequest);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/orders/orderDetail";
    }

    @GetMapping("/{id}/history")
    public String viewOrderHistory(@PathVariable long id, Model model,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String paymentMethod,
                                   @RequestParam(required = false) String paymentStatus,
                                   @RequestParam(required = false) String shippingMethod,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Order order = orderService.findById(id);
        if (order == null) {
            return "redirect:/admin/order";
        }
        List<OrderStatusHistory> historyList = historyRepository.findByDonHang_MaDonHangOrderByThoiDiemThayDoiDesc(id);
        model.addAttribute("order", order);
        model.addAttribute("historyList", historyList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/orders/orderHistory";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable long id,
                              @ModelAttribute("orderUpdateRequest") OrderUpdateRequest request,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String paymentMethod,
                              @RequestParam(required = false) String paymentStatus,
                              @RequestParam(required = false) String shippingMethod,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        try {
            orderService.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("status", status);
        redirectAttributes.addAttribute("paymentMethod", paymentMethod);
        redirectAttributes.addAttribute("paymentStatus", paymentStatus);
        redirectAttributes.addAttribute("shippingMethod", shippingMethod);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        return "redirect:/admin/order/" + id;
    }
}