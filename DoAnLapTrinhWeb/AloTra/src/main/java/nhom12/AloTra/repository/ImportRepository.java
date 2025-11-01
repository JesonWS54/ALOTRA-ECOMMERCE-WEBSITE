package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportRepository extends JpaRepository<Import, Integer>, JpaSpecificationExecutor<Import> {
    boolean existsByNhaCungCap_MaNCC(Integer supplierId);
}
