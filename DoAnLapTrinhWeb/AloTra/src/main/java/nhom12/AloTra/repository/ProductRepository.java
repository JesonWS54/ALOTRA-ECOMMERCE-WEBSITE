package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByTenSanPhamIgnoreCase(String tenSanPham);
    boolean existsByTenSanPhamIgnoreCase(String tenSanPham);
    boolean existsByTenSanPhamIgnoreCaseAndMaSanPhamNot(String tenSanPham, Integer productId);
    boolean existsByDanhMuc_MaDanhMuc(Integer categoryId);
    boolean existsByThuongHieu_MaThuongHieu(Integer brandId);
    List<Product> findTop8ByKichHoatIsTrueOrderByNgayTaoDesc();

    @Query("SELECT p FROM Product p WHERE p.kichHoat = true AND p.giaNiemYet > p.giaBan ORDER BY ((p.giaNiemYet - p.giaBan) / p.giaNiemYet) DESC")
    List<Product> findTopDiscountedProducts(Pageable pageable);

//    User
    @Query("SELECT p FROM Product p WHERE p.kichHoat = true AND (" +
            "LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.danhMuc.tenDanhMuc) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.thuongHieu.tenThuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchForUser(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT TOP (:limit) p.* " +
            "FROM SanPham p " +
            "JOIN DonHang_ChiTiet od ON p.MaSanPham = od.MaSanPham " +
            "JOIN DonHang o ON od.MaDonHang = o.MaDonHang " +
            "WHERE o.TrangThai = N'Đã giao' AND p.KichHoat = 1 " +
            "GROUP BY p.MaSanPham, p.TenSanPham, p.MaDanhMuc, p.MaThuongHieu, p.MoTa, p.GiaBan, p.GiaNiemYet, p.HanSuDung, p.HinhAnh, p.KichHoat, p.NgayTao " +
            "ORDER BY SUM(od.SoLuong) DESC",
            nativeQuery = true)
    List<Product> findTopSellingProducts(@Param("limit") int limit);
}
