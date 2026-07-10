package smarttrade.backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private String getJwt(HttpServletRequest request) {

        if (request.getCookies() == null)
            return null;

        for (Cookie cookie : request.getCookies()) {

            if (CookieService.ACCESS_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        /*
            Extract Authorization header.

            Expected format:
            Authorization: Bearer TOKEN
        */

        String token = getJwt(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {

            String email = jwtService.extractEmail(token);

        /*
            Authenticate only if:
            - email exists
            - user not already authenticated
        */
        if (email != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            /*
                Validate token.
            */
            if (jwtService.isTokenValid(token, userDetails.getUsername())) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                /*
                    Save authenticated user into Spring Security context.

                    This is what makes:
                    SecurityContextHolder.getContext().getAuthentication()
                    work later.
                */
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    } catch (JwtException e) {
        SecurityContextHolder.clearContext();
    }
        filterChain.doFilter(request, response);
    }
}
