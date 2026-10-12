package smarttrade.backend.service.pricing;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class WeightedPriceAggregator {

    public int weightedMedian(List<PriceCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("At least one price candidate is required");
        }

        List<PriceCandidate> sorted = candidates.stream()
                .filter(candidate -> candidate != null
                        && Double.isFinite(candidate.price())
                        && candidate.price() > 0
                        && Double.isFinite(candidate.weight())
                        && candidate.weight() > 0)
                .sorted(Comparator.comparingDouble(PriceCandidate::price))
                .toList();

        if (sorted.isEmpty()) {
            throw new IllegalArgumentException("No valid price candidates were supplied");
        }

        double totalWeight = sorted.stream()
                .mapToDouble(PriceCandidate::weight)
                .sum();

        double cumulativeWeight = 0.0;
        double halfWeight = totalWeight / 2.0;

        for (PriceCandidate candidate : sorted) {
            cumulativeWeight += candidate.weight();
            if (cumulativeWeight >= halfWeight) {
                return safeRound(candidate.price());
            }
        }

        return safeRound(sorted.get(sorted.size() - 1).price());
    }

    private int safeRound(double value) {
        if (!Double.isFinite(value)
                || value <= 0
                || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Price is outside the supported integer range");
        }
        return (int) Math.round(value);
    }
}
