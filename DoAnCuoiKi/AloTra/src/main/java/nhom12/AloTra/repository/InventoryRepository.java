package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Inventory;
import nhom17.OneShop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository
        extends JpaRepository<Inventory, Integer>, JpaSpecificationExecutor<Inventory> {

    // Đúng tên field trong entity Inventory: sanPham
    Optional<Inventory> findBySanPham(Product sanPham);

    @Query("select coalesce(sum(i.soLuongTon), 0) from Inventory i where i.sanPham = :sanPham")
    Long totalQuantityBySanPham(@Param("sanPham") Product sanPham);

    boolean existsBySanPham(Product sanPham);
}
