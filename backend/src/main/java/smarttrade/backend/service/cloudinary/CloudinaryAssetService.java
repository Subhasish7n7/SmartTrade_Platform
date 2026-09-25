package smarttrade.backend.service.cloudinary;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CloudinaryAssetService {

    private final RestTemplate restTemplate;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    public CloudinaryAssetService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void deleteByAssetId(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("Cloudinary asset ID is required");
        }
        requireConfiguration();

        long timestamp = Instant.now().getEpochSecond();
        Map<String, String> signedParams = new TreeMap<>();
        signedParams.put("asset_id", assetId);
        signedParams.put("invalidate", "true");
        signedParams.put("timestamp", String.valueOf(timestamp));

        String canonicalParams = signedParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        String signature = sha1Hex(canonicalParams + apiSecret);

        String url = UriComponentsBuilder
                .fromUriString(
                        "https://api.cloudinary.com/v1_1/{cloudName}/asset/destroy"
                )
                .buildAndExpand(cloudName)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("asset_id", assetId);
        form.add("timestamp", String.valueOf(timestamp));
        form.add("invalidate", "true");
        form.add("api_key", apiKey);
        form.add("signature", signature);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                url, new HttpEntity<>(form, headers), JsonNode.class
        );

        JsonNode body = response.getBody();
        String result = body == null ? null : body.path("result").asText(null);

        if ("ok".equalsIgnoreCase(result) || "not found".equalsIgnoreCase(result)) {
            return;
        }

        throw new IllegalStateException(
                "Cloudinary asset deletion did not succeed"
        );
    }

    private void requireConfiguration() {
        if (cloudName == null || cloudName.isBlank()
                || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Cloudinary server credentials are not configured"
            );
        }
    }

    private String sha1Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : hash) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is unavailable", e);
        }
    }
}