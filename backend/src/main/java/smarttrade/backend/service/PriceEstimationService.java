package smarttrade.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    public Integer fetchEstimatedNewPrice(
            String itemName,
            ItemCategory category,
            String customCategory,
            List<String> labels
    ) {

        if (itemName == null || itemName.isBlank()) {
            log.warn(
                    "Skipping SerpAPI price lookup because item name is missing"
            );
            return null;
        }

        if (!apiEnabled) {
            log.debug(
                    "SerpAPI pricing is disabled"
            );
            return null;
        }

        if (serpApiKey == null || serpApiKey.isBlank()) {
            log.warn(
                    "SerpAPI pricing is enabled but serp.api.key is missing"
            );
            return null;
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
                    .queryParam("engine", "google_shopping")
                    .queryParam("q", query)
                    .queryParam("google_domain", googleDomain)
                    .queryParam("gl", googleCountry)
                    .queryParam("hl", googleLanguage)
                    .queryParam("api_key", serpApiKey)
                    .build()
                    .encode()
                    .toUriString();

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

            JsonNode body = response.getBody();

            if (body == null) {
                log.warn(
                        "SerpAPI returned an empty response for query '{}'",
                        query
                );
                return null;
            }

            JsonNode error = body.path("error");

            if (!error.isMissingNode() && !error.isNull()) {
                log.warn(
                        "SerpAPI rejected price lookup for query '{}': {}",
                        query,
                        error.asText()
                );
                return null;
            }

            JsonNode shoppingResults = body.path("shopping_results");

            if (!shoppingResults.isArray()) {
                log.warn(
                        "SerpAPI returned no shopping_results for query '{}'",
                        query
                );
                return null;
            }

            List<Double> prices = new ArrayList<>();

            for (JsonNode result : shoppingResults) {

                // We are estimating NEW retail price.
                // Google Shopping exposes second_hand_condition for
                // second-hand/refurbished results.
                String secondHandCondition =
                        result.path("second_hand_condition")
                                .asText("");

                if (!secondHandCondition.isBlank()) {
                    continue;
                }

                double price = result.path("extracted_price")
                                .asDouble(Double.NaN);

                if (!Double.isFinite(price) || price <= 0) {
                    continue;
                }

                prices.add(price);
            }

            if (prices.isEmpty()) {
                log.warn(
                        "No usable new-product prices found for query '{}'",
                        query
                );
                return null;
            }

            Collections.sort(prices);

            double median;

            int middle = prices.size() / 2;

            if (prices.size() % 2 == 0) {

                median = (prices.get(middle - 1) + prices.get(middle)) / 2.0;

            } else {
                median = prices.get(middle);
            }

            if (median > Integer.MAX_VALUE) {
                log.warn(
                        "SerpAPI median price exceeded Integer range for query '{}'",
                        query
                );
                return null;
            }

            return (int) Math.round(median);

        } catch (Exception e) {
            log.error(
                    "SerpAPI price lookup failed for query '{}'",
                    query,
                    e
            );
            return null;
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

        if (category != null) {
            if (customCategory != null
                    && !customCategory.isBlank()) {

                parts.add(customCategory.trim());

            } else {
                parts.add(category.name().toLowerCase(Locale.ROOT));
            }
        }

        if (labels != null) {
            labels.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(label -> !label.isBlank())
                    .distinct()
                    .limit(5)
                    .forEach(parts::add);
        }

        // We want NEW retail listings, not used/refurbished listings.
        parts.add("new");

        return parts.stream().collect(Collectors.joining(" "));
    }

    public Integer generateUsedPrice(Integer newPrice, ItemCondition condition) {

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

        return (int) Math.round(newPrice * multiplier);
    }
}