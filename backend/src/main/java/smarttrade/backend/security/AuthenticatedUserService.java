package smarttrade.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.user.UserRepo;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserService {

    private final UserRepo userRepo;

    public UserEntity getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated user");
        }

        String email = authentication.getName();

        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new ForbiddenOperationException("Account is suspended");
        }

        return user;
    }
}