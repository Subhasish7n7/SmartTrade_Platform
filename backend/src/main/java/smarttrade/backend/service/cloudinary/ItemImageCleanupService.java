package smarttrade.backend.service.cloudinary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.repository.item.ItemImageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemImageCleanupService {

    private final ItemImageRepository itemImageRepository;
    private final CloudinaryAssetService cloudinaryAssetService;

    @Async("cloudinaryTaskExecutor")
    public void cleanupAsync(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;
        imageIds.forEach(this::cleanupOne);
    }

    @org.springframework.scheduling.annotation.Scheduled(
            fixedDelayString = "${cloudinary.cleanup.fixed-delay-ms:3600000}"
    )
    public void cleanupPendingBatch() {
        List<ItemImageEntity> pending = itemImageRepository
                .findTop50ByStatusOrderByMarkedForDeletionAtAsc(
                        ItemImageStatus.PENDING_DELETION
                );
        pending.forEach(image -> cleanupOne(image.getId()));
    }

    private void cleanupOne(Long imageId) {
        ItemImageEntity image = itemImageRepository.findById(imageId).orElse(null);
        if (image == null || image.getStatus() != ItemImageStatus.PENDING_DELETION) return;

        String assetId = image.getAssetId();
        if (assetId == null || assetId.isBlank()) {
            log.error("Pending image {} has no Cloudinary asset ID; leaving DB row for repair", imageId);
            return;
        }


        try {
            cloudinaryAssetService.deleteByAssetId(assetId);
            itemImageRepository.deleteById(imageId);
            log.info("Deleted Cloudinary asset {} and DB image {}", assetId, imageId);
        } catch (Exception e) {
            log.warn(
                    "Cloudinary deletion failed for image {} asset {}; leaving row pending",
                    imageId, assetId, e
            );
        }
    }
}