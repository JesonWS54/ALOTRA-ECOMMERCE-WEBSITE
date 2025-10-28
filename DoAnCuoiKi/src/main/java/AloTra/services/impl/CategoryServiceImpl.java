package AloTra.services.impl;

import AloTra.Model.CategoryDTO;
import AloTra.entity.Category;
import AloTra.repository.CategoryRepository;
import AloTra.repository.ProductRepository;
import AloTra.services.CategoryService;
import AloTra.services.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private ProductRepository productRepository;

    // --- USER/VENDOR METHODS ---
    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    // --- ADMIN METHODS ---
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategoriesAdmin(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getRootCategoriesAdmin() {
        return categoryRepository.findByParentIsNullAndIsActiveTrue(Sort.by("name"))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryByIdAdmin(Long categoryId) {
        return categoryRepository.findById(categoryId).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CategoryDTO addCategoryAdmin(CategoryDTO categoryDTO, MultipartFile imageFile) throws IOException {
        if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new RuntimeException("Tên danh mục '" + categoryDTO.getName() + "' đã tồn tại.");
        }

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setIsActive(categoryDTO.getIsActive() != null ? categoryDTO.getIsActive() : true);

        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục cha với ID: " + categoryDTO.getParentId()));
            category.setParent(parent);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(imageFile);
            category.setImageUrl((String) uploadResult.get("secure_url"));
        }

        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategoryAdmin(Long categoryId, CategoryDTO categoryDTO, MultipartFile imageFile) throws IOException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));

        if (!category.getName().equalsIgnoreCase(categoryDTO.getName()) &&
            categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new RuntimeException("Tên danh mục '" + categoryDTO.getName() + "' đã tồn tại.");
        }

        category.setName(categoryDTO.getName());
        // *** SỬA LỖI: Dùng getIsActive() ***
        category.setIsActive(categoryDTO.getIsActive() != null ? categoryDTO.getIsActive() : category.getIsActive()); // Giữ nguyên nếu null

        if (categoryDTO.getParentId() != null) {
            if (categoryDTO.getParentId().equals(category.getId())) {
                throw new RuntimeException("Không thể đặt danh mục làm cha của chính nó.");
            }
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục cha với ID: " + categoryDTO.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(imageFile);
            category.setImageUrl((String) uploadResult.get("secure_url"));
        }

        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategoryAdmin(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));

        List<Category> subCategories = categoryRepository.findByParent_Id(categoryId);
        if (subCategories != null && !subCategories.isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục vì còn chứa danh mục con.");
        }

        boolean hasProducts = productRepository.existsByCategory_Id(categoryId);
        if (hasProducts) {
            throw new RuntimeException("Không thể xóa danh mục vì còn chứa sản phẩm.");
        }

        categoryRepository.delete(category);
    }

    // --- Helper Method ---
    private CategoryDTO convertToDTO(Category category) {
        if (category == null) return null;
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setImageUrl(category.getImageUrl());
        // *** SỬA LỖI: Dùng getIsActive() ***
        dto.setIsActive(category.getIsActive());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

         if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
             dto.setSubCategories(
                 category.getSubCategories().stream()
                         .map(this::convertToBasicDTO)
                         .collect(Collectors.toList())
             );
         } else {
             dto.setSubCategories(new ArrayList<>());
         }

        return dto;
    }

    private CategoryDTO convertToBasicDTO(Category category) {
        if (category == null) return null;
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}

