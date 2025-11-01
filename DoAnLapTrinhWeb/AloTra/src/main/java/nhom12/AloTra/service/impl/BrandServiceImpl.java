package nhom12.AloTra.service.impl;

import nhom12.AloTra.entity.Brand;
import nhom12.AloTra.exception.DataIntegrityViolationException;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.repository.BrandRepository;
import nhom12.AloTra.repository.ProductRepository;
import nhom12.AloTra.request.BrandRequest;
import nhom12.AloTra.service.BrandService;
import nhom12.AloTra.service.StorageService;
import nhom12.AloTra.specification.BrandSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private StorageService storageService;

    @Autowired private
    ProductRepository productRepository;

    @Override
    public Page<Brand> searchAndFilter(String keyword, Boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("maThuongHieu").ascending());

        Specification<Brand> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(BrandSpecification.hasKeyword(keyword));
        }
        if (status != null) {
            spec = spec.and(BrandSpecification.hasStatus(status));
        }

        return brandRepository.findAll(spec, pageable);
    }

    @Override
    public Brand findById(int id) {
        return brandRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(BrandRequest brandRequest) {
        validateUniqueBrandName(brandRequest);
        Brand brand = prepareBrandEntity(brandRequest);
        String oldImage = brand.getHinhAnh();
        mapRequestToEntity(brandRequest, brand);
        brandRepository.save(brand);

        if (StringUtils.hasText(brandRequest.getHinhAnh()) && StringUtils.hasText(oldImage) && !oldImage.equals(brandRequest.getHinhAnh())) {
            storageService.deleteFile(oldImage);
        }
    }

    private void validateUniqueBrandName(BrandRequest request) {
        if (request.getMaThuongHieu() == null) { // Tạo mới
            if (brandRepository.existsByTenThuongHieuIgnoreCase(request.getTenThuongHieu())) {
                throw new DuplicateRecordException("Tên thương hiệu '" + request.getTenThuongHieu() + "' đã tồn tại.");
            }
        } else { // Cập nhật
            if (brandRepository.existsByTenThuongHieuIgnoreCaseAndMaThuongHieuNot(request.getTenThuongHieu(), request.getMaThuongHieu())) {
                throw new DuplicateRecordException("Tên thương hiệu '" + request.getTenThuongHieu() + "' đã được sử dụng.");
            }
        }
    }

    private Brand prepareBrandEntity(BrandRequest request) {
        if (request.getMaThuongHieu() != null) {
            return findById(request.getMaThuongHieu());
        }
        return new Brand();
    }

    private void mapRequestToEntity(BrandRequest request, Brand brand) {
        brand.setTenThuongHieu(request.getTenThuongHieu());
        brand.setMoTa(request.getMoTa());
        brand.setKichHoat(request.isKichHoat());
        if (StringUtils.hasText(request.getHinhAnh())) {
            brand.setHinhAnh(request.getHinhAnh());
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        Brand brandToDelete = findById(id);
        if (productRepository.existsByThuongHieu_MaThuongHieu(id)) {
            throw new DataIntegrityViolationException("Không thể xóa thương hiệu '" + brandToDelete.getTenThuongHieu() + "' vì vẫn còn sản phẩm thuộc thương hiệu này.");
        }
        if (StringUtils.hasText(brandToDelete.getHinhAnh())) {
            storageService.deleteFile(brandToDelete.getHinhAnh());
        }
        brandRepository.delete(brandToDelete);
    }
}
