package smarttrade.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.item.ItemImageRequest;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.events.ItemImageCleanupRequestedEvent;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.ItemImageRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemImageService {

    private static final int MAX_IMAGES = 8;

    private final ItemImageRepository itemImageRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void applyUpdateImages(ItemEntity item, List<ItemImageRequest> requests, List<Long> removedImageIds) {
        if (requests == null) {
            if (removedImageIds != null && !removedImageIds.isEmpty()) {
                throw new IllegalArgumentException("removedImageIds requires images to be provided");
            }
            return;
        }

        validateImageCount(requests.size());

        List<Long> requestedExistingIds = requests.stream()
                .map(ItemImageRequest::getId)
                .filter(Objects::nonNull)
                .toList();

        Set<Long> requestedIdSet = new HashSet<>(requestedExistingIds);
        if (requestedIdSet.size() != requestedExistingIds.size()) {
            throw new IllegalArgumentException("Duplicate item image IDs are not allowed");
        }

        Set<Long> removalIds = removedImageIds == null
                ? Set.of()
                : new HashSet<>(removedImageIds);

        if (removalIds.size() != (removedImageIds == null ? 0 : removedImageIds.size())) {
            throw new IllegalArgumentException("Duplicate removed image IDs are not allowed");
        }

        Set<Long> overlap = new HashSet<>(requestedIdSet);
        overlap.retainAll(removalIds);
        if (!overlap.isEmpty()) {
            throw new IllegalArgumentException("An image cannot be both retained and removed");
        }

        List<ItemImageEntity> currentImages = itemImageRepository.findByItem_ItemId(item.getItemId());
        Set<Long> activeExistingIds = currentImages.stream()
                .filter(image -> image.getStatus() == ItemImageStatus.ACTIVE)
                .map(ItemImageEntity::getId)
                .collect(Collectors.toSet());

        Set<Long> allReferencedExistingIds = new HashSet<>(requestedIdSet);
        allReferencedExistingIds.addAll(removalIds);

        if (!activeExistingIds.equals(allReferencedExistingIds)) {
            throw new IllegalArgumentException(
                    "The image update must explicitly retain or remove every active existing image"
            );
        }

        Set<String> submittedNewAssetIds = new HashSet<>();

        for (int index = 0; index < requests.size(); index++) {
            ItemImageRequest request = requests.get(index);

            if (request.getId() != null) {
                ItemImageEntity existing = itemImageRepository
                        .findByIdAndItem_ItemIdAndStatus(
                                request.getId(),
                                item.getItemId(),
                                ItemImageStatus.ACTIVE
                        )
                        .orElseThrow(() -> new ForbiddenOperationException(
                                "Item image does not belong to this item"
                        ));

                existing.setSortOrder(index);
            } else {
                validateNewImageRequest(request);
                if (!submittedNewAssetIds.add(request.getAssetId())) {
                    throw new IllegalArgumentException("Duplicate Cloudinary asset IDs are not allowed");
                }

                item.getImages().add(
                        ItemImageEntity.builder()
                                .item(item)
                                .secureUrl(request.getSecureUrl())
                                .publicId(request.getPublicId())
                                .assetId(request.getAssetId())
                                .width(request.getWidth())
                                .height(request.getHeight())
                                .format(request.getFormat())
                                .bytes(request.getBytes())
                                .sortOrder(index)
                                .status(ItemImageStatus.ACTIVE)
                                .createdAt(LocalDateTime.now())
                                .build()
                );
            }
        }

        if (removalIds.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        List<ItemImageEntity> marked = new ArrayList<>();

        for (Long imageId : removalIds) {
            ItemImageEntity image = itemImageRepository
                    .findByIdAndItem_ItemIdAndStatus(
                            imageId,
                            item.getItemId(),
                            ItemImageStatus.ACTIVE
                    )
                    .orElseThrow(() -> new ForbiddenOperationException(
                            "Item image does not belong to this item"
                    ));

            image.setStatus(ItemImageStatus.PENDING_DELETION);
            image.setMarkedForDeletionAt(now);
            marked.add(image);
        }

        itemImageRepository.saveAll(marked);

        applicationEventPublisher.publishEvent(
                new ItemImageCleanupRequestedEvent(
                        marked.stream().map(ItemImageEntity::getId).toList()
                )
        );
    }

    @Transactional
    public List<Long> markAllImagesForDeletion(ItemEntity item) {
        List<ItemImageEntity> images = itemImageRepository.findByItem_ItemId(item.getItemId());
        if (images.isEmpty()) return List.of();

        LocalDateTime now = LocalDateTime.now();
        List<Long> imageIds = new ArrayList<>();

        for (ItemImageEntity image : images) {
            image.setStatus(ItemImageStatus.PENDING_DELETION);
            if (image.getMarkedForDeletionAt() == null) {
                image.setMarkedForDeletionAt(now);
            }
            image.setItem(null);
            imageIds.add(image.getId());
        }

        itemImageRepository.saveAll(images);
        itemImageRepository.flush();

        applicationEventPublisher.publishEvent(
                new ItemImageCleanupRequestedEvent(imageIds)
        );

        item.getImages().clear();
        return imageIds;
    }

    private void validateImageCount(int count) {
        if (count > MAX_IMAGES) {
            throw new IllegalArgumentException(
                    "A listing can contain at most " + MAX_IMAGES + " images"
            );
        }
    }

    private void validateNewImageRequest(ItemImageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        if (isBlank(request.getSecureUrl())
                || isBlank(request.getPublicId())
                || isBlank(request.getAssetId())
                || isBlank(request.getFormat())) {
            throw new IllegalArgumentException("Cloudinary image metadata is incomplete");
        }

        if (request.getWidth() == null || request.getWidth() <= 0
                || request.getHeight() == null || request.getHeight() <= 0
                || request.getBytes() == null || request.getBytes() <= 0) {
            throw new IllegalArgumentException(
                    "Cloudinary image dimensions and size must be valid"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}