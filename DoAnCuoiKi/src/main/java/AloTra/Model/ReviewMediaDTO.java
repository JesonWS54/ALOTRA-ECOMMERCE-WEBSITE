package AloTra.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMediaDTO {
    private Long id;
    private Long reviewId;
    private String mediaUrl;
    private String mediaType;
}