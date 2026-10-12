package smarttrade.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAvatarRequest {

    @NotBlank
    private String secureUrl;

    @NotBlank
    private String publicId;

    @NotBlank
    private String assetId;

    @NotNull
    @Positive
    private Integer width;

    @NotNull
    @Positive
    private Integer height;

    @NotBlank
    private String format;

    @NotNull
    @Positive
    private Long bytes;
}