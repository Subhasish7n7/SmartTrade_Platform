package smarttrade.backend.service.pricing;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceEstimationService {

    @Value("${serp.api.key:}")
    private String serpApiKey;

    @Value("${serp.api.enabled:false}")
    private boolean apiEnabled;

    @Value("${serp.api.google-domain:google.co.in}")
    private String googleDomain;

    @Value("${serp.api.gl:in}")
    private String googleCountry;

    @Value("${serp.api.hl:en}")
    private String googleLanguage;

    private final RestTemplate restTemplate;

    public PricingFetchResult fetchEstimatedNewPrice(
            String itemName,
            ItemCategory category,
            String customCategory,
            List<String> labels
    ) {

        if (itemName == null || itemName.isBlank()) {
            return PricingFetchResult.failure(
                    PricingFetchStatus.INVALID_CONFIGURATION,
                    "ITEM_NAME_MISSING",
                    "Item name is required for market pricing.",
                    null
            );
        }

        if (!apiEnabled) {
            log.warn("SerpAPI pricing is disabled.");
            return PricingFetchResult.failure(
                    PricingFetchStatus.INVALID_CONFIGURATION,
                    "SERP_API_DISABLED",
                    "Market pricing is currently disabled.",
                    null
            );
        }

        if (serpApiKey == null || serpApiKey.isBlank()) {
            log.error(
                    "SerpAPI pricing is enabled but SERP_API_KEY is missing."
            );

            return PricingFetchResult.failure(
                    PricingFetchStatus.INVALID_CONFIGURATION,
                    "SERP_API_KEY_MISSING",
                    "Market pricing is not configured correctly.",
                    null
            );
        }

        String query = buildShoppingQuery(
                itemName,
                category,
                customCategory,
                labels
        );

        try {
            String url = UriComponentsBuilder
                    .fromUriString("https://serpapi.com/search")
                    .queryParam("engine", "google_shopping_light")
                    .queryParam("q", query)
                    .queryParam("google_domain", googleDomain)
                    .queryParam("gl", googleCountry)
                    .queryParam("hl", googleLanguage)
                    .queryParam("api_key", serpApiKey)
                    .build()
                    .encode()
                    .toUriString();

            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(
                            url,
                            JsonNode.class
                    );

            JsonNode body = response.getBody();

            if (body == null || body.isNull()) {
                log.warn(
                        "SerpAPI returned an empty body for query '{}'",
                        query
                );

                return PricingFetchResult.failure(
                        PricingFetchStatus.PROVIDER_ERROR,
                        "EMPTY_PROVIDER_RESPONSE",
                        "The pricing provider returned an empty response.",
                        response.getStatusCode().value()
                );
            }

            JsonNode error = body.path("error");

            if (!error.isMissingNode()
                    && !error.isNull()
                    && !error.asText().isBlank()) {

                String providerMessage = error.asText();

                log.warn(
                        "SerpAPI returned an API error for query '{}': {}",
                        query,
                        providerMessage
                );

                return PricingFetchResult.failure(
                        PricingFetchStatus.PROVIDER_ERROR,
                        "SERP_API_ERROR",
                        "The market pricing provider returned an error.",
                        response.getStatusCode().value()
                );
            }

            JsonNode shoppingResults = body.path("shopping_results");

            if (!shoppingResults.isArray()) {
                log.warn(
                        "SerpAPI response contains no shopping_results for query '{}'",
                        query
                );

                return PricingFetchResult.failure(
                        PricingFetchStatus.NO_RESULTS,
                        "SHOPPING_RESULTS_MISSING",
                        "No retail market results were returned for this item.",
                        response.getStatusCode().value()
                );
            }

            List<Double> prices = new ArrayList<>();

            for (JsonNode result : shoppingResults) {

                /*
                 * We estimate the NEW retail reference price.
                 * Exclude results explicitly marked as second-hand/refurbished.
                 */
                String secondHandCondition =
                        result.path("second_hand_condition")
                                .asText("");

                if (!secondHandCondition.isBlank()) {
                    continue;
                }

                double price =
                        result.path("extracted_price")
                                .asDouble(Double.NaN);

                if (!Double.isFinite(price) || price <= 0) {
                    continue;
                }

                prices.add(price);
            }

            if (prices.isEmpty()) {
                log.warn(
                        "SerpAPI returned shopping results but no usable new-product prices for query '{}'",
                        query
                );

                return PricingFetchResult.failure(
                        PricingFetchStatus.NO_RESULTS,
                        "NO_USABLE_PRICES",
                        "No usable new-product prices were found for this item.",
                        response.getStatusCode().value()
                );
            }

            Collections.sort(prices);

            double median;

            int middle = prices.size() / 2;

            if (prices.size() % 2 == 0) {
                median =
                        (prices.get(middle - 1) + prices.get(middle))
                                / 2.0;
            } else {
                median = prices.get(middle);
            }

            if (!Double.isFinite(median)
                    || median <= 0
                    || median > Integer.MAX_VALUE) {

                log.warn(
                        "SerpAPI median is outside supported range for query '{}': {}",
                        query,
                        median
                );

                return PricingFetchResult.failure(
                        PricingFetchStatus.NO_RESULTS,
                        "INVALID_MEDIAN",
                        "The returned market prices could not be used safely.",
                        response.getStatusCode().value()
                );
            }

            return PricingFetchResult.success(
                    (int) Math.round(median)
            );

        } catch (HttpStatusCodeException e) {

            int status = e.getStatusCode().value();

            String providerBody =
                    e.getResponseBodyAsString();

            log.error(
                    "SerpAPI HTTP failure. status={}, query={}, response={}",
                    status,
                    query,
                    providerBody,
                    e
            );

            if (status == 401 || status == 403) {
                return PricingFetchResult.failure(
                        PricingFetchStatus.AUTHENTICATION_FAILED,
                        "SERP_API_AUTHENTICATION",
                        "The market pricing provider rejected the API credentials.",
                        status
                );
            }

            if (status == 429) {
                return PricingFetchResult.failure(
                        PricingFetchStatus.RATE_LIMITED,
                        "SERP_API_RATE_LIMITED",
                        "The market pricing provider rate limit was reached.",
                        status
                );
            }

            if (status >= 400 && status < 500) {
                return PricingFetchResult.failure(
                        PricingFetchStatus.BAD_REQUEST,
                        "SERP_API_BAD_REQUEST",
                        "The market pricing request was rejected by the provider.",
                        status
                );
            }

            return PricingFetchResult.failure(
                    PricingFetchStatus.PROVIDER_ERROR,
                    "SERP_API_HTTP_" + status,
                    "The market pricing provider returned an HTTP " + status + " error.",
                    status
            );

        } catch (ResourceAccessException e) {

            log.error(
                    "SerpAPI network/timeout failure for query '{}'",
                    query,
                    e
            );

            return PricingFetchResult.failure(
                    PricingFetchStatus.NETWORK_ERROR,
                    "SERP_API_NETWORK_ERROR",
                    "The market pricing provider could not be reached.",
                    null
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected SerpAPI pricing failure for query '{}'",
                    query,
                    e
            );

            return PricingFetchResult.failure(
                    PricingFetchStatus.PROVIDER_ERROR,
                    "SERP_API_UNEXPECTED_ERROR",
                    "The market pricing provider could not complete the lookup.",
                    null
            );
        }
    }

    private String buildShoppingQuery(
            String itemName,
            ItemCategory category,
            String customCategory,
            List<String> labels
    ) {
        List<String> parts = new ArrayList<>();

        parts.add(itemName.trim());

        if (customCategory != null && !customCategory.isBlank()) {
            parts.add(customCategory.trim());
        } else if (category != null) {
            parts.add(category.name().toLowerCase(Locale.ROOT));
        }

        return parts.stream()
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .distinct()
                .collect(Collectors.joining(" "));
    }

    public Integer generateUsedPrice(
            Integer newPrice,
            ItemCondition condition
    ) {

        if (newPrice == null || condition == null) {
            return null;
        }

        double multiplier = switch (condition) {
            case NEW -> 0.95;
            case LIKE_NEW -> 0.85;
            case GOOD -> 0.70;
            case FAIR -> 0.50;
            case POOR -> 0.35;
        };

        return (int) Math.round(
                newPrice * multiplier
        );
    }
}