package smarttrade.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceEstimationService {

    @Value("${serp.api.key}")
    private String serpApiKey;

    @Value("${price.api.enabled}")
    private boolean apiEnabled;

    private final RestTemplate restTemplate;

    public Integer fetchEstimatedNewPrice(String itemName, Integer userPrice) {

        try {
            if (!apiEnabled) {

                if (userPrice == null) {
                    return 1000;
                }

                double variation = 0.9 + Math.random() * 0.2;

                return (int)(userPrice * variation);
            }

            String url =
                    "https://serpapi.com/search.json" +
                            "?engine=google_shopping" +
                            "&q=" + itemName +
                            "&api_key=" + serpApiKey;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            Map body = response.getBody();

            if(body == null || !body.containsKey("shopping_results")) {
                return null;
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("shopping_results");

            List<Integer> prices = new ArrayList<>();

            for(Map<String, Object> result : results) {

                Object priceObj = result.get("extracted_price");

                if(priceObj instanceof Number number) {
                    prices.add(number.intValue());
                }
            }

            if(prices.isEmpty()) {
                return null;
            }

            return (int) prices.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0);

        } catch (Exception e) {

            log.error("Failed to fetch market price", e);

            return null;
        }
    }

    public Integer generateUsedPrice(Integer newPrice, String condition) {

        if(newPrice == null) {
            return null;
        }

        double multiplier = switch (condition.toLowerCase()) {
            case "new" -> 0.95;
            case "used - like new" -> 0.85;
            case "used - good" -> 0.70;
            case "used - fair" -> 0.50;
            default -> 0.60;
        };

        return (int)(newPrice * multiplier);
    }
}
