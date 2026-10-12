package smarttrade.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.dto.trust.TrustScoreHistoryResponse;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.trust.TrustScoreService;

@RestController
@RequestMapping("/trust")
@RequiredArgsConstructor
public class TrustController {

    private final TrustScoreService trustScoreService;
    private final AuthenticatedUserService authenticatedUserService;

    @GetMapping("/me/history")
    public ResponseEntity<Page<TrustScoreHistoryResponse>> getMyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);

        Long userId = authenticatedUserService.getCurrentUser().getUserId();

        return ResponseEntity.ok(trustScoreService.getHistory(
                        userId,
                        PageRequest.of(safePage, safeSize)
                )
        );
    }

    @GetMapping("/users/{userId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TrustScoreHistoryResponse>> getUserHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);

        return ResponseEntity.ok(trustScoreService.getHistory(
                        userId,
                        PageRequest.of(safePage, safeSize)
                )
        );
    }
}