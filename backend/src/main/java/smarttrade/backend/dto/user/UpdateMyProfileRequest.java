package smarttrade.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMyProfileRequest {

    private String name;

    @JsonAlias("phone_no")
    private String phoneNo;

    @Valid
    private UserAvatarRequest avatar;

    private boolean clearAvatar = false;
}