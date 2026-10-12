package smarttrade.backend.service.cloudinary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.repository.item.ItemImageRepository;
import smarttrade.backend.repository.user.UserAvatarRepo;

@Service
@RequiredArgsConstructor
public class CloudinaryAssetReferenceService {

    private final ItemImageRepository itemImageRepository;
    private final UserAvatarRepo userAvatarRepo;

    public boolean existsByAssetId(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            return false;
        }

        return itemImageRepository.existsByAssetId(assetId)
                || userAvatarRepo.existsByAssetId(assetId);
    }
}