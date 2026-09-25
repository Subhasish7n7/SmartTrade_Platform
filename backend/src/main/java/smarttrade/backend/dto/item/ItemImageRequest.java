package smarttrade.backend.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemImageRequest {
    private Long id;
    @NotBlank
    private String secureUrl;
    @NotBlank
    private String publicId;
    @NotBlank
    private String assetId;
    @NotNull
    private Integer width;
    @NotNull
    private Integer height;
    @NotBlank
    private String format;
    @NotNull
    private Long bytes;
    @NotNull
    private Integer sortOrder;
}