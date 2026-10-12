package smarttrade.backend.UnitTest.Pricing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.service.pricing.PriceCandidateMatcher;
import smarttrade.backend.service.pricing.PriceEstimationService;
import smarttrade.backend.service.pricing.PricingFetchResult;
import smarttrade.backend.service.pricing.PricingFetchStatus;
import smarttrade.backend.service.pricing.WeightedPriceAggregator;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceEstimationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private PriceEstimationService priceEstimationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Use real matching and aggregation logic; only the external HTTP call is mocked.
        priceEstimationService = new PriceEstimationService(
                restTemplate,
                new PriceCandidateMatcher(),
                new WeightedPriceAggregator()
        );

        ReflectionTestUtils.setField(priceEstimationService, "serpApiKey", "test-key");
        ReflectionTestUtils.setField(priceEstimationService, "apiEnabled", true);
        ReflectionTestUtils.setField(priceEstimationService, "googleDomain", "google.co.in");
        ReflectionTestUtils.setField(priceEstimationService, "googleCountry", "in");
        ReflectionTestUtils.setField(priceEstimationService, "googleLanguage", "en");
    }

    @Test
    void shouldRejectMissingItemName() {
        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                " ",
                ItemCategory.PHONES,
                null,
                List.of("android")
        );

        assertEquals(PricingFetchStatus.INVALID_CONFIGURATION, result.status());
        assertEquals("ITEM_NAME_MISSING", result.errorCode());
        assertEquals("Item name is required for market pricing.", result.errorMessage());

        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldRejectWhenApiIsDisabled() {
        ReflectionTestUtils.setField(priceEstimationService, "apiEnabled", false);

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "iPhone 15",
                ItemCategory.PHONES,
                null,
                List.of("apple")
        );

        assertEquals(PricingFetchStatus.INVALID_CONFIGURATION, result.status());
        assertEquals("SERP_API_DISABLED", result.errorCode());

        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldRejectWhenApiKeyIsMissing() {
        ReflectionTestUtils.setField(priceEstimationService, "serpApiKey", " ");

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "iPhone 15",
                ItemCategory.PHONES,
                null,
                List.of("apple")
        );

        assertEquals(PricingFetchStatus.INVALID_CONFIGURATION, result.status());
        assertEquals("SERP_API_KEY_MISSING", result.errorCode());

        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldReturnWeightedMedianOfUsableNewProductPrices() throws Exception {
        JsonNode body = objectMapper.readTree("""
                {
                  "shopping_results": [
                    {"title": "Apple iPhone 15", "extracted_price": 1000},
                    {"title": "Apple iPhone 15", "extracted_price": 5000},
                    {"title": "Apple iPhone 15", "extracted_price": 3000},
                    {"title": "Apple iPhone 15", "extracted_price": 4000, "second_hand_condition": "refurbished"},
                    {"title": "Apple iPhone 15", "extracted_price": 0},
                    {"title": "Apple iPhone 15", "extracted_price": -10}
                  ]
                }
                """);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                " iPhone 15 ",
                ItemCategory.PHONES,
                null,
                List.of(" Apple ", "5G")
        );

        assertEquals(PricingFetchStatus.SUCCESS, result.status());
        assertEquals(3000, result.price());
        assertEquals(200, result.httpStatus());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(JsonNode.class));

        String url = urlCaptor.getValue();
        // Queries are normalized to lowercase before being sent to SerpApi.
        assertTrue(url.contains("q=iphone%2015"));
        assertTrue(url.contains("google_domain=google.co.in"));
        assertTrue(url.contains("gl=in"));
        assertTrue(url.contains("hl=en"));
        assertTrue(url.contains("api_key=test-key"));
    }

    @Test
    void shouldReturnLowerWeightedMedianForEvenNumberOfEquallyWeightedCandidates() throws Exception {
        JsonNode body = objectMapper.readTree("""
                {
                  "shopping_results": [
                    {"title": "Dell Inspiron 15 Laptop", "extracted_price": 1000},
                    {"title": "Dell Inspiron 15 Laptop", "extracted_price": 2000},
                    {"title": "Dell Inspiron 15 Laptop", "extracted_price": 3000},
                    {"title": "Dell Inspiron 15 Laptop", "extracted_price": 4000}
                  ]
                }
                """);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Dell Inspiron 15 Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.SUCCESS, result.status());
        // WeightedPriceAggregator returns the first candidate whose cumulative
        // weight reaches half the total. With four equal weights, this is 2000.
        assertEquals(2000, result.price());
    }

    @Test
    void shouldUseItemNameForShoppingQueryAndKeepCategoryAsMatchingContext() throws Exception {
        JsonNode body = objectMapper.readTree("""
                {
                  "shopping_results": [
                    {"title": "MacBook Pro Laptop", "extracted_price": 1000}
                  ]
                }
                """);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "MacBook Pro",
                ItemCategory.COMPUTERS,
                "professional laptop",
                List.of()
        );

        assertEquals(PricingFetchStatus.SUCCESS, result.status());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(JsonNode.class));

        String url = urlCaptor.getValue();
        assertTrue(url.contains("q=macbook%20pro"));
        // The current service intentionally does not send category context to Google.
        assertFalse(url.contains("professional%20laptop"));
    }

    @Test
    void shouldReturnProviderErrorForEmptyBody() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.PROVIDER_ERROR, result.status());
        assertEquals("EMPTY_PROVIDER_RESPONSE", result.errorCode());
        assertEquals(200, result.httpStatus());
    }

    @Test
    void shouldReturnProviderErrorForApiErrorPayload() throws Exception {
        JsonNode body = objectMapper.readTree("""
                {
                  "error": "Invalid API key"
                }
                """);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.PROVIDER_ERROR, result.status());
        assertEquals("SERP_API_ERROR", result.errorCode());
        assertEquals(200, result.httpStatus());
    }

    @Test
    void shouldReturnNoResultsWhenShoppingResultsMissing() throws Exception {
        JsonNode body = objectMapper.readTree("""
                {
                  "search_metadata": {
                    "status": "Success"
                  }
                }
                """);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.NO_RESULTS, result.status());
        assertEquals("SHOPPING_RESULTS_MISSING", result.errorCode());
    }

    @Test
    void shouldReturnNoResultsWhenNoUsablePricesExist() throws Exception {
        JsonNode body = objectMapper.readTree("""
                {
                  "shopping_results": [
                    {"extracted_price": 0},
                    {"extracted_price": -100},
                    {"extracted_price": "not-a-number"}
                  ]
                }
                """);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.NO_RESULTS, result.status());
        assertEquals("NO_USABLE_PRICES", result.errorCode());
    }

    @Test
    void shouldReturnNoRelevantCandidatesWhenOnlyPositivePricedCandidateIsUsed() throws Exception {
        JsonNode body = objectMapper.readTree("""
                {
                  "shopping_results": [
                    {
                      "title": "Dell Inspiron 15 Laptop",
                      "second_hand_condition": "used",
                      "extracted_price": 1000
                    }
                  ]
                }
                """);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Dell Inspiron 15 Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        // There is a positive price, but the only candidate is rejected as used.
        assertEquals(PricingFetchStatus.NO_RELEVANT_CANDIDATES, result.status());
        assertEquals("NO_RELEVANT_CANDIDATES", result.errorCode());
    }

    @Test
    void shouldReturnAuthenticationFailureFor401() {
        HttpStatusCodeException exception = new HttpStatusCodeException(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                null,
                "bad-key".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        ) {
        };

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(exception);

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.AUTHENTICATION_FAILED, result.status());
        assertEquals("SERP_API_AUTHENTICATION", result.errorCode());
        assertEquals(401, result.httpStatus());
    }

    @Test
    void shouldReturnRateLimitedFor429() {
        HttpStatusCodeException exception = new HttpStatusCodeException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limited"
        ) {
        };

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(exception);

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.RATE_LIMITED, result.status());
        assertEquals("SERP_API_RATE_LIMITED", result.errorCode());
        assertEquals(429, result.httpStatus());
    }

    @Test
    void shouldReturnBadRequestForOther4xx() {
        HttpStatusCodeException exception = new HttpStatusCodeException(
                HttpStatus.BAD_REQUEST,
                "Bad request"
        ) {
        };

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(exception);

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.BAD_REQUEST, result.status());
        assertEquals("SERP_API_BAD_REQUEST", result.errorCode());
        assertEquals(400, result.httpStatus());
    }

    @Test
    void shouldReturnProviderErrorFor5xx() {
        HttpStatusCodeException exception = new HttpStatusCodeException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Provider error"
        ) {
        };

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(exception);

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.PROVIDER_ERROR, result.status());
        assertEquals("SERP_API_HTTP_500", result.errorCode());
        assertEquals(500, result.httpStatus());
    }

    @Test
    void shouldReturnNetworkErrorForResourceAccessException() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.NETWORK_ERROR, result.status());
        assertEquals("SERP_API_NETWORK_ERROR", result.errorCode());
    }

    @Test
    void shouldReturnUnexpectedProviderErrorForUnexpectedException() {
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("unexpected"));

        PricingFetchResult result = priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                null
        );

        assertEquals(PricingFetchStatus.PROVIDER_ERROR, result.status());
        assertEquals("SERP_API_UNEXPECTED_ERROR", result.errorCode());
    }

    @Test
    void shouldGenerateUsedPriceForEveryCondition() {
        assertEquals(950, priceEstimationService.generateUsedPrice(1000, ItemCondition.NEW));
        assertEquals(850, priceEstimationService.generateUsedPrice(1000, ItemCondition.LIKE_NEW));
        assertEquals(700, priceEstimationService.generateUsedPrice(1000, ItemCondition.GOOD));
        assertEquals(500, priceEstimationService.generateUsedPrice(1000, ItemCondition.FAIR));
        assertEquals(350, priceEstimationService.generateUsedPrice(1000, ItemCondition.POOR));
    }

    @Test
    void shouldReturnNullWhenUsedPriceInputsAreMissing() {
        assertNull(priceEstimationService.generateUsedPrice(null, ItemCondition.GOOD));
        assertNull(priceEstimationService.generateUsedPrice(1000, null));
        assertNull(priceEstimationService.generateUsedPrice(null, null));
    }
}
