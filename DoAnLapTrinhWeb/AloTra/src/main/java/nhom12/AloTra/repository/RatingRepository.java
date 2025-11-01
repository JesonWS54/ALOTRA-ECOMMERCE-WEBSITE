package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Product;
import nhom12.AloTra.entity.Rating;
import nhom12.AloTra.entity.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, RatingId> {
    List<Rating> findBySanPham_MaSanPhamOrderByNgayTaoDesc(Integer maSanPham);
    boolean existsByNguoiDung_MaNguoiDungAndSanPham_MaSanPham(Integer userId, Integer productId);
    // Lấy tất cả đánh giá của 1 sản phẩm
    List<Rating> findBySanPham(Product sanPham);

    // Đếm số đánh giá của 1 sản phẩm
    long countBySanPham(Product sanPham);

    default Double avgScoreByProduct(Product sanPham) {
        List<Rating> list = findBySanPham(sanPham);
        if (list == null || list.isEmpty()) return null;

        double avg = list.stream()
                .map(RatingRepository::extractScore)
                .filter(Objects::nonNull)
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(Double.NaN);

        return Double.isNaN(avg) ? null : avg;
    }

    private static Number extractScore(Rating r) {
        for (String getter : new String[]{
                "getSoSao", "getDiem", "getRating", "getScore", "getMucDanhGia", "getSao"
        }) {
            try {
                Method m = r.getClass().getMethod(getter);
                Object val = m.invoke(r);
                if (val instanceof Number n) return n;
            } catch (Exception ignored) {}
        }
        return null;
    }
    Optional<Rating> findByNguoiDung_MaNguoiDungAndSanPham_MaSanPham(Integer userId, Integer productId);
}
