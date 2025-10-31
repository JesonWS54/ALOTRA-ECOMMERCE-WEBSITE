package nhom17.OneShop.service.impl;

import jakarta.transaction.Transactional;
import nhom17.OneShop.dto.QuickViewDTO;
import nhom17.OneShop.entity.Product;
import nhom17.OneShop.repository.InventoryRepository;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.service.QuickViewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class QuickViewServiceImpl implements QuickViewService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    public QuickViewServiceImpl(ProductRepository productRepo,
                                InventoryRepository inventoryRepo) {
        this.productRepo = productRepo;
        this.inventoryRepo = inventoryRepo;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public QuickViewDTO build(Integer productId) {
        Product p = productRepo.findById(productId).orElse(null);
        if (p == null) return null;

        Integer stockQuantity = inventoryRepo.findBySanPham(p)
                .map(i -> i.getSoLuongTon() != null ? i.getSoLuongTon() : 0)
                .orElse(0);
        boolean inStock = stockQuantity > 0;

        String imageUrl = (p.getHinhAnh() == null || p.getHinhAnh().isBlank())
                ? "/web/assets/images/product/electric/product-01.png"
                : "/uploads/" + p.getHinhAnh();

        long price = p.getGiaBan() != null ? p.getGiaBan().longValue() : 0L;
        long oldPrice = p.getGiaNiemYet() != null ? p.getGiaNiemYet().longValue() : 0L;

        String name = p.getTenSanPham();
        String shortDesc = p.getMoTa();
        String brandName = (p.getThuongHieu() != null) ? p.getThuongHieu().getTenThuongHieu() : null;
        String categoryName = (p.getDanhMuc() != null) ? p.getDanhMuc().getTenDanhMuc() : null;

        Integer reviewCount = (p.getDanhSachRating() != null) ? p.getDanhSachRating().size() : 0;
        Double avgRating = 0.0;
        if (reviewCount > 0) {
            avgRating = p.getDanhSachRating().stream()
                    .mapToInt(rating -> rating.getDiemDanhGia() != null ? rating.getDiemDanhGia() : 0)
                    .average()
                    .orElse(0.0);
        }

        return new QuickViewDTO(
                p.getMaSanPham(), name, brandName, categoryName,
                shortDesc, price, oldPrice, inStock, stockQuantity,
                avgRating, reviewCount, List.of(imageUrl)
        );
    }
}