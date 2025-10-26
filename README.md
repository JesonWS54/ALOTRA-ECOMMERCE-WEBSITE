# ALOTRA E-COMMERCE WEBSITE

Website bán trà sữa AloTra – đồ án môn *Lập trình Web*, trường Đại học Sư Phạm Kỹ Thuật TP.HCM.

---

# Nhóm thực hiện:

Phạm Quốc Long			MSSV: 22110366
Nguyễn Thanh Khang 		MSSV: 23110237	
Huỳnh Tấn Vinh     		MSSV: 23110365

# Mục tiêu

Xây dựng một website quản lý và bán hàng cho chuỗi trà sữa AloTra sử dụng Spring Boot + Thymeleaf + Bootstrap + JPA + SQLServer/MySQL/PostgreSQL + JWT + WebSocket + Cloudinary.
Mục tiêu chính:

Triển khai hệ thống e-commerce đơn giản cho chuỗi cửa hàng trà sữa (thông tin sản phẩm, giỏ hàng, đặt đơn, quản trị đơn & kho).

Bảo mật API bằng JWT (vai trò: ADMIN, EMPLOYEE, CUSTOMER).

Cập nhật hình ảnh sản phẩm trên Cloudinary.

Cập nhật trạng thái đơn hàng theo thời gian thực bằng WebSocket (thông báo cho khách & quản trị).

Sử dụng JPA để tương tác với database (hỗ trợ SQLServer / MySQL / PostgreSQL).

Tài liệu hóa cách cài đặt, cấu hình và triển khai.

# Mô tả dự án

AloTra là website bán hàng dành cho chuỗi trà sữa, gồm hai phần giao diện chính:

Customer (Thương mại điện tử): duyệt sản phẩm, lọc, thêm vào giỏ, đặt hàng, theo dõi trạng thái đơn hàng (thời gian thực).

Admin / Employee (Quản trị): quản lý sản phẩm (CRUD, upload hình ảnh lên Cloudinary), quản lý đơn hàng, quản lý kho, quản lý cửa hàng/chi nhánh, xem báo cáo đơn giản.

Tính năng nổi bật:

Xác thực & phân quyền bằng JWT.

Thanh toán mô phỏng (hoặc tích hợp cổng trong tương lai).

Upload ảnh sản phẩm bằng Cloudinary.

WebSocket (STOMP over SockJS) để đẩy thông báo trạng thái đơn hàng và chat nội bộ (tuỳ chọn).

Responsive UI với Thymeleaf + Bootstrap.

Kiến trúc & Thành phần

Backend: Spring Boot (Spring Web, Spring Security, Spring Data JPA, Spring WebSocket).

Frontend server-side rendered: Thymeleaf + Bootstrap 5 (tối ưu cho SEO + đơn giản).

Database: hỗ trợ MySQL / PostgreSQL / SQLServer (cấu hình qua application.properties / profiles).

Lưu trữ ảnh: Cloudinary (hoặc local fallback).

Bảo mật: JWT (access token, refresh token — tùy chọn).

Thông báo thời gian thực: WebSocket + STOMP (endpoint /ws), client dùng SockJS + STOMP.js.

Migrations (khuyến nghị): Flyway hoặc để Hibernate tạo schema (dev).

Build: Maven (hoặc Gradle nếu bạn muốn).

Các chức năng chính (chi tiết)
Người dùng (Customer)

Đăng ký / Đăng nhập (JWT).

Duyệt sản phẩm theo danh mục, tìm kiếm, phân trang.

Thêm/sửa/xoá giỏ hàng.

Thanh toán (mô phỏng).

Xem lịch sử đơn & trạng thái (cập nhật real-time).

Nhận thông báo khi trạng thái đơn thay đổi.

Quản trị (Admin/Employee)

Quản lý sản phẩm (thêm/ sửa/ xóa) với upload ảnh Cloudinary.

Quản lý danh mục, giá, khuyến mãi.

Quản lý đơn hàng: nhận đơn, cập nhật trạng thái (Pending → Preparing → Delivering → Completed/Cancelled).

Quản lý kho: tồn kho sản phẩm, cảnh báo khi sắp hết.

Thống kê đơn giản: doanh thu theo ngày/chi nhánh (basic).

Hệ thống & Kỹ thuật

