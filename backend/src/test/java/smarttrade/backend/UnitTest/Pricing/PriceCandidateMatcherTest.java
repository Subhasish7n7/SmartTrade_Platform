package smarttrade.backend.UnitTest.Pricing;

import org.junit.jupiter.api.Test;
import smarttrade.backend.service.pricing.CandidateMatchResult;
import smarttrade.backend.service.pricing.PriceCandidateMatcher;
import smarttrade.backend.service.pricing.ShoppingCandidate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PriceCandidateMatcherTest {

    private final PriceCandidateMatcher matcher = new PriceCandidateMatcher();

    @Test
    void strongProductAndPrimarySpecificationsShouldBeAcceptedWithHighScore() {
        CandidateMatchResult result = matcher.evaluate(
                "ASUS ROG Zephyrus G14 Gaming Laptop",
                null,
                candidate(
                        "ASUS ROG Zephyrus G14 RTX 4060 Ryzen 9 Laptop",
                        "16GB RAM 1TB SSD 165Hz",
                        129999
                ),
                List.of("RTX 4060", "Ryzen 9", "16GB RAM"),
                List.of("1TB SSD", "165Hz"),
                List.of("WiFi 6")
        );

        assertTrue(result.accepted());
        assertTrue(result.score() >= 0.90);
    }


    @Test
    void primarySpecificationConflictShouldLowerScoreWithoutAutomaticallyRejectingCandidate() {
        List<String> primarySpecifications =
                List.of("RTX 4060", "Ryzen 9", "16GB RAM");

        List<String> secondarySpecifications =
                List.of("1TB SSD");

        List<String> tertiarySpecifications =
                List.of("WiFi 6");

        CandidateMatchResult matchingCandidate = matcher.evaluate(
                "ASUS ROG Zephyrus G14 Gaming Laptop",
                null,
                candidate(
                        "ASUS ROG Zephyrus G14 RTX 4060 Ryzen 9 Laptop",
                        "16GB RAM 1TB SSD",
                        129999
                ),
                primarySpecifications,
                secondarySpecifications,
                tertiarySpecifications
        );

        CandidateMatchResult conflictingCandidate = matcher.evaluate(
                "ASUS ROG Zephyrus G14 Gaming Laptop",
                null,
                candidate(
                        "ASUS ROG Zephyrus G14 RTX 3050 Ryzen 7 Laptop",
                        "8GB RAM 512GB SSD",
                        99999
                ),
                primarySpecifications,
                secondarySpecifications,
                tertiarySpecifications
        );

        assertTrue(matchingCandidate.accepted());
        assertTrue(conflictingCandidate.accepted());

        assertTrue(
                conflictingCandidate.score() < matchingCandidate.score(),
                "A candidate with conflicting specifications should receive a lower score"
        );
    }


    @Test
    void obviousAccessoryShouldBeRejected() {
        CandidateMatchResult result = matcher.evaluate(
                "ASUS ROG Zephyrus G14 Gaming Laptop",
                null,
                candidate(
                        "Laptop Case for ASUS ROG Zephyrus G14",
                        "Protective sleeve",
                        2499
                ),
                List.of("RTX 4060"),
                List.of(),
                List.of()
        );

        assertFalse(result.accepted());
        assertEquals("likely_accessory_or_replacement", result.reason());
    }

    @Test
    void customProductTypeCanRejectDiningTableForStudyTable() {
        CandidateMatchResult result = matcher.evaluate(
                "Wooden Study Table",
                "study table",
                candidate(
                        "Oak Dining Table",
                        "solid oak table",
                        14000
                ),
                List.of("oak"),
                List.of("4 drawers", "120cm"),
                List.of()
        );

        assertFalse(result.accepted());
        assertEquals("product_type_modifier_conflict", result.reason());
    }

    @Test
    void missingLowPrioritySpecificationShouldNotAutomaticallyRejectCandidate() {
        CandidateMatchResult result = matcher.evaluate(
                "Canon EOS R50",
                null,
                candidate(
                        "Canon EOS R50 Mirrorless Camera",
                        "24.2MP sensor",
                        74999
                ),
                List.of(),
                List.of("mirrorless"),
                List.of("WiFi 6")
        );

        assertTrue(result.accepted());
    }

    private ShoppingCandidate candidate(String title, String snippet, double price) {
        return new ShoppingCandidate(
                null,
                title,
                snippet,
                List.of(),
                "Store",
                "",
                price
        );
    }
}
