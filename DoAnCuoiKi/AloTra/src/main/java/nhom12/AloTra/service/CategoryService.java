package nhom12.AloTra.service;

import nhom12.AloTra.entity.Category;
import nhom12.AloTra.request.CategoryRequest;
import org.springframework.data.domain.Page;

import java.util.List;
public interface CategoryService {
    List<Category> findAll();
    Page<Category> searchAndFilter(String keyword, Boolean status, int page, int size);
    Category findById(int id);
    void save(CategoryRequest categoryRequest);
    void delete(int id);
}
