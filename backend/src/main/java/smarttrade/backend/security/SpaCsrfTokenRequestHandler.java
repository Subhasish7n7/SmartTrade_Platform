package smarttrade.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

import java.util.function.Supplier;

public final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();

    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Supplier<CsrfToken> csrfToken
    ) {
        xor.handle(request, response, csrfToken);

        // Force token generation so CookieCsrfTokenRepository
        // writes the XSRF-TOKEN cookie for the SPA.
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(
            HttpServletRequest request,
            CsrfToken csrfToken
    ) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());

        if (headerValue != null && !headerValue.isBlank()) {
            // Axios sends the raw CSRF token in X-XSRF-TOKEN.
            return plain.resolveCsrfTokenValue(
                    request,
                    csrfToken
            );
        }

        // Keep XOR handling for request-parameter/server-rendered cases.
        return xor.resolveCsrfTokenValue(
                request,
                csrfToken
        );
    }
}