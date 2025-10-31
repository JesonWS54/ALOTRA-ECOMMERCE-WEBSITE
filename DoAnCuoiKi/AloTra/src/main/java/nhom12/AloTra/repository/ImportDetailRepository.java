package nhom17.OneShop.repository;

import nhom17.OneShop.entity.ImportDetail;
import nhom17.OneShop.entity.ImportDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportDetailRepository extends JpaRepository<ImportDetail, ImportDetailId> {
    void deleteAllByPhieuNhap_MaPhieuNhap(Integer maPhieuNhap);
}
