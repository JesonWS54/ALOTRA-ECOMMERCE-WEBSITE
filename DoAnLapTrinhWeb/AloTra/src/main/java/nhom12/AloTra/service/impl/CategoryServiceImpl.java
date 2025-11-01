package nhom12.AloTra.service.impl;

import nhom12.AloTra.entity.Category;
import nhom12.AloTra.exception.DataIntegrityViolationException;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.repository.CategoryRepository;
import nhom12.AloTra.repository.ProductRepository;
import nhom12.AloTra.request.CategoryRequest;
import nhom12.AloTra.service.CategoryService;
import nhom12.AloTra.service.StorageService;
import nhom12.AloTra.specification.CategorySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StorageService storageService;

    @Autowired private
    ProductRepository productRepository;

    @Override
    public Page<Category> searchAndFilter(String keyword, Boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("maDanhMuc").ascending());

        Specification<Category> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(CategorySpecification.hasKeyword(keyword));
        }
        if (status != null) {
            spec = spec.and(CategorySpecification.hasStatus(status));
        }

        return categoryRepository.findAll(spec, pageable);
    }

    @Override
    public Category findById(int id) {
        return categoryRepository.findById(id).orElse(null);
    }
    @Override
    @Transactional
    public void save(CategoryRequest categoryRequest) {
        validateUniqueCategoryName(categoryRequest);
        Category category = prepareCategoryEntity(categoryRequest);
        String oldImage = category.getHinhAnh();
        mapRequestToEntity(categoryRequest, category);
        categoryRepository.save(category);

        if (StringUtils.hasText(categoryRequest.getHinhAnh()) && StringUtils.hasText(oldImage) && !oldImage.equals(categoryRequest.getHinhAnh())) {
            storageService.deleteFile(oldImage);
        }
    }

    private void validateUniqueCategoryName(CategoryRequest request) {
        if (request.getMaDanhMuc() == null) {
            if (categoryRepository.existsByTenDanhMucIgnoreCase(request.getTenDanhMuc())) {
                throw new DuplicateRecordException("Tên danh mục '" + request.getTenDanhMuc() + "' đã tồn tại.");
            }
        } else {
            if (categoryRepository.existsByTenDanhMucIgnoreCaseAndMaDanhMucNot(request.getTenDanhMuc(), request.getMaDanhMuc())) {
                throw new DuplicateRecordException("Tên danh mục '" + request.getTenDanhMuc() + "' đã được sử dụng.");
            }
        }
    }

    private Category prepareCategoryEntity(CategoryRequest request) {
        if (request.getMaDanhMuc() != null) {
            return findById(request.getMaDanhMuc());
        }
        return new Category();
    }

    private void mapRequestToEntity(CategoryRequest request, Category category) {
        category.setTenDanhMuc(request.getTenDanhMuc());
        category.setKichHoat(request.isKichHoat());
        if (StringUtils.hasText(request.getHinhAnh())) {
            category.setHinhAnh(request.getHinhAnh());
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        Category categoryToDelete = findById(id);
        if (productRepository.existsByDanhMuc_MaDanhMuc(id)) {
            throw new DataIntegrityViolationException("Không thể xóa danh mục '" + categoryToDelete.getTenDanhMuc() + "' vì vẫn còn sản phẩm thuộc danh mục này.");
        }
        if (StringUtils.hasText(categoryToDelete.getHinhAnh())) {
            storageService.deleteFile(categoryToDelete.getHinhAnh());
        }
        categoryRepository.delete(categoryToDelete);
    }
    
    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}
