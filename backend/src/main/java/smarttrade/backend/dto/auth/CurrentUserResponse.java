package smarttrade.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CurrentUserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNo;
}