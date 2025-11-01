package nhom12.AloTra.repository;

import nhom12.AloTra.entity.ImportDetail;
import nhom12.AloTra.entity.ImportDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportDetailRepository extends JpaRepository<ImportDetail, ImportDetailId> {
    void deleteAllByPhieuNhap_MaPhieuNhap(Integer maPhieuNhap);
}
