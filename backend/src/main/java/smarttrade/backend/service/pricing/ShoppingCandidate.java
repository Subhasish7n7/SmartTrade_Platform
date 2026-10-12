package smarttrade.backend.service.pricing;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public record ShoppingCandidate(
        String productId,
        String title,
        String snippet,
        List<String> extensions,
        String source,
        String secondHandCondition,
        double price
) {

    public static ShoppingCandidate from(JsonNode result) {
        List<String> extensions = new ArrayList<>();
        JsonNode extensionNode = result.path("extensions");

        if (extensionNode.isArray()) {
            for (JsonNode extension : extensionNode) {
                if (extension != null && !extension.isNull()) {
                    String value = extension.asText("").trim();
                    if (!value.isBlank()) {
                        extensions.add(value);
                    }
                }
            }
        }

        return new ShoppingCandidate(
                textOrNull(result.path("product_id")),
                result.path("title").asText(""),
                result.path("snippet").asText(""),
                List.copyOf(extensions),
                result.path("source").asText(""),
                result.path("second_hand_condition").asText(""),
                result.path("extracted_price").asDouble(Double.NaN)
        );
    }

    public String searchableText() {
        return String.join(" ",
                title == null ? "" : title,
                snippet == null ? "" : snippet,
                String.join(" ", extensions == null ? List.of() : extensions)
        );
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String value = node.asText("").trim();
        return value.isBlank() ? null : value;
    }
}
