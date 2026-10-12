package smarttrade.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminActionRequest(
        @NotBlank @Size(max = 2000) String reason
) {}