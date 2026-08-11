package smarttrade.backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        Cookie[] cookies = servletRequest.getServletRequest().getCookies();

        if (cookies == null) {
            return false;
        }

        String token = null;

        for (Cookie cookie : cookies) {

            if (CookieService.ACCESS_COOKIE.equals(cookie.getName())) {
                token = cookie.getValue();
                break;
            }

        }

        if (token == null) {
            return false;
        }

        try {

            String email = jwtService.extractEmail(token);

            UserDetails user = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(token, email)) {
                return false;
            }

            attributes.put(
                    "authentication",
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    )
            );

            return true;

        } catch (Exception ex) {

            return false;

        }

    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {

    }

}