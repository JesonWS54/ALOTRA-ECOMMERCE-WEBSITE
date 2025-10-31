package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Brand;
import nhom17.OneShop.entity.Category;
import nhom17.OneShop.entity.Product;
import nhom17.OneShop.entity.Brand;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.BrandRepository;
import nhom17.OneShop.repository.CategoryRepository;
import nhom17.OneShop.repository.ProductRepository;
import nhom17.OneShop.request.ProductRequest;
import nhom17.OneShop.service.ProductService;
import nhom17.OneShop.service.StorageService;
import nhom17.OneShop.specification.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private StorageService storageService;

    @Override
    public Page<Product> searchProducts(String keyword, Boolean status, Integer categoryId, Integer brandId, String sort, int page, int size) {
        // 1. Xử lý logic sắp xếp (giữ nguyên như cũ)
        Sort sortable = Sort.by("maSanPham").ascending();
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price_asc": sortable = Sort.by("giaBan").ascending(); break;
                case "price_desc": sortable = Sort.by("giaBan").descending(); break;
            }
        }
        Pageable pageable = PageRequest.of(page - 1, size, sortable);

        Specification<Product> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(ProductSpecification.hasKeyword(keyword));
        }
        if (status != null) {
            spec = spec.and(ProductSpecification.hasStatus(status));
        }
        if (categoryId != null) {
            spec = spec.and(ProductSpecification.inCategory(categoryId));
        }
        if (brandId != null) {
            spec = spec.and(ProductSpecification.inBrand(brandId));
        }
        return productRepository.findAll(spec, pageable);
    }

    @Override
    public Product findById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(ProductRequest productRequest) {
        validateUniqueProductName(productRequest);
        Product product = prepareProductEntity(productRequest);
        String oldImage = product.getHinhAnh();
        mapRequestToEntity(productRequest, product);
        productRepository.save(product);

        if (StringUtils.hasText(productRequest.getHinhAnh()) && StringUtils.hasText(oldImage) && !oldImage.equals(productRequest.getHinhAnh())) {
            storageService.deleteFile(oldImage);
        }
    }

    private void validateUniqueProductName(ProductRequest request) {
        if (request.getGiaBan() != null && request.getGiaNiemYet() != null &&
                request.getGiaBan().compareTo(request.getGiaNiemYet()) > 0) {
            throw new IllegalArgumentException("Giá bán không được lớn hơn giá niêm yết.");
        }

        if (request.getMaSanPham() == null && (request.getHinhAnh() == null || request.getHinhAnh().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng chọn hình ảnh cho sản phẩm mới.");
        }

        if (request.getMaSanPham() == null) {
            if (productRepository.existsByTenSanPhamIgnoreCase(request.getTenSanPham())) {
                throw new DuplicateRecordException("Tên sản phẩm '" + request.getTenSanPham() + "' đã tồn tại.");
            }
        }
        else {
            if (productRepository.existsByTenSanPhamIgnoreCaseAndMaSanPhamNot(request.getTenSanPham(), request.getMaSanPham())) {
                throw new DuplicateRecordException("Tên sản phẩm '" + request.getTenSanPham() + "' đã được sử dụng.");
            }
        }
    }

    private Product prepareProductEntity(ProductRequest request) {
        if (request.getMaSanPham() != null) {
            return findById(request.getMaSanPham());
        }
        return new Product();
    }

    private void mapRequestToEntity(ProductRequest request, Product product) {
        Category danhMuc = categoryRepository.findById(request.getMaDanhMuc())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục với ID: " + request.getMaDanhMuc()));
        Brand thuongHieu = brandRepository.findById(request.getMaThuongHieu())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thương hiệu với ID: " + request.getMaThuongHieu()));

        product.setTenSanPham(request.getTenSanPham());
        product.setMoTa(request.getMoTa());
        product.setGiaBan(request.getGiaBan());
        product.setGiaNiemYet(request.getGiaNiemYet());
        product.setHanSuDung(request.getHanSuDung());
        product.setKichHoat(request.isKichHoat());
        product.setDanhMuc(danhMuc);
        product.setThuongHieu(thuongHieu);
        if (StringUtils.hasText(request.getHinhAnh())) {
            product.setHinhAnh(request.getHinhAnh());
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        Product productToDelete = findById(id);

        if (StringUtils.hasText(productToDelete.getHinhAnh())) {
            storageService.deleteFile(productToDelete.getHinhAnh());
        }
        productRepository.delete(productToDelete);
    }

    @Override
    public Page<Product> searchUserProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortOption, List<Integer> brandIds, int page, int size) {
        int pageNumber = page > 0 ? page - 1 : 0;

        Sort sort;
        if (sortOption == null || sortOption.isEmpty() || sortOption.equals("newest")) {
            sort = Sort.by("ngayTao").descending();
        } else {
            switch (sortOption) {
                case "price_asc":
                    sort = Sort.by("giaBan").ascending();
                    break;
                case "price_desc":
                    sort = Sort.by("giaBan").descending();
                    break;
                case "oldest":
                    sort = Sort.by("ngayTao").ascending();
                    break;
                default:
                    sort = Sort.by("ngayTao").descending();
                    break;
            }
        }

        Pageable pageable = PageRequest.of(pageNumber, size, sort);

        // Bắt đầu câu truy vấn với điều kiện cơ bản là sản phẩm phải được kích hoạt
        Specification<Product> spec = (root, query, cb) -> cb.isTrue(root.get("kichHoat"));

        // Thêm các điều kiện lọc nếu chúng tồn tại
        if (categoryId != null) {
            spec = spec.and(ProductSpecification.inCategory(categoryId));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("giaBan"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("giaBan"), maxPrice));
        }
        if (brandIds != null && !brandIds.isEmpty()) {
            spec = spec.and(ProductSpecification.inBrands(brandIds));
        }

        return productRepository.findAll(spec, pageable);
    }
    @Override
    public Page<Product> searchProductsForUser(String keyword, int page, int size) {
        // Sắp xếp theo ngày tạo mới nhất làm mặc định
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTao").descending());
        return productRepository.searchForUser(keyword, pageable);
    }
    @Override
    public List<Product> findNewestProducts(int limit) {
        // Phương thức này đã được định nghĩa sẵn trong Repository, chỉ cần gọi
        return productRepository.findTop8ByKichHoatIsTrueOrderByNgayTaoDesc();
    }

    @Override
    public List<Product> findMostDiscountedProducts(int limit) {
        // Sử dụng Pageable để giới hạn số lượng kết quả trả về
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findTopDiscountedProducts(pageable);
    }

    @Override
    public List<Product> findTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(limit);
    }
}