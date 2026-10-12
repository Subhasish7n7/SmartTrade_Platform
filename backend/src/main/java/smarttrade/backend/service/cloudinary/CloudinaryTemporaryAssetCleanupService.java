package smarttrade.backend.service.cloudinary;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

@Service
@Slf4j
public class CloudinaryTemporaryAssetCleanupService {

    private final RestTemplate restTemplate;
    private final CloudinaryAssetService cloudinaryAssetService;
    private final CloudinaryAssetReferenceService assetReferenceService;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.temporary-tag:smarttrade_temp}")
    private String temporaryTag;

    @Value("${cloudinary.cleanup.retention-hours:72}")
    private long retentionHours;

    public CloudinaryTemporaryAssetCleanupService(
            RestTemplate restTemplate,
            CloudinaryAssetService cloudinaryAssetService,
            CloudinaryAssetReferenceService assetReferenceService
    ) {
        this.restTemplate = restTemplate;
        this.cloudinaryAssetService = cloudinaryAssetService;
        this.assetReferenceService = assetReferenceService;
    }

    @Scheduled(
            fixedDelayString = "${cloudinary.cleanup.fixed-delay-ms:3600000}"
    )
    public void cleanupUnpersistedAssets() {

        if (cloudName == null || cloudName.isBlank()
                || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            return;
        }

        try {
            String url = UriComponentsBuilder
                    .fromUriString(
                            "https://api.cloudinary.com/v1_1/{cloudName}/resources/image/tags/{tag}"
                    )
                    .queryParam("max_results", 100)
                    .buildAndExpand(cloudName, temporaryTag)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(apiKey, apiSecret);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    JsonNode.class
            );

            JsonNode resources = response.getBody() == null
                    ? null
                    : response.getBody().path("resources");

            if (resources == null || !resources.isArray()) {
                return;
            }

            Instant cutoff =
                    Instant.now().minusSeconds(retentionHours * 3600);

            for (JsonNode resource : resources) {

                String assetId =
                        resource.path("asset_id").asText(null);

                String createdAt =
                        resource.path("created_at").asText(null);

                if (assetId == null || assetId.isBlank()
                        || createdAt == null || createdAt.isBlank()) {
                    continue;
                }

                Instant created;

                try {
                    created = Instant.parse(createdAt);
                } catch (Exception ignored) {
                    continue;
                }

                if (created.isAfter(cutoff)) {
                    continue;
                }

                /*
                 * An asset is considered safe only when neither an item
                 * image nor a user avatar references it.
                 */
                if (assetReferenceService.existsByAssetId(assetId)) {
                    continue;
                }

                try {
                    cloudinaryAssetService.deleteByAssetId(assetId);
                } catch (Exception e) {
                    log.warn(
                            "Temporary Cloudinary asset {} could not be deleted; will retry",
                            assetId,
                            e
                    );
                }
            }

        } catch (Exception e) {
            log.warn(
                    "Cloudinary temporary asset cleanup run failed",
                    e
            );
        }
    }
}