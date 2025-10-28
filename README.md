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
alotra/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── alotra/
│   │   │           ├── controller/        # REST API Controllers
│   │   │           ├── dto/               # Data Transfer Objects
│   │   │           ├── entity/            # Entity Classes (Product, Order, User, etc.)
│   │   │           ├── repository/        # JPA Repositories
│   │   │           ├── service/           # Business Logic Services
│   │   │           ├── security/          # JWT Config, Filters, Authentication
│   │   │           ├── config/            # AppConfig, WebSocketConfig, CORS, etc.
│   │   │           ├── websocket/         # WebSocket Handler & Events
│   │   │           └── AlotraApplication.java  # Spring Boot Main Class
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   ├── images/
│   │       │   └── uploads/              # Local fallback for images
│   │       ├── templates/
│   │       │   ├── index.html            # Trang chủ / Home page
│   │       │   ├── login.html            # Đăng nhập
│   │       │   ├── register.html         # Đăng ký
│   │       │   ├── product-list.html     # Danh sách sản phẩm
│   │       │   ├── product-detail.html   # Chi tiết sản phẩm
│   │       │   ├── cart.html             # Giỏ hàng
│   │       │   ├── order.html            # Đơn hàng
│   │       │   ├── admin/
│   │       │   │   ├── dashboard.html    # Trang quản trị
│   │       │   │   ├── products.html     # Quản lý sản phẩm
│   │       │   │   ├── orders.html       # Quản lý đơn hàng
│   │       │   │   └── users.html        # Quản lý người dùng
│   │       │   └── fragments/            # Navbar, Footer, Components
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
│       └── java/com/alotra/
│           └── ...                       # Unit & Integration Tests
│
├── pom.xml
├── README.md
├── .env.example                          # Mẫu cấu hình biến môi trường
└── .gitignore

