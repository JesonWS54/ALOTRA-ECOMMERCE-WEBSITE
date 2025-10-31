package nhom17.OneShop.repository;

import nhom17.OneShop.entity.ShippingFee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingFeeRepository extends JpaRepository<ShippingFee, Integer> {
    boolean existsByPhuongThucVanChuyenIgnoreCaseAndTenGoiCuocIgnoreCaseAndNhaVanChuyen_MaNVC(String phuongThucVanChuyen, String tenGoiCuoc, Integer maNVC);
    List<ShippingFee> findByPhuongThucVanChuyenIgnoreCaseAndCacTinhApDung_Id_TenTinhThanh(String phuongThucVanChuyen, String tenTinhThanh);

    boolean existsByPhuongThucVanChuyenIgnoreCaseAndTenGoiCuocIgnoreCaseAndNhaVanChuyen_MaNVCAndMaChiPhiVCNot(
            String phuongThucVanChuyen, String tenGoiCuoc, Integer maNVC, Integer maChiPhiVC);

    List<ShippingFee> findByNhaVanChuyen_MaNVCOrderByMaChiPhiVCDesc(int maNVC);
    @Query("SELECT DISTINCT f.phuongThucVanChuyen FROM ShippingFee f ORDER BY f.phuongThucVanChuyen ASC")
    List<String> findDistinctPhuongThucVanChuyen();
    @Query("SELECT sf FROM ShippingFee sf JOIN sf.cacTinhApDung ap WHERE ap.id.tenTinhThanh = :province ORDER BY sf.chiPhi ASC")
    List<ShippingFee> findApplicableFeesByProvinceOrderedByCost(@Param("province") String tenTinhThanh);
}
