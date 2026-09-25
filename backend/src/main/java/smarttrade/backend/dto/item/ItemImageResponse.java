package smarttrade.backend.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemImageResponse {
    private Long id;
    private String secureUrl;
    private String publicId;
    private String assetId;
    private Integer width;
    private Integer height;
    private String format;
    private Long bytes;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}