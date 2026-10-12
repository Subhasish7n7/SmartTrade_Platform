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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    /**
     * Optional geographic context for Google Shopping.
     * <p>
     * SerpApi documents that omitting location can make the search inherit the
     * proxy's location, which can change the Shopping result set. Keep this
     * configurable rather than hard-coding a city into the pricing service.
     */
    @Value("${serp.api.location:}")
    private String searchLocation;

    /**
     * Number of product-family matches we consider enough to avoid a fallback query.
     */
    private static final int MIN_FAMILY_MATCHES = 5;

    private static final List<String> GENERIC_QUERY_WORDS = List.of(
            "the", "a", "an", "with", "for", "and", "of", "new", "latest",
            "sale", "buy", "best", "online", "official", "free", "shipping",
            "gaming", "laptop", "notebook", "computer", "pc", "desktop",
            "device", "product", "portable", "wireless", "smart", "original",
            "edition", "model"
    );

    private final RestTemplate restTemplate;
    private final PriceCandidateMatcher priceCandidateMatcher;
    private final WeightedPriceAggregator weightedPriceAggregator;

    /**
     * Backward-compatible overload for callers/tests that do not yet supply
     * structured specifications.
     */
    public PricingFetchResult fetchEstimatedNewPrice(
            String itemName,
            ItemCategory category,
            String customCategory,
            List<String> labels
    ) {
        return fetchEstimatedNewPrice(
                itemName,
                category,
                customCategory,
                labels,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public PricingFetchResult fetchEstimatedNewPrice(
            String itemName,
            ItemCategory category,
            String customCategory,
            List<String> labels,
            List<String> primarySpecifications,
            List<String> secondarySpecifications,
            List<String> tertiarySpecifications
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
            log.error("SerpAPI pricing is enabled but SERP_API_KEY is missing.");
            return PricingFetchResult.failure(
                    PricingFetchStatus.INVALID_CONFIGURATION,
                    "SERP_API_KEY_MISSING",
                    "Market pricing is not configured correctly.",
                    null
            );
        }

        String query = buildShoppingQuery(itemName);

        try {
            ShoppingFetchResult primaryFetch = fetchShoppingCandidates(query);

            if (!primaryFetch.success()) {
                return primaryFetch.failureResult();
            }

            List<ShoppingCandidate> candidates = deduplicate(primaryFetch.candidates());
            int familyMatches = countProductFamilyMatches(itemName, candidates);

            log.info(
                    "SerpAPI primary pricing search: query='{}', results={}, familyMatches={}, location='{}', searchId={}",
                    query,
                    candidates.size(),
                    familyMatches,
                    searchLocation,
                    primaryFetch.searchId()
            );

            /*
             * Google Shopping can return a broad or shifted candidate set for
             * the same text query. If the first result set does not contain
             * enough members of the requested product family, do one narrower
             * fallback query and merge both pools.
             */
            if (familyMatches < MIN_FAMILY_MATCHES) {
                String fallbackQuery = buildFallbackShoppingQuery(itemName);

                if (!fallbackQuery.isBlank() && !fallbackQuery.equalsIgnoreCase(query)) {
                    ShoppingFetchResult fallbackFetch = fetchShoppingCandidates(fallbackQuery);

                    if (fallbackFetch.success()) {
                        candidates = deduplicate(merge(candidates, fallbackFetch.candidates()));

                        log.info(
                                "SerpAPI fallback pricing search: query='{}', results={}, familyMatches={}, searchId={}",
                                fallbackQuery,
                                fallbackFetch.candidates().size(),
                                countProductFamilyMatches(itemName, fallbackFetch.candidates()),
                                fallbackFetch.searchId()
                        );
                    } else {
                        // The primary search already returned usable data, so a
                        // failed fallback must not destroy the first result set.
                        log.warn(
                                "SerpAPI fallback search failed for '{}'; continuing with primary candidates. code={}",
                                fallbackQuery,
                                fallbackFetch.failureResult() == null
                                        ? "UNKNOWN"
                                        : fallbackFetch.failureResult().errorCode()
                        );
                    }
                }
            }

            if (candidates.isEmpty()) {
                log.info("Google Shopping returned zero candidates for query '{}'.", query);
                return PricingFetchResult.failure(
                        PricingFetchStatus.NO_RESULTS,
                        "SHOPPING_RESULTS_EMPTY",
                        "Google Shopping returned no product candidates for this item.",
                        primaryFetch.httpStatus()
                );
            }

            List<PriceCandidate> relevantCandidates = new ArrayList<>();
            int usablePriceCandidates = 0;

            for (ShoppingCandidate candidate : candidates) {
                if (!Double.isFinite(candidate.price()) || candidate.price() <= 0) {
                    continue;
                }

                usablePriceCandidates++;

                CandidateMatchResult match = priceCandidateMatcher.evaluate(
                        itemName,
                        customCategory,
                        candidate,
                        primarySpecifications,
                        secondarySpecifications,
                        tertiarySpecifications
                );

                if (!match.accepted()) {
                    log.info(
                            "Rejected pricing candidate for '{}': score={}, reason={}, title={}",
                            itemName,
                            match.score(),
                            match.reason(),
                            candidate.title()
                    );
                    continue;
                }

                double weight = relevanceWeight(match.score());

                relevantCandidates.add(
                        new PriceCandidate(
                                candidate.price(),
                                match.score(),
                                weight,
                                candidate.title()
                        )
                );

                log.info(
                        "Accepted pricing candidate for '{}': score={}, weight={}, price={}, title={}",
                        itemName,
                        match.score(),
                        weight,
                        candidate.price(),
                        candidate.title()
                );
            }

            if (usablePriceCandidates == 0) {
                log.warn(
                        "SerpAPI returned shopping results but no usable prices for query '{}'.",
                        query
                );

                return PricingFetchResult.failure(
                        PricingFetchStatus.NO_RESULTS,
                        "NO_USABLE_PRICES",
                        "No usable retail prices were found for this item.",
                        primaryFetch.httpStatus()
                );
            }

            if (relevantCandidates.isEmpty()) {
                log.warn(
                        "Shopping candidates were retrieved but none were sufficiently relevant for query '{}'.",
                        query
                );

                return PricingFetchResult.failure(
                        PricingFetchStatus.NO_RELEVANT_CANDIDATES,
                        "NO_RELEVANT_CANDIDATES",
                        "Retail products were found, but none were sufficiently similar to this listing.",
                        primaryFetch.httpStatus()
                );
            }

            int median = weightedPriceAggregator.weightedMedian(relevantCandidates);

            if (median <= 0) {
                return PricingFetchResult.failure(
                        PricingFetchStatus.NO_RESULTS,
                        "INVALID_MEDIAN",
                        "The returned market prices could not be used safely.",
                        primaryFetch.httpStatus()
                );
            }

            log.info(
                    "Pricing estimate ready for '{}': query='{}', relevantCandidates={}, median={}",
                    itemName,
                    query,
                    relevantCandidates.size(),
                    median
            );

            return PricingFetchResult.success(median);

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

    private ShoppingFetchResult fetchShoppingCandidates(String query) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString("https://serpapi.com/search")
                    .queryParam("engine", "google_shopping")
                    .queryParam("q", query)
                    .queryParam("google_domain", googleDomain)
                    .queryParam("gl", googleCountry)
                    .queryParam("hl", googleLanguage)
                    .queryParam("api_key", serpApiKey);

            if (searchLocation != null && !searchLocation.isBlank()) {
                builder.queryParam("location", searchLocation.trim());
            }

            String url = builder
                    .build()
                    .encode()
                    .toUriString();

            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(url, JsonNode.class);

            JsonNode body = response.getBody();

            if (body == null || body.isNull()) {
                log.warn("SerpAPI returned an empty body for query '{}'.", query);
                return ShoppingFetchResult.failure(
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

                return ShoppingFetchResult.failure(
                        PricingFetchStatus.PROVIDER_ERROR,
                        "SERP_API_ERROR",
                        "The market pricing provider returned an error.",
                        response.getStatusCode().value()
                );
            }

            JsonNode shoppingResults = body.path("shopping_results");
            String searchId = body.path("search_metadata").path("id").asText("");

            if (!shoppingResults.isArray()) {
                log.warn(
                        "SerpAPI response contains no shopping_results for query '{}', searchId={}",
                        query,
                        searchId
                );

                return ShoppingFetchResult.failure(
                        PricingFetchStatus.NO_RESULTS,
                        "SHOPPING_RESULTS_MISSING",
                        "No retail market results were returned for this item.",
                        response.getStatusCode().value()
                );
            }

            List<ShoppingCandidate> candidates = new ArrayList<>();
            for (JsonNode result : shoppingResults) {
                candidates.add(ShoppingCandidate.from(result));
            }

            return ShoppingFetchResult.success(
                    candidates,
                    searchId,
                    response.getStatusCode().value()
            );

        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            log.error(
                    "SerpAPI HTTP failure. status={}, query={}, response={}",
                    status,
                    query,
                    e.getResponseBodyAsString(),
                    e
            );

            if (status == 401 || status == 403) {
                return ShoppingFetchResult.failure(
                        PricingFetchStatus.AUTHENTICATION_FAILED,
                        "SERP_API_AUTHENTICATION",
                        "The market pricing provider rejected the API credentials.",
                        status
                );
            }

            if (status == 429) {
                return ShoppingFetchResult.failure(
                        PricingFetchStatus.RATE_LIMITED,
                        "SERP_API_RATE_LIMITED",
                        "The market pricing provider rate limit was reached.",
                        status
                );
            }

            if (status >= 400 && status < 500) {
                return ShoppingFetchResult.failure(
                        PricingFetchStatus.BAD_REQUEST,
                        "SERP_API_BAD_REQUEST",
                        "The market pricing request was rejected by the provider.",
                        status
                );
            }

            return ShoppingFetchResult.failure(
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

            return ShoppingFetchResult.failure(
                    PricingFetchStatus.NETWORK_ERROR,
                    "SERP_API_NETWORK_ERROR",
                    "The market pricing provider could not be reached.",
                    null
            );
        }
    }

    private List<ShoppingCandidate> merge(
            List<ShoppingCandidate> first,
            List<ShoppingCandidate> second
    ) {
        List<ShoppingCandidate> merged = new ArrayList<>(first);
        merged.addAll(second);
        return merged;
    }

    private List<ShoppingCandidate> deduplicate(List<ShoppingCandidate> candidates) {
        Map<String, ShoppingCandidate> unique = new LinkedHashMap<>();

        for (ShoppingCandidate candidate : candidates) {
            if (candidate == null) continue;

            String productId = candidate.productId();
            String key;

            if (productId != null && !productId.isBlank()) {
                key = "id:" + productId;
            } else {
                key = "title:" + normalize(candidate.title())
                        + "|price:" + Math.round(candidate.price() * 100.0) / 100.0;
            }

            unique.putIfAbsent(key, candidate);
        }

        return new ArrayList<>(unique.values());
    }

    private int countProductFamilyMatches(
            String itemName,
            List<ShoppingCandidate> candidates
    ) {
        List<String> identityTokens = buildFamilyTokens(itemName);
        if (identityTokens.isEmpty()) return 0;

        int count = 0;

        for (ShoppingCandidate candidate : candidates) {
            String candidateText = normalize(candidate.searchableText());

            int matched = 0;
            for (String token : identityTokens) {
                if (candidateText.contains(token)) {
                    matched++;
                }
            }

            // Numeric/model-bearing identity token is the most useful anchor.
            boolean modelMatched = identityTokens.stream()
                    .filter(token -> token.matches(".*\\d.*"))
                    .anyMatch(candidateText::contains);

            if (modelMatched && matched >= Math.min(2, identityTokens.size())) {
                count++;
            }
        }

        return count;
    }

    private List<String> buildFamilyTokens(String itemName) {
        String normalized = normalize(itemName);
        if (normalized.isBlank()) return List.of();

        List<String> tokens = new ArrayList<>();
        for (String token : normalized.split(" ")) {
            if (token.length() < 2 || GENERIC_QUERY_WORDS.contains(token)) {
                continue;
            }
            tokens.add(token);
        }

        return tokens.stream().distinct().toList();
    }

    private String buildShoppingQuery(String itemName) {
        return normalize(itemName);
    }

    private String buildFallbackShoppingQuery(String itemName) {
        List<String> meaningful = buildFamilyTokens(itemName);
        if (meaningful.isEmpty()) return "";

        return String.join(" ", meaningful);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return "";

        String normalized = value.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replace('–', '-').replace('—', '-');
        normalized = normalized.replaceAll("[-_/,]+", " ");
        normalized = normalized.replaceAll("[^a-z0-9.]+", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }

    private double relevanceWeight(double score) {
        double clampedScore = Math.max(
                PriceCandidateMatcher.ACCEPTANCE_THRESHOLD,
                Math.min(1.0, score)
        );

        double normalized =
                (clampedScore - PriceCandidateMatcher.ACCEPTANCE_THRESHOLD)
                        / (1.0 - PriceCandidateMatcher.ACCEPTANCE_THRESHOLD);

        // Relevance matters, but no individual merchant listing can dominate.
        return 1.0 + (normalized * 4.0);
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

        return (int) Math.round(newPrice * multiplier);
    }

    private record ShoppingFetchResult(
            boolean success,
            List<ShoppingCandidate> candidates,
            String searchId,
            Integer httpStatus,
            PricingFetchResult failureResult
    ) {
        static ShoppingFetchResult success(
                List<ShoppingCandidate> candidates,
                String searchId,
                int httpStatus
        ) {
            return new ShoppingFetchResult(
                    true,
                    candidates,
                    searchId,
                    httpStatus,
                    null
            );
        }

        static ShoppingFetchResult failure(
                PricingFetchStatus status,
                String errorCode,
                String message,
                Integer httpStatus
        ) {
            return new ShoppingFetchResult(
                    false,
                    List.of(),
                    "",
                    httpStatus,
                    PricingFetchResult.failure(status, errorCode, message, httpStatus)
            );
        }
    }
}
