package nhom17.OneShop.service;

import nhom17.OneShop.entity.Order;
import java.math.BigDecimal;

public interface CheckoutService {
    // Thêm 2 tham số mới
    Order placeOrder(Integer diaChiId, String paymentMethod, BigDecimal shippingFee, String shippingMethodName);
}