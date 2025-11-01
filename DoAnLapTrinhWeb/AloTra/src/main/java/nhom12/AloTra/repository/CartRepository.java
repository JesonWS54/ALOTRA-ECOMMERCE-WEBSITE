package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Cart;
import nhom12.AloTra.entity.CartId;
import nhom12.AloTra.entity.User;
import nhom12.AloTra.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {

    Optional<Cart> findByNguoiDungAndSanPham(User nguoiDung, Product sanPham);
    @Query("SELECT gh FROM Cart gh JOIN FETCH gh.sanPham WHERE gh.nguoiDung = :nguoiDung")
    List<Cart> findByNguoiDungWithProduct(@Param("nguoiDung") User nguoiDung);

    @Transactional
    void deleteByNguoiDungAndSanPham(User nguoiDung, Product sanPham);
}