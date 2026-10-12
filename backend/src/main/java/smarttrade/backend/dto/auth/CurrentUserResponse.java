package smarttrade.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserRole;

@Data
@AllArgsConstructor
@Builder
public class CurrentUserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNo;
    private String avatarUrl;
    private UserRole role;
    private AccountStatus accountStatus;
}