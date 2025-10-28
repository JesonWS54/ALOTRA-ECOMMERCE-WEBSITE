package AloTra.Model;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Long parentId;
    private String parentName;
    private Boolean isActive;
    private List<CategoryDTO> subCategories;
}