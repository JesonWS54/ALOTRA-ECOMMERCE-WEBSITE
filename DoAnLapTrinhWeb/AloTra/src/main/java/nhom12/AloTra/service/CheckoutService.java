package nhom12.AloTra.service;

import nhom12.AloTra.entity.Order;
import java.math.BigDecimal;

public interface CheckoutService {
    // Thêm 2 tham số mới
    Order placeOrder(Integer diaChiId, String paymentMethod, BigDecimal shippingFee, String shippingMethodName);
}