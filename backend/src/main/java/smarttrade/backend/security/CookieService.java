package smarttrade.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public static final String ACCESS_COOKIE = "access_token";

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    public void setJwtCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie =
                ResponseCookie.from(ACCESS_COOKIE, token)
                        .httpOnly(true)
                        .secure(secureCookie)
                        .sameSite(sameSite)
                        .path("/")
                        .maxAge(60 * 60 * 24)
                        .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearJwtCookie(HttpServletResponse response) {

        ResponseCookie cookie =
                ResponseCookie.from(ACCESS_COOKIE, "")
                        .httpOnly(true)
                        .secure(secureCookie)
                        .sameSite(sameSite)
                        .path("/")
                        .maxAge(0)
                        .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}