package nhom12.AloTra.service;

import nhom17.OneShop.entity.Inventory;
import org.springframework.data.domain.Page;

public interface InventoryService {
    Page<Inventory> findAll(String keyword, String sort, int page, int size);
}
