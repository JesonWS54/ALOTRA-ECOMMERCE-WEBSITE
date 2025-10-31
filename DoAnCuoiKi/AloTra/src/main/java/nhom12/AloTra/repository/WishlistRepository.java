package nhom12.AloTra.repository;

import nhom12.AloTra.entity.User;
import nhom12.AloTra.entity.WishList;
import nhom12.AloTra.entity.WishListId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishList, WishListId> {
    List<WishList> findByNguoiDung(User nguoiDung);
    long countByNguoiDung(User nguoiDung);
    Optional<WishList> findByNguoiDungAndSanPham_MaSanPham(User nguoiDung, Integer productId);
}