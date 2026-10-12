package smarttrade.backend.service.pricing;

public record CandidateMatchResult(
        boolean accepted,
        double score,
        String reason
) {
    public static CandidateMatchResult accepted(double score) {
        return new CandidateMatchResult(true, score, "accepted");
    }

    public static CandidateMatchResult rejected(String reason) {
        return new CandidateMatchResult(false, 0.0, reason);
    }
}
