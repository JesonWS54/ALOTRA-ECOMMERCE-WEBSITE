package AloTra.services.impl;

import AloTra.config.VnPayConfig; // Import VnPayConfig
import AloTra.entity.Order;
import AloTra.entity.Payment;
import AloTra.repository.OrderRepository; // Import OrderRepository
import AloTra.repository.PaymentRepository;
import AloTra.services.OrderService; // Import OrderService (nếu cần cập nhật order)
import AloTra.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime; // *** THÊM IMPORT NÀY ***
import java.util.*; // Import Map và Collections

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository; // Inject OrderRepository

    // @Autowired
    // private OrderService orderService; // Inject OrderService nếu cần update phức tạp

    @Autowired
    private VnPayConfig vnPayConfig; // Inject VnPayConfig

    @Override
    public String createVnPayPaymentUrl(Order order, HttpServletRequest request) throws Exception {
        long amount = (long) (order.getFinalTotal() * 100); // VNPAY dùng đơn vị đồng (nhân 100)
        String bankCode = request.getParameter("bankCode"); // Có thể lấy từ form checkout

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VnPayConfig.VNP_VERSION); // Sử dụng hằng số từ VnPayConfig
        vnp_Params.put("vnp_Command", VnPayConfig.VNP_COMMAND_PAY); // Sử dụng hằng số từ VnPayConfig
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode()); // Gọi getter
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", String.valueOf(order.getId()) + "_" + System.currentTimeMillis()); // Mã tham chiếu GD duy nhất
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + order.getId());
        vnp_Params.put("vnp_OrderType", VnPayConfig.ORDER_TYPE); // Sử dụng hằng số từ VnPayConfig

        String locate = request.getParameter("language");
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }

        String returnUrl = vnPayConfig.getReturnUrl().trim(); // Gọi getter
        vnp_Params.put("vnp_ReturnUrl", returnUrl);

        String ipAddress = VnPayConfig.getIpAddress(request);
        vnp_Params.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Thời hạn thanh toán 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build data query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString()); // Gọi getter
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryUrl; // Gọi getter

        // (Optional) Lưu thông tin giao dịch ban đầu vào bảng Payments với status PENDING
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getFinalTotal());
        payment.setPaymentGateway("VNPAY");
        payment.setStatus("PENDING"); // Trạng thái chờ xử lý
        payment.setTransactionCode(vnp_Params.get("vnp_TxnRef")); // Lưu mã tham chiếu
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);


        return paymentUrl;
    }

    @Override
    @Transactional // Quan trọng: Cần Transaction để cập nhật Order và Payment
    public int handleVnPayReturn(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        // Xóa hash ra khỏi params để kiểm tra chữ ký
        params.remove("vnp_SecureHash");

        // Sắp xếp lại params để tạo hashData
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                 try {
                     hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                 } catch (Exception e) { e.printStackTrace(); } // Bỏ qua lỗi encode ở đây
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String secureHash = VnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString()); // Gọi getter

        // 1. Kiểm tra chữ ký
        if (secureHash.equals(vnp_SecureHash)) {
            String orderIdStr = params.get("vnp_TxnRef").split("_")[0]; // Lấy Order ID từ TxnRef
            long orderId = Long.parseLong(orderIdStr);
            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpTransactionStatus = params.get("vnp_TransactionStatus"); // 00 là thành công
            double vnpAmount = Double.parseDouble(params.get("vnp_Amount")) / 100; // Chia 100 về đơn vị gốc
            String vnpTransactionNo = params.get("vnp_TransactionNo"); // Mã GD của VNPAY

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return 1; // Order not found
            }
            Order order = orderOpt.get();

            // Tìm Payment tương ứng (nếu đã tạo lúc createUrl)
             Optional<Payment> paymentOpt = paymentRepository.findByTransactionCode(params.get("vnp_TxnRef"));
             Payment payment;
             if(paymentOpt.isPresent()) {
                 payment = paymentOpt.get();
             } else {
                 // Nếu chưa tạo payment lúc createUrl thì tạo mới ở đây
                 payment = new Payment();
                 payment.setOrder(order);
                 payment.setAmount(vnpAmount);
                 payment.setPaymentGateway("VNPAY");
                 payment.setTransactionCode(params.get("vnp_TxnRef")); // Lưu mã tham chiếu
                 payment.setCreatedAt(LocalDateTime.now());
             }

            // 2. Kiểm tra xem Order đã được thanh toán chưa
            if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                 payment.setStatus("FAILED"); // Cập nhật payment nếu order đã paid
                 paymentRepository.save(payment);
                return 3; // Order already paid
            }

            // 3. Kiểm tra số tiền
            if (order.getFinalTotal().doubleValue() != vnpAmount) {
                payment.setStatus("FAILED"); // Cập nhật payment
                paymentRepository.save(payment);
                return 2; // Invalid amount
            }

            // 4. Kiểm tra trạng thái giao dịch từ VNPAY
            if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
                // Giao dịch thành công
                order.setPaymentStatus("PAID");
                order.setStatus("CONFIRMED"); // Hoặc trạng thái phù hợp sau thanh toán
                orderRepository.save(order);

                payment.setStatus("SUCCESS");
                // Lưu mã giao dịch của VNPAY (nếu cần)
                // payment.setVnpTransactionNo(vnpTransactionNo);
                paymentRepository.save(payment);

                // TODO: Thực hiện các logic sau thanh toán thành công ở đây hoặc trong IPN
                // - Cập nhật số lượng Voucher
                // - Gửi email xác nhận
                // - Gửi thông báo WebSocket cho Vendor

                return 0; // Success
            } else {
                // Giao dịch thất bại
                 order.setStatus("CANCELLED"); // Hủy đơn hàng nếu thanh toán thất bại
                 orderRepository.save(order);

                 payment.setStatus("FAILED");
                 paymentRepository.save(payment);

                 // TODO: Xử lý khi thanh toán thất bại (vd: khôi phục stock?)

                return 99; // Transaction failed
            }
        } else {
            return 4; // Invalid signature
        }
    }

}