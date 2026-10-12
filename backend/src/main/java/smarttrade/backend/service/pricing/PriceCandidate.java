package smarttrade.backend.service.pricing;

public record PriceCandidate(
        double price,
        double relevanceScore,
        double weight,
        String title
) {
}
