package AloTra.services;

import AloTra.entity.Order;
import AloTra.entity.Payment;
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest

import java.util.Map; // Import Map

public interface PaymentService {

    /**
     * Tạo URL thanh toán VNPAY.
     * @param order Đơn hàng cần thanh toán.
     * @param request HttpServletRequest để lấy IP client.
     * @return URL thanh toán VNPAY.
     * @throws Exception Nếu có lỗi xảy ra.
     */
    String createVnPayPaymentUrl(Order order, HttpServletRequest request) throws Exception;

    /**
     * Xử lý callback từ VNPAY (Return URL).
     * @param params Map chứa các tham số từ VNPAY.
     * @return 0: Success, 1: Order not found, 2: Invalid amount, 3: Order already paid, 4: Invalid signature, 99: Unknown error
     */
    int handleVnPayReturn(Map<String, String> params);

    // TODO: Thêm hàm handleVnPayIPN nếu cần xử lý IPN riêng biệt
}