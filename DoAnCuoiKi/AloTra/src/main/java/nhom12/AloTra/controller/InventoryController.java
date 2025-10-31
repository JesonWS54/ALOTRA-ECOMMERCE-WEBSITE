package nhom12.AloTra.controller;

import nhom12.AloTra.entity.Inventory;
import nhom12.AloTra.repository.InventoryRepository;
import nhom12.AloTra.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public String listInventory(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String sort,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "5") int size,
                                Model model) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Specification<Inventory> spec = (root, query, cb) -> cb.conjunction();
        if (StringUtils.hasText(keyword)) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("sanPham").get("tenSanPham"), "%" + keyword + "%")
            );
        }

        Page<Inventory> inventoryPage = inventoryService.findAll(keyword, sort, page, size);
        model.addAttribute("inventoryPage", inventoryPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "admin/warehouse/inventory";
    }
}
