package smarttrade.backend.dto.admin;

import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserRole;

import java.time.LocalDateTime;

public record AdminUserSummaryResponse(
        Long userId,
        String name,
        String email,
        String phoneNo,
        UserRole role,
        AccountStatus accountStatus,
        double trustScore,
        int totalListings,
        int successfulTrades,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt
) {}