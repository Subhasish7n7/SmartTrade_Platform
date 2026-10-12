package smarttrade.backend.service.pricing;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PriceCandidateMatcher {

    /**
     * The matcher filters a broad Shopping candidate pool. It does not try to
     * prove exact SKU identity, so the threshold stays deliberately moderate.
     */
    public static final double ACCEPTANCE_THRESHOLD = 0.35;

    private static final double IDENTITY_WEIGHT = 0.55;
    private static final double PRIMARY_WEIGHT = 0.30;
    private static final double SECONDARY_WEIGHT = 0.10;
    private static final double TERTIARY_WEIGHT = 0.05;

    private static final Set<String> GENERIC_IDENTITY_WORDS = Set.of(
            "the", "a", "an", "with", "for", "and", "of", "new", "latest",
            "sale", "buy", "best", "online", "official", "free", "shipping",
            "gaming", "laptop", "notebook", "computer", "pc", "desktop",
            "device", "product", "portable", "wireless", "smart", "original",
            "edition", "model"
    );

    private static final Set<String> CONDITION_MARKERS = Set.of(
            "used", "refurbished", "renewed", "pre owned", "preowned", "second hand",
            "open box", "open-box"
    );

    private static final Set<String> ACCESSORY_MARKERS = Set.of(
            "case", "cover", "sleeve", "skin", "protector", "adapter", "charger",
            "replacement", "housing", "shell", "stand", "mount", "cable"
    );

    /**
     * Safe model-anchor pattern. Technical identifiers such as RTX4060 are
     * handled as specifications, not product-model conflicts.
     */
    private static final Pattern MODEL_ANCHOR_PATTERN = Pattern.compile(
            "\\b([a-z]{1,4})(\\d{1,4})([a-z0-9-]*)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public CandidateMatchResult evaluate(
            String itemName,
            String customCategory,
            ShoppingCandidate candidate,
            List<String> primarySpecifications,
            List<String> secondarySpecifications,
            List<String> tertiarySpecifications
    ) {
        if (candidate == null) {
            return CandidateMatchResult.rejected("null_candidate");
        }

        if (!isFinitePositive(candidate.price())) {
            return CandidateMatchResult.rejected("invalid_price");
        }

        String candidateText = normalize(candidate.searchableText());
        String candidateTitle = normalize(candidate.title());

        if (containsSecondHandMarker(candidate.secondHandCondition())
                || containsConditionMarker(candidateText)) {
            return CandidateMatchResult.rejected("second_hand_or_refurbished");
        }

        if (isLikelyAccessory(candidateTitle, itemName)) {
            return CandidateMatchResult.rejected("likely_accessory_or_replacement");
        }

        if (hasProductTypeModifierConflict(customCategory, candidateTitle)) {
            return CandidateMatchResult.rejected("product_type_modifier_conflict");
        }

        String listingIdentity = buildListingIdentity(itemName, customCategory);
        double identityScore = scoreIdentity(listingIdentity, candidateText);

        /*
         * Primary specifications are deliberately NOT hard filters.
         * RTX 4060 vs RTX 5060, Ryzen 9 vs Ryzen 7, 16GB vs 32GB, etc.
         * should change the relevance/price weight rather than automatically
         * deleting a potentially useful market comparable.
         *
         * The only model-level hard rejection below is conservative: it needs
         * the same brand and an explicit conflicting model anchor such as
         * ASUS G14 vs ASUS G16. This avoids the previous false positives like
         * ASUS G14 vs HP G9 or Logitech G402.
         */
        if (hasSameBrandDifferentModelFamily(itemName, candidate.title())) {
            return CandidateMatchResult.rejected("model_identifier_conflict");
        }

        boolean hasAnySpecification = hasValues(primarySpecifications)
                || hasValues(secondarySpecifications)
                || hasValues(tertiarySpecifications);

        double score = calculateScore(
                identityScore,
                scoreTier(primarySpecifications, candidateText),
                scoreTier(secondarySpecifications, candidateText),
                scoreTier(tertiarySpecifications, candidateText),
                hasAnySpecification,
                primarySpecifications,
                secondarySpecifications,
                tertiarySpecifications
        );

        /*
         * Keep strong identity matches even when Shopping did not expose all
         * specifications in its title/snippet. Specifications influence the
         * candidate weight; they are not used as destructive hard filters.
         */
        if (score < ACCEPTANCE_THRESHOLD && identityScore < 0.50) {
            return CandidateMatchResult.rejected("below_relevance_threshold");
        }

        return CandidateMatchResult.accepted(score);
    }

    private double calculateScore(
            double identityScore,
            double primaryScore,
            double secondaryScore,
            double tertiaryScore,
            boolean hasAnySpecification,
            List<String> primary,
            List<String> secondary,
            List<String> tertiary
    ) {
        if (!hasAnySpecification) {
            return identityScore;
        }

        double totalWeight = IDENTITY_WEIGHT;
        if (hasValues(primary)) totalWeight += PRIMARY_WEIGHT;
        if (hasValues(secondary)) totalWeight += SECONDARY_WEIGHT;
        if (hasValues(tertiary)) totalWeight += TERTIARY_WEIGHT;

        double weighted = identityScore * IDENTITY_WEIGHT;
        if (hasValues(primary)) weighted += primaryScore * PRIMARY_WEIGHT;
        if (hasValues(secondary)) weighted += secondaryScore * SECONDARY_WEIGHT;
        if (hasValues(tertiary)) weighted += tertiaryScore * TERTIARY_WEIGHT;

        return totalWeight == 0.0 ? identityScore : weighted / totalWeight;
    }

    private double scoreTier(List<String> specifications, String candidateText) {
        if (!hasValues(specifications)) return 0.0;

        List<String> normalizedSpecifications = cleanValues(specifications);
        if (normalizedSpecifications.isEmpty()) return 0.0;

        return normalizedSpecifications.stream()
                .mapToDouble(specification -> scoreSpecification(specification, candidateText))
                .average()
                .orElse(0.0);
    }

    private double scoreSpecification(String specification, String candidateText) {
        String normalizedSpecification = normalize(specification);
        if (normalizedSpecification.isBlank()) return 0.0;

        if (candidateText.contains(normalizedSpecification)) {
            return 1.0;
        }

        List<String> specificationTokens = meaningfulTokens(normalizedSpecification);
        if (specificationTokens.isEmpty()) return 0.0;

        Set<String> candidateTokens = new HashSet<>(Arrays.asList(candidateText.split(" ")));

        double totalWeight = 0.0;
        double matchedWeight = 0.0;

        for (String token : specificationTokens) {
            double tokenWeight = tokenWeight(token);
            totalWeight += tokenWeight;
            if (candidateTokens.contains(token)) {
                matchedWeight += tokenWeight;
            }
        }

        if (totalWeight == 0.0) return 0.0;

        return Math.min(1.0, matchedWeight / totalWeight);
    }

    private double scoreIdentity(String listingIdentity, String candidateText) {
        List<String> tokens = meaningfulIdentityTokens(listingIdentity);
        if (tokens.isEmpty()) return 0.0;

        Set<String> candidateTokens = new HashSet<>(Arrays.asList(candidateText.split(" ")));

        double totalWeight = 0.0;
        double matchedWeight = 0.0;

        for (String token : tokens) {
            double weight = identityTokenWeight(token);
            totalWeight += weight;

            if (candidateTokens.contains(token)
                    || candidateText.contains(" " + token + " ")
                    || candidateText.startsWith(token + " ")
                    || candidateText.endsWith(" " + token)) {
                matchedWeight += weight;
            }
        }

        return totalWeight == 0.0 ? 0.0 : matchedWeight / totalWeight;
    }

    private boolean hasSameBrandDifferentModelFamily(String itemName, String candidateTitle) {
        if (itemName == null || itemName.isBlank()
                || candidateTitle == null || candidateTitle.isBlank()) {
            return false;
        }

        String normalizedItem = normalize(itemName);
        String normalizedCandidate = normalize(candidateTitle);

        String listingBrand = firstMeaningfulToken(normalizedItem);
        String candidateBrand = firstMeaningfulToken(normalizedCandidate);

        if (listingBrand.isBlank()
                || candidateBrand.isBlank()
                || !listingBrand.equals(candidateBrand)) {
            return false;
        }

        List<ModelAnchor> listingModels = extractModelAnchors(normalizedItem);
        List<ModelAnchor> candidateModels = extractModelAnchors(normalizedCandidate);

        if (listingModels.isEmpty() || candidateModels.isEmpty()) {
            return false;
        }

        for (ModelAnchor listingModel : listingModels) {
            for (ModelAnchor candidateModel : candidateModels) {
                if (listingModel.isTechnicalIdentifier() || candidateModel.isTechnicalIdentifier()) {
                    continue;
                }

                if (listingModel.prefix().equals(candidateModel.prefix())
                        && listingModel.number() != candidateModel.number()) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<ModelAnchor> extractModelAnchors(String text) {
        if (text == null || text.isBlank()) return List.of();

        Matcher matcher = MODEL_ANCHOR_PATTERN.matcher(text);
        List<ModelAnchor> models = new ArrayList<>();

        while (matcher.find()) {
            String prefix = matcher.group(1).toLowerCase(Locale.ROOT);
            int number;

            try {
                number = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException ignored) {
                continue;
            }

            String full = matcher.group().toLowerCase(Locale.ROOT);
            boolean technical = prefix.equals("rtx")
                    || prefix.equals("gtx")
                    || prefix.equals("ryzen")
                    || prefix.equals("core")
                    || prefix.equals("intel")
                    || prefix.equals("amd")
                    || prefix.equals("i");

            models.add(new ModelAnchor(prefix, number, full, technical));
        }

        return models;
    }

    private String firstMeaningfulToken(String text) {
        for (String token : meaningfulTokens(text)) {
            if (!GENERIC_IDENTITY_WORDS.contains(token)) {
                return token;
            }
        }
        return "";
    }

    private double identityTokenWeight(String token) {
        if (token.matches(".*\\d.*")) return 3.0;
        if (token.length() >= 6) return 1.5;
        return 1.0;
    }

    private double tokenWeight(String token) {
        if (token.matches(".*\\d.*")) return 2.5;
        if (token.length() >= 6) return 1.5;
        return 1.0;
    }

    private boolean hasProductTypeModifierConflict(String customCategory, String candidateTitle) {
        if (customCategory == null || customCategory.isBlank()
                || candidateTitle == null || candidateTitle.isBlank()) {
            return false;
        }

        List<String> categoryTokens = meaningfulIdentityTokens(customCategory);
        List<String> candidateTokens = meaningfulTokens(candidateTitle);

        if (categoryTokens.size() < 2 || candidateTokens.size() < 2) {
            return false;
        }

        String head = categoryTokens.get(categoryTokens.size() - 1);
        int categoryHeadIndex = categoryTokens.lastIndexOf(head);
        int candidateHeadIndex = candidateTokens.lastIndexOf(head);

        if (categoryHeadIndex <= 0 || candidateHeadIndex <= 0) {
            return false;
        }

        String expectedModifier = categoryTokens.get(categoryHeadIndex - 1);
        String candidateModifier = candidateTokens.get(candidateHeadIndex - 1);

        if (GENERIC_IDENTITY_WORDS.contains(expectedModifier)
                || GENERIC_IDENTITY_WORDS.contains(candidateModifier)) {
            return false;
        }

        return !expectedModifier.equals(candidateModifier);
    }

    private boolean isLikelyAccessory(String candidateTitle, String itemName) {
        if (candidateTitle == null || candidateTitle.isBlank()) return false;

        String listingTitle = normalize(itemName);
        boolean listingItselfIsAccessory = ACCESSORY_MARKERS.stream()
                .anyMatch(listingTitle::contains);

        if (listingItselfIsAccessory) return false;

        for (String marker : ACCESSORY_MARKERS) {
            if (candidateTitle.startsWith(marker + " ")
                    || candidateTitle.contains(marker + " for ")
                    || candidateTitle.contains(marker + " compatible with ")
                    || candidateTitle.startsWith("replacement " + marker)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsSecondHandMarker(String condition) {
        return containsConditionMarker(normalize(condition));
    }

    private boolean containsConditionMarker(String normalizedText) {
        if (normalizedText == null || normalizedText.isBlank()) return false;

        for (String marker : CONDITION_MARKERS) {
            if (normalizedText.contains(marker)) return true;
        }

        return false;
    }

    private String buildListingIdentity(String itemName, String customCategory) {
        String name = itemName == null ? "" : itemName.trim();

        if (customCategory == null || customCategory.isBlank()) {
            return name;
        }

        // Category is matching context only; it is still not sent to Google.
        return name + " " + customCategory.trim();
    }

    private List<String> meaningfulIdentityTokens(String text) {
        List<String> tokens = meaningfulTokens(normalize(text));
        List<String> result = new ArrayList<>();

        for (String token : tokens) {
            if (!GENERIC_IDENTITY_WORDS.contains(token)) {
                result.add(token);
            }
        }

        return result;
    }

    private List<String> meaningfulTokens(String text) {
        if (text == null || text.isBlank()) return List.of();

        return Arrays.stream(text.split(" "))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .filter(token -> token.length() >= 2)
                .toList();
    }

    private boolean hasValues(List<String> values) {
        return values != null && values.stream()
                .anyMatch(value -> value != null && !value.isBlank());
    }

    private List<String> cleanValues(List<String> values) {
        if (!hasValues(values)) return List.of();

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return "";

        String normalized = value.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replace('–', '-').replace('—', '-');

        // Make common measurement spellings converge: 16 GB, 16GB, 16-gb.
        normalized = normalized.replaceAll(
                "(?i)(\\d+(?:\\.\\d+)?)\\s*(gb|tb|mhz|ghz|hz|inch|in)\\b",
                "$1$2"
        );

        normalized = normalized.replaceAll("[-_/,]+", " ");
        normalized = normalized.replaceAll("[^a-z0-9.]+", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }

    private boolean isFinitePositive(double value) {
        return Double.isFinite(value) && value > 0;
    }

    private record ModelAnchor(
            String prefix,
            int number,
            String full,
            boolean technicalIdentifier
    ) {
        boolean isTechnicalIdentifier() {
            return technicalIdentifier;
        }
    }
}
