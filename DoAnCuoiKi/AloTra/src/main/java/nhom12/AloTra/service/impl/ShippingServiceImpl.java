package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.*;
import nhom17.OneShop.exception.DataIntegrityViolationException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.request.ShippingRequest;
import nhom17.OneShop.service.OrderService;
import nhom17.OneShop.service.ShippingService;
import nhom17.OneShop.specification.ShippingSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class ShippingServiceImpl implements ShippingService {

    @Autowired
    private ShippingRepository shippingRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ShippingCarrierRepository shippingCarrierRepository;
    @Autowired // ✅ THÊM VÀO
    private OrderStatusHistoryRepository historyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderService orderService;

    @Override
    public Page<Shipping> search(String keyword, Integer carrierId, String status, String shippingMethod, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("guiLuc").descending());
        Specification<Shipping> spec = ShippingSpecification.filterBy(keyword, carrierId, status, shippingMethod);
        return shippingRepository.findAll(spec, pageable);
    }

    @Override
    public Shipping findById(Long id) {
        return shippingRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(ShippingRequest request) {
        // Bước 1: Validate
        validateShippingRequest(request);

        // Bước 2: Chuẩn bị Entity
        Shipping shipping = prepareShippingEntity(request);

        // Bước 3: Map dữ liệu và xử lý nghiệp vụ
        mapRequestToEntity(request, shipping);

        // Bước 4: Lưu
        shippingRepository.save(shipping);
    }

    private void validateShippingRequest(ShippingRequest request) {
        if (request.getMaVanChuyen() == null) { // Chỉ kiểm tra khi tạo mới
            if (shippingRepository.existsByDonHang_MaDonHang(request.getMaDonHang())) {
                throw new DataIntegrityViolationException("Đơn hàng #" + request.getMaDonHang() + " đã có đơn vận chuyển. Không thể tạo thêm.");
            }
        }
    }

    private Shipping prepareShippingEntity(ShippingRequest request) {
        if (request.getMaVanChuyen() == null) {
            Shipping newShipping = new Shipping();
            newShipping.setMaVanDon(UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            newShipping.setGuiLuc(LocalDateTime.now());
            return newShipping;
        }
        return findById(request.getMaVanChuyen());
    }

    private void mapRequestToEntity(ShippingRequest request, Shipping shipping) {
        Order order = orderRepository.findById(request.getMaDonHang())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với mã: " + request.getMaDonHang()));

        ShippingCarrier carrier = shippingCarrierRepository.findById(request.getMaNVC())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà vận chuyển với ID: " + request.getMaNVC()));

        shipping.setDonHang(order);
        shipping.setNhaVanChuyen(carrier);

        String oldOrderStatus = order.getTrangThai();
        String newShippingStatus = request.getTrangThai();

        if (request.getMaVanChuyen() == null) { // Trường hợp Thêm mới
            shipping.setTrangThai("Đã khởi tạo");
        } else {
            if (!Objects.equals(shipping.getTrangThai(), newShippingStatus)) {
                shipping.setTrangThai(newShippingStatus);
                if ("Đã giao".equals(newShippingStatus)) {
                    if (shipping.getGiaoLuc() == null) {
                        shipping.setGiaoLuc(LocalDateTime.now());
                    }
                    order.setTrangThaiThanhToan("Đã thanh toán");
                }

                String newOrderStatus = mapShippingStatusToOrderStatus(newShippingStatus);
                if (newOrderStatus != null) {
                    orderService.updateLoyaltyPoints(order, oldOrderStatus, newOrderStatus);
                    updateOrderStatusAndLog(order, newOrderStatus, oldOrderStatus);
                }
            }
        }
    }

    private String mapShippingStatusToOrderStatus(String shippingStatus) {
        switch (shippingStatus) {
            case "Đang giao": return "Đang giao";
            case "Đã giao": return "Đã giao";
            case "Trả hàng": return "Trả hàng-Hoàn tiền";
            default: return null;
        }
    }

    private void updateOrderStatusAndLog(Order order, String newStatus, String oldStatus) {
        if (newStatus != null && !Objects.equals(oldStatus, newStatus)) {
            order.setTrangThai(newStatus);
            OrderStatusHistory history = new OrderStatusHistory();
            history.setDonHang(order);
            history.setTuTrangThai(oldStatus);
            history.setDenTrangThai(newStatus);
            history.setThoiDiemThayDoi(LocalDateTime.now());

            User currentUser = getCurrentUser();
            history.setNguoiThucHien(currentUser);

            historyRepository.save(history);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Shipping shipping = findById(id);
        if ("Đang giao".equals(shipping.getTrangThai()) || "Đã giao".equals(shipping.getTrangThai())) {
            throw new DataIntegrityViolationException("Không thể xóa đơn vận chuyển đang hoặc đã giao hàng.");
        }
        shippingRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        return userRepository.findByEmail(username).orElseThrow();
    }
}
