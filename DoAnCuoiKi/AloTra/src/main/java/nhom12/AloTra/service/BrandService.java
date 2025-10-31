package nnhom12.AloTra.service;

import nhom12.AloTra.entity.Brand;
import nhom12.AloTra.request.BrandRequest;
import org.springframework.data.domain.Page;

public interface BrandService {
    Page<Brand> searchAndFilter(String keyword, Boolean status, int page, int size);
    Brand findById(int id);
    void save(BrandRequest brandRequest);
    void delete(int id);
}
