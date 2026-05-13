package smarttrade.backend.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${frontend.url}")
    private String frontendUrl;

    /*
        Custom JWT authentication filter.

        This filter:
        - Reads JWT token from Authorization header
        - Validates token
        - Sets authenticated user in SecurityContext
    */
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                /*
                    Disable CSRF.

                    Why?
                    JWT APIs are stateless.

                    CSRF protection is mainly needed for
                    session/cookie-based authentication.
                */
                .csrf(csrf -> csrf.disable())

                /*
                    Enable CORS using the configuration
                    defined in corsConfigurationSource() bean.

                    Required for frontend-backend communication
                    across different origins.

                    Example:
                    Frontend -> localhost:3000
                    Backend  -> localhost:8080
                */
                .cors(cors -> {})

                /*
                    Stateless session policy.

                    Why?
                    JWT already stores authentication data.

                    Server does NOT keep session state.
                */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                            Allow browser preflight requests.

                            Browsers send OPTIONS requests before
                            actual authenticated requests.

                            Without this, CORS fails.
                        */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        /*
                            Public authentication routes.

                            Examples:
                            - login
                            - register
                        */
                        .requestMatchers(
                                "/auth/**"
                        ).permitAll()

                        /*
                            Public item browsing endpoints.
                        */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/items",
                                "/items/*",
                                "/items/search",
                                "/items/nearby"
                        ).permitAll()
                        /*
                            All other endpoints require JWT authentication.
                        */
                        .anyRequest().authenticated()
                )

                /*
                    Add JWT filter before Spring Security's
                    default authentication filter.

                    This ensures JWT validation happens first.
                */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        /*
            CORS configuration.

            Defines which frontend origins
            can access backend APIs.
        */
        CorsConfiguration configuration =
                new CorsConfiguration();

        /*
            Allowed frontend origins.

            Development frontend:
            React/Vite -> localhost:3000
        */
        configuration.setAllowedOrigins(
                List.of(frontendUrl)
        );

        /*
            Allowed HTTP methods.
        */
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        /*
            Allowed request headers.

            Includes:
            - Authorization
            - Content-Type
            - etc.
        */
        configuration.setAllowedHeaders(
                List.of("*")
        );

        /*
            Allow credentials.

            Useful for:
            - cookies
            - authorization headers
            - authenticated requests
        */
        configuration.setAllowCredentials(true);

        /*
            Apply this CORS configuration
            to all API endpoints.
        */
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    /*
        Password hashing bean.

        NEVER store plain text passwords.
    */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
        Authentication manager bean.

        Used during login authentication.
    */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }
}