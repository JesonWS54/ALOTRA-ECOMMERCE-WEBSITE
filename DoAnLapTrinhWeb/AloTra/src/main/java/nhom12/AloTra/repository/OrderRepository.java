package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Order;
import nhom12.AloTra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByNguoiDungOrderByNgayDatDesc(User nguoiDung);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.maDonHang = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);

    boolean existsByNguoiDung_MaNguoiDung(Integer userId);

    @Query(value = "SELECT COALESCE(SUM(o.TongTien), 0), COUNT(o.MaDonHang) " +
            "FROM DonHang o " + // SỬA LẠI: orders -> DonHang
            "WHERE o.TrangThai = N'Đã giao' AND o.NgayDat >= :startDate AND o.NgayDat < :endDate",
            nativeQuery = true)
    List<Object[]> findKpiDataBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "WITH LatestImportPrice AS ( " +
            "    SELECT MaSanPham, GiaNhap, ROW_NUMBER() OVER(PARTITION BY MaSanPham ORDER BY MaPhieuNhap DESC) as rn " +
            "    FROM ChiTietPhieuNhap " + // SỬA LẠI: import_details -> ChiTietPhieuNhap
            ") " +
            "SELECT COALESCE(SUM(od.SoLuong), 0), COALESCE(SUM(od.SoLuong * lip.GiaNhap), 0) " +
            "FROM DonHang o " + // SỬA LẠI: orders -> DonHang
            "JOIN DonHang_ChiTiet od ON o.MaDonHang = od.MaDonHang " + // SỬA LẠI: order_details -> DonHang_ChiTiet
            "LEFT JOIN LatestImportPrice lip ON od.MaSanPham = lip.MaSanPham AND lip.rn = 1 " +
            "WHERE o.TrangThai = N'Đã giao' AND o.NgayDat >= :startDate AND o.NgayDat < :endDate",
            nativeQuery = true)
    List<Object[]> findProductsAndCogsDataBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "SELECT CAST(o.NgayDat AS DATE) as OrderDate, SUM(o.TongTien) as DailyRevenue " +
            "FROM DonHang o " + // SỬA LẠI: orders -> DonHang
            "WHERE o.TrangThai = N'Đã giao' AND o.NgayDat >= :startDate AND o.NgayDat < :endDate " +
            "GROUP BY CAST(o.NgayDat AS DATE) ORDER BY OrderDate ASC",
            nativeQuery = true)
    List<Object[]> findRevenueByDayBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "WITH RankedProducts AS ( " +
            "    SELECT p.TenSanPham, p.HinhAnh, SUM(od.SoLuong) as TotalQuantity, SUM(od.ThanhTien) as TotalRevenue, " +
            "           ROW_NUMBER() OVER (ORDER BY SUM(od.SoLuong) DESC) as rn " +
            "    FROM DonHang o JOIN DonHang_ChiTiet od ON o.MaDonHang = od.MaDonHang JOIN SanPham p ON od.MaSanPham = p.MaSanPham " + // SỬA LẠI
            "    WHERE o.TrangThai = N'Đã giao' AND o.NgayDat >= :startDate AND o.NgayDat < :endDate " +
            "    GROUP BY p.TenSanPham, p.HinhAnh " +
            ") " +
            "SELECT rp.TenSanPham, rp.HinhAnh, rp.TotalQuantity, rp.TotalRevenue FROM RankedProducts rp WHERE rp.rn <= :limit",
            nativeQuery = true)
    List<Object[]> findTopSellingProductsBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate, @Param("limit") int limit);

    @Query(value = "SELECT c.TenDanhMuc, SUM(od.ThanhTien) as CategoryRevenue " +
            "FROM DonHang o " + // SỬA LẠI
            "JOIN DonHang_ChiTiet od ON o.MaDonHang = od.MaDonHang " + // SỬA LẠI
            "JOIN SanPham p ON od.MaSanPham = p.MaSanPham " + // SỬA LẠI
            "JOIN DanhMuc c ON p.MaDanhMuc = c.MaDanhMuc " + // SỬA LẠI
            "WHERE o.TrangThai = N'Đã giao' AND o.NgayDat >= :startDate AND o.NgayDat < :endDate " +
            "GROUP BY c.TenDanhMuc ORDER BY CategoryRevenue DESC",
            nativeQuery = true)
    List<Object[]> findRevenueByCategoryBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);


    @Query(value = "SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) " +
            "FROM DonHang o " +
            "JOIN DonHang_ChiTiet od ON o.MaDonHang = od.MaDonHang " +
            "WHERE o.MaNguoiDung = :userId AND od.MaSanPham = :productId AND o.TrangThai = N'Đã giao'",
            nativeQuery = true)
    boolean hasCompletedPurchase(@Param("userId") Integer userId, @Param("productId") Integer productId);
}

