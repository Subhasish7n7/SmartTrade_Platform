
package smarttrade.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

import java.util.function.Supplier;

public final class SpaCsrfTokenRequestHandler
        implements CsrfTokenRequestHandler {

    public static final String RAW_CSRF_TOKEN_ATTRIBUTE =
            SpaCsrfTokenRequestHandler.class.getName()
                    + ".RAW_CSRF_TOKEN";

    private final CsrfTokenRequestHandler plain =
            new CsrfTokenRequestAttributeHandler();

    private final CsrfTokenRequestHandler xor =
            new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Supplier<CsrfToken> csrfToken
    ) {
        // Resolve the actual, unmasked token.
        // This also forces deferred token generation when needed.
        CsrfToken rawToken = csrfToken.get();

        // Keep the raw token available to our CSRF bootstrap endpoint.
        request.setAttribute(
                RAW_CSRF_TOKEN_ATTRIBUTE,
                rawToken.getToken()
        );

        // Preserve Spring Security's XOR masking for request attributes.
        xor.handle(request, response, () -> rawToken);
    }

    @Override
    public String resolveCsrfTokenValue(
            HttpServletRequest request,
            CsrfToken csrfToken
    ) {
        String headerValue =
                request.getHeader(csrfToken.getHeaderName());

        if (headerValue != null && !headerValue.isBlank()) {
            // Our SPA sends the raw token through the header.
            return plain.resolveCsrfTokenValue(
                    request,
                    csrfToken
            );
        }

        // Retain XOR resolution for form or request-parameter cases.
        return xor.resolveCsrfTokenValue(
                request,
                csrfToken
        );
    }
}