JWT cho API REST: token lưu trong header Authorization: Bearer <token>.

WebSocket: endpoint /ws (STOMP), topic ví dụ: /topic/order-status/{orderId}.

Cloudinary: upload ảnh, trả về URL lưu trong DB.

Hỗ trợ đa DB: cấu hình profile dev-mysql, dev-postgres, dev-sqlserver.

# Cấu trúc thư mục (gợi ý)
alotra/
├─ src/
│  ├─ main/
│  │  ├─ java/com/alotra/
│  │  │  ├─ controller/
│  │  │  ├─ dto/
│  │  │  ├─ entity/
│  │  │  ├─ repository/
│  │  │  ├─ service/
│  │  │  ├─ security/
│  │  │  └─ websocket/
│  │  └─ resources/
│  │     ├─ templates/      (Thymeleaf)
│  │     ├─ static/         (css, js)
│  │     └─ application-*.properties
└─ pom.xml

# Cài đặt & Chạy (Local)
Yêu cầu

Java 17+ (hoặc phiên bản dự án yêu cầu).

Maven (hoặc Gradle).

MySQL / PostgreSQL / SQLServer (hoặc dùng Docker).

Tài khoản Cloudinary (API key/secret) nếu dùng upload ảnh.

1) Clone repo


2) Cấu hình biến môi trường / application.properties


Lưu ý: Đặt security.jwt.secret bằng biến môi trường trong production, không commit lên git.

3) Khởi động database (ví dụ Docker Compose)


4) Build & Run
# với Maven (wrapper)
./mvnw clean package
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev-mysql
# hoặc chạy jar
java -jar target/alotra-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev-mysql


Ứng dụng sẽ chạy mặc định trên http://localhost:8080.

API / WebSocket (ví dụ)
Endpoints chính (REST)

POST /api/auth/register — đăng ký user.

POST /api/auth/login — đăng nhập, trả JWT.

GET /products — danh sách sản phẩm (filter, page).

GET /products/{id} — chi tiết sản phẩm.

POST /cart — thêm vào giỏ hàng (customer).

POST /orders — tạo đơn hàng (customer).

GET /orders/{id} — lấy đơn hàng (có kiểm tra quyền).

PUT /admin/products — tạo/sửa sản phẩm (ADMIN/EMPLOYEE).

POST /admin/products/{id}/image — upload ảnh (Cloudinary).

WebSocket (STOMP)

Endpoint connect: /ws (SockJS fallback enabled).

Subscribe topics:

/topic/order-status/{orderId} — nhận cập nhật trạng thái cho đơn cụ thể.

/topic/admin/orders — admin nhận đơn mới real-time.

Khi admin cập nhật trạng thái đơn, server convertAndSend("/topic/order-status/" + orderId, payload) để notify client.

Bảo mật (JWT + Roles)

Sử dụng filter để xác thực JWT cho các request API.

Phân quyền theo ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_CUSTOMER.

Endpoints bảo mật ví dụ:

/admin/** → hasRole("ADMIN") hoặc hasAnyRole("ADMIN","EMPLOYEE").

/api/orders/** → authenticated() và kiểm tra ownership.

Cloudinary — Upload ảnh

Cấu hình account Cloudinary trong application.properties (hoặc biến môi trường).

Khi upload ảnh sản phẩm, backend gọi Cloudinary SDK (Java) để upload → nhận secure_url lưu vào Product.imageUrl.

Tùy chọn: hỗ trợ upload nhiều ảnh (gallery) + thumbnail generation.

Testing

Unit tests: JUnit + Mockito cho service & repository.

Integration tests: Spring Boot Test (cấu hình H2 / test profile).

Manual QA: kiểm tra luồng đặt hàng, upload ảnh, socket notification.

Triển khai (Deployment)



# Hướng phát triển (tương lai)

Tích hợp cổng thanh toán thực tế (VNPay, Stripe, ...).

Microservice hóa: tách service order, product, auth.

Hệ thống recommend sản phẩm (machine learning).

Mobile app / PWA.

Tối ưu hiệu năng cho tải lớn, caching (Redis), CDN cho ảnh.

# Tài liệu tham khảo

Spring Boot Documentation — https://spring.io/projects/spring-boot

Thymeleaf Documentation — https://www.thymeleaf.org/

