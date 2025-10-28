# 🧋 ALOTRA E-COMMERCE WEBSITE  
**Website Bán Trà Sữa AloTra – Đồ Án Môn Lập Trình Web**  
Trường Đại học Sư Phạm Kỹ Thuật TP. Hồ Chí Minh  

---

## 👥 Nhóm Thực Hiện

| Họ Tên | MSSV |
|:-------|:------:|
| Phạm Quốc Long | 22110366 |
| Nguyễn Thanh Khang | 23110237 |
| Huỳnh Tấn Vinh | 23110365 |

---

## 📘 Mục Lục
- [Giới Thiệu](#-giới-thiệu)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Cấu Trúc Thư Mục](#-cấu-trúc-thư-mục)
- [Tính Năng Chi Tiết](#-tính-năng-chi-tiết)
- [Cài Đặt & Chạy Dự Án](#-cài-đặt--chạy-dự-án)
- [Kiến Thức Học Được](#-kiến-thức-học-được)
- [Tác Giả](#-tác-giả)

---

## 📖 Giới Thiệu

**AloTra** là website thương mại điện tử dành cho chuỗi cửa hàng **trà sữa AloTra**, giúp quản lý sản phẩm, đơn hàng, kho và khách hàng một cách hiệu quả.  
Hệ thống được xây dựng với **Spring Boot + Thymeleaf + Bootstrap + JPA + SQLServer/MySQL/PostgreSQL + JWT + WebSocket + Cloudinary**, mang lại trải nghiệm trực quan, bảo mật và cập nhật theo thời gian thực.

### 🎯 Mục Tiêu Dự Án
- Xây dựng nền tảng **bán hàng trực tuyến** cho chuỗi cửa hàng trà sữa.  
- **Quản lý sản phẩm, đơn hàng, kho, chi nhánh, tài khoản người dùng.**  
- **Bảo mật API bằng JWT** và **phân quyền theo vai trò (Admin, Employee, Customer)**.  
- **Cập nhật trạng thái đơn hàng real-time** bằng WebSocket.  
- **Upload ảnh sản phẩm lên Cloudinary**.  
- Tối ưu **hiệu năng và khả năng mở rộng** qua JPA, Bootstrap và Thymeleaf.

---

## ⚙️ Công Nghệ Sử Dụng

### 🔧 Backend Framework & Core
- **Java 17+**
- **Spring Boot**
- **Maven**

### 🗄️ Database & Persistence
- **Spring Data JPA / Hibernate**
- **MySQL / SQLServer / PostgreSQL**

### 🔐 Security & Authentication
- **Spring Security**
- **JWT (Access & Refresh Tokens)**
- **Role-based Authorization:** Admin, Employee, Customer

### 💬 Real-time Communication
- **Spring WebSocket (STOMP over SockJS)**

### 🎨 View Layer & Frontend
- **Thymeleaf**
- **Bootstrap 5**
- **HTML5 / CSS3 / JavaScript**

### ☁️ Cloud & File Storage
- **Cloudinary SDK (Java)** – Upload ảnh và lưu đường dẫn URL vào DB

---

## 🧩 Cấu Trúc Thư Mục

```bash
AloTra/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── AloTra/
│       │           ├── config/
│       │           │   ├── CloudinaryConfig.java
│       │           │   ├── VnPayConfig.java
│       │           │   └── WebSocketConfig.java
│       │           │
│       │           ├── controller/
│       │           │   ├── AdminController.java
│       │           │   ├── CartController.java
│       │           │   ├── CheckoutController.java
│       │           │   ├── OrderDetailController.java
│       │           │   ├── PaymentController.java
│       │           │   ├── ProductDetailsController.java
│       │           │   ├── UserHistoryController.java
│       │           │   ├── UserHomeController.java
│       │           │   ├── UserMenuController.java
│       │           │   ├── UserProfileController.java
│       │           │   ├── VendorManagementController.java
│       │           │   └── WebController.java
│       │           │
│       │           ├── entity/
│       │           │   ├── Account.java
│       │           │   ├── Addresses.java
│       │           │   ├── AppCommission.java
│       │           │   ├── Cart.java
│       │           │   ├── CartItem.java
│       │           │   ├── Category.java
│       │           │   ├── Order.java
│       │           │   ├── OrderItem.java
│       │           │   ├── OrderStatusHistory.java
│       │           │   ├── Payment.java
│       │           │   ├── Product.java
│       │           │   ├── ProductFavorite.java
│       │           │   ├── ProductImage.java
│       │           │   ├── ProductView.java
│       │           │   ├── Review.java
│       │           │   ├── ReviewMedia.java
│       │           │   ├── ShippingCarrier.java
│       │           │   ├── Shop.java
│       │           │   └── Voucher.java
│       │           │
│       │           ├── model/
│       │           │   ├── AccountDTO.java
│       │           │   ├── AddressDTO.java
│       │           │   ├── AppCommissionDTO.java
│       │           │   ├── CartDTO.java
│       │           │   ├── CartItemDTO.java
│       │           │   ├── CartViewDTO.java
│       │           │   ├── CategoryDTO.java
│       │           │   ├── NotificationDTO.java
│       │           │   ├── OrderDTO.java
│       │           │   ├── OrderItemDTO.java
│       │           │   ├── OrderStatusHistoryDTO.java
│       │           │   ├── PaymentDTO.java
│       │           │   ├── ProductDTO.java
│       │           │   ├── ProductFavoriteDTO.java
│       │           │   ├── ProductHomeDTO.java
│       │           │   ├── ProductImageDTO.java
│       │           │   ├── ReviewDTO.java
│       │           │   ├── ReviewMediaDTO.java
│       │           │   ├── ShippingCarrierDTO.java
│       │           │   ├── ShopDTO.java
│       │           │   └── VoucherDTO.java
│       │           │
│       │           ├── repository/
│       │           │   ├── AccountRepository.java
│       │           │   ├── AddressRepository.java
│       │           │   ├── AppCommissionRepository.java
│       │           │   ├── CartItemRepository.java
│       │           │   ├── CartRepository.java
│       │           │   ├── CategoryRepository.java
│       │           │   ├── OrderItemRepository.java
│       │           │   ├── OrderRepository.java
│       │           │   ├── PaymentRepository.java
│       │           │   ├── ProductFavoriteRepository.java
│       │           │   ├── ProductImageRepository.java
│       │           │   ├── ProductRepository.java
│       │           │   ├── ProductViewRepository.java
│       │           │   ├── ReviewMediaRepository.java
│       │           │   ├── ReviewRepository.java
│       │           │   ├── ShippingCarrierRepository.java
│       │           │   ├── ShopRepository.java
│       │           │   └── VoucherRepository.java
│       │           │
│       │           ├── services/
│       │           │   ├── impl/
│       │           │   │   ├── AccountService.java
│       │           │   │   ├── AddressService.java
│       │           │   │   ├── AppCommissionService.java
│       │           │   │   ├── CartService.java
│       │           │   │   ├── CategoryService.java
│       │           │   │   ├── CloudinaryService.java
│       │           │   │   ├── OrderService.java
│       │           │   │   ├── PaymentService.java
│       │           │   │   ├── ProductFavoriteService.java
│       │           │   │   ├── ProductService.java
│       │           │   │   ├── ProductViewService.java
│       │           │   │   ├── ReviewService.java
│       │           │   │   ├── ShippingCarrierService.java
│       │           │   │   ├── ShopService.java
│       │           │   │   └── VoucherService.java
│       │           │   │
│       │           │   ├── AccountService.java
│       │           │   ├── PaymentService.java
│       │           │   └── VoucherService.java
│       │           │
│       │           ├── utils/
│       │           │   └── ...
│       │           │
│       │           └── AloTraBubbleTeaApplication.java
│       │
│       └── resources/
│           ├── static/
│           │   ├── assets/
│           │   │   ├── css/
│           │   │   ├── img/
│           │   │   ├── js/
│           │   │   ├── scss/
│           │   │   └── vendor/
│           │
│           ├── templates/
│           │   ├── admin/
│           │   │   ├── fragments/
│           │   │   │   ├── footer-admin.html
│           │   │   │   ├── header-admin.html
│           │   │   │   └── nav-admin.html
│           │   │   ├── layout-admin.html
│           │   │   └── admin-dashboard.html
│           │   │
│           │   ├── shipper/
│           │   │   ├── fragments/
│           │   │   │   ├── footer.html
│           │   │   │   ├── header.html
│           │   │   │   └── nav.html
│           │   │   └── layout-shipper.html
│           │   │
│           │   ├── user/
│           │   │   ├── fragments/
│           │   │   │   ├── footer-user.html
│           │   │   │   ├── header-user.html
│           │   │   │   └── nav-user.html
│           │   │   ├── cart.html
│           │   │   ├── checkout.html
│           │   │   ├── history.html
│           │   │   ├── home.html
│           │   │   ├── layout-user.html
│           │   │   ├── menu.html
│           │   │   ├── order-details.html
│           │   │   ├── payment-result.html
│           │   │   ├── product-details.html
│           │   │   └── profile.html
│           │   │
│           │   ├── vendor/
│           │   │   ├── fragments/
│           │   │   │   ├── footer-vendor.html
│           │   │   │   ├── header-vendor.html
│           │   │   │   └── nav-vendor.html
│           │   │   ├── layout-vendor.html
│           │   │   ├── management.html
│           │   │   ├── shop-register.html
│           │   │   └── vendor-dashboard.html
│           │   │
│           │   ├── web/
│           │   │   ├── fragments/
│           │   │   │   ├── footer.html
│           │   │   │   ├── header.html
│           │   │   │   └── nav.html
│           │   │   ├── index.html
│           │   │   ├── layout-web.html
│           │   │   ├── login.html
│           │   │   └── register.html
│           │
│           └── application.properties
│
├── pom.xml
├── README.md
└── .gitignore

## 🧠 Tính Năng Chi Tiết

### 1️⃣ Xác Thực & Phân Quyền
- Đăng ký / Đăng nhập bằng **JWT**
- Phân quyền theo vai trò: **Admin / Employee / Customer**
- Bảo mật API bằng Header: Authorization: Bearer <token>


---

### 2️⃣ Quản Lý Sản Phẩm
- **CRUD** sản phẩm, danh mục, khuyến mãi
- Upload hình ảnh sản phẩm qua **Cloudinary**
- Hiển thị danh sách, phân trang, tìm kiếm

---

### 3️⃣ Quản Lý Đơn Hàng
- Nhận, xử lý và cập nhật đơn hàng
- Trạng thái: Pending → Preparing → Delivering → Completed / Cancelled
- Gửi thông báo **real-time** cho khách và quản trị qua **WebSocket**

---

### 4️⃣ Quản Lý Kho & Báo Cáo
- Theo dõi **tồn kho**, cảnh báo khi sắp hết
- Xem **thống kê doanh thu**, số lượng đơn, sản phẩm bán chạy

---

### 5️⃣ Giao Tiếp Real-time
- WebSocket endpoint: `/ws`
- Topic ví dụ: 
/topic/order-status/{orderId} → Cập nhật trạng thái đơn hàng
/topic/admin/orders → Đơn mới cho Admin


---

## ⚙️ Cài Đặt & Chạy Dự Án

### 1️⃣ Yêu Cầu Hệ Thống
- **Java 17+**
- **Maven**
- **MySQL / SQLServer / PostgreSQL**
- **Tài khoản Cloudinary**

---

### 2️⃣ Clone Dự Án
```bash
git clone https://github.com/JesonWS54/ALOTRA-ECOMMERCE-WEBSITE.git
cd alotra
---
### 3️⃣ Tạo Database (ví dụ SQL)
CREATE DATABASE AloTra


---
### 4️⃣ Cấu Hình Application
spring.datasource.url=jdbc:mysql://localhost:3306/alotra
spring.datasource.username=<username>
spring.datasource.password=<password>

security.jwt.secret=<jwt_secret>

cloudinary.cloud-name=<cloud_name>
cloudinary.api-key=<api_key>
cloudinary.api-secret=<api_secret>
---

### 5️⃣ Chạy Ứng Dụng
mvn clean install
mvn spring-boot:run

🎓 Kiến Thức Học Được

Thiết kế & xây dựng RESTful API với Spring Boot

Áp dụng Spring Security & JWT cho xác thực người dùng

Giao tiếp real-time bằng WebSocket

Triển khai upload ảnh lên Cloudinary

Tổ chức kiến trúc 3 tầng: Controller – Service – Repository
✍️ Tác Giả
Họ Tên	            	MSSV
Phạm Quốc Long	     22110366
Nguyễn Thanh Khang   23110237
Huỳnh Tấn Vinh	     23110365

