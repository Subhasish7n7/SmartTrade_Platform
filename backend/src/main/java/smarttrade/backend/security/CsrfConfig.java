package smarttrade.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class CsrfConfig {

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {

        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookiePath("/");

        repository.setCookieCustomizer(cookie -> cookie
                        .secure(secureCookie)
                        .sameSite(sameSite)
        );

        return repository;
    }
}