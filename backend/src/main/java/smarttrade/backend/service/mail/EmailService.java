package smarttrade.backend.service.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Getter
    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.relay-url:}")
    private String relayUrl;

    @Value("${app.mail.relay-secret:}")
    private String relaySecret;

    @Value("${frontend.url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(
            String email,
            String displayName,
            String rawToken
    ) {
        if (!enabled) {
            throw new IllegalStateException(
                    "Outbound email is disabled"
            );
        }

        String resetUrl = frontendUrl.replaceAll("/+$", "")
                + "/reset-password?token="
                + URLEncoder.encode(
                rawToken,
                StandardCharsets.UTF_8
        );

        String name = (displayName == null || displayName.isBlank())
                ? "there"
                : displayName.trim();

        String html = """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#222">
                  <h2>Reset your SmartTrade password</h2>
                  <p>Hi %s,</p>
                  <p>A password reset was requested for your SmartTrade account.</p>
                  <p><a href="%s">Reset password</a></p>
                  <p>This link expires in 30 minutes and can only be used once.</p>
                  <p>If you did not request this, you can ignore this email.</p>
                </div>
                """.formatted(escapeHtml(name), escapeHtml(resetUrl));

        sendHtmlEmail(
                email,
                "Reset your SmartTrade password",
                html
        );
    }

    public void sendLoginAlertEmail(
            String email,
            String displayName
    ) {
        if (!enabled) {
            return;
        }

        String name = (displayName == null || displayName.isBlank())
                ? "there"
                : displayName.trim();

        String html = """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#222">
                  <p>Hi %s,</p>
                  <p>Your SmartTrade account was just signed in to.</p>
                  <p>If this wasn't you, change your password immediately.</p>
                </div>
                """.formatted(escapeHtml(name));

        try {
            sendHtmlEmail(
                    email,
                    "New sign-in to your SmartTrade account",
                    html
            );
        } catch (RuntimeException ex) {
            // A login-alert delivery failure must not break login.
            log.warn(
                    "Could not send SmartTrade login alert email",
                    ex
            );
        }
    }

    private void sendHtmlEmail(
            String email,
            String subject,
            String html
    ) {
        if (relayUrl == null || relayUrl.isBlank()) {
            throw new IllegalStateException(
                    "MAIL_RELAY_URL must be configured when email is enabled"
            );
        }

        if (relaySecret == null || relaySecret.isBlank()) {
            throw new IllegalStateException(
                    "MAIL_RELAY_SECRET must be configured when email is enabled"
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email recipient must not be blank"
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "secret", relaySecret,
                "to", email,
                "subject", subject,
                "html", html
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(payload, headers);

        String responseBody;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    relayUrl,
                    request,
                    String.class
            );

            if (response.getStatusCode().is3xxRedirection()) {
                // Apps Script ContentService redirects its response.
                URI redirectUri = response.getHeaders().getLocation();

                if (redirectUri == null) {
                    throw new IllegalStateException(
                            "Email relay redirected without a Location header"
                    );
                }

                // Follow the response redirect with GET.
                ResponseEntity<String> redirectedResponse =
                        restTemplate.getForEntity(
                                redirectUri,
                                String.class
                        );

                if (!redirectedResponse.getStatusCode().is2xxSuccessful()) {
                    throw new IllegalStateException(
                            "Email relay response redirect failed with HTTP "
                                    + redirectedResponse.getStatusCode().value()
                    );
                }

                responseBody = redirectedResponse.getBody();

            } else if (response.getStatusCode().is2xxSuccessful()) {
                responseBody = response.getBody();

            } else {
                throw new IllegalStateException(
                        "Email relay returned HTTP "
                                + response.getStatusCode().value()
                );
            }

        } catch (RestClientException ex) {
            log.error(
                    "Could not contact the Google Apps Script email relay",
                    ex
            );

            throw new IllegalStateException(
                    "Could not contact the email relay",
                    ex
            );
        }

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException(
                    "Email relay returned an empty response"
            );
        }

        final JsonNode result;

        try {
            result = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException ex) {
            log.error("Email relay returned an invalid JSON response");

            throw new IllegalStateException(
                    "Email relay returned an invalid response",
                    ex
            );
        }

        if (!result.path("success").asBoolean(false)) {
            String error = result.path("error")
                    .asText("Unknown email relay error");

            throw new IllegalStateException(
                    "Email relay rejected the request: " + error
            );
        }

        log.info("SmartTrade email relay accepted the email request");
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}