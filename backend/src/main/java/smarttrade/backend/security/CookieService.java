package smarttrade.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public static final String ACCESS_COOKIE = "access_token";

    public void setJwtCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie = ResponseCookie.from(ACCESS_COOKIE, token)
                .httpOnly(true)
                .secure(false)      // true in production HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(60 * 60 * 24)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearJwtCookie(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from(ACCESS_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}