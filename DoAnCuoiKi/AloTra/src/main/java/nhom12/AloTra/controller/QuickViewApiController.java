package nhom12.AloTra.controller;

import nhom12.AloTra.dto.QuickViewDTO;
import nhom12.AloTra.service.QuickViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quick-view")
public class QuickViewApiController {

    private final QuickViewService qs;

    public QuickViewApiController(QuickViewService qs) {
        this.qs = qs;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> quickView(@PathVariable Integer id) {
        QuickViewDTO dto = qs.build(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }
}
