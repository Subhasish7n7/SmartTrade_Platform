package smarttrade.backend.UnitTest.Item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.dto.item.ItemImageRequest;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.events.itemImageCleanup.ItemImageCleanupRequestedEvent;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.item.ItemImageRepository;
import smarttrade.backend.service.item.ItemImageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemImageServiceTest {

    @Mock
    private ItemImageRepository itemImageRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ItemImageService itemImageService;

    private ItemEntity item;
    private ItemImageEntity image1;
    private ItemImageEntity image2;

    @BeforeEach
    void setUp() {
        item = ItemEntity.builder()
                .itemId(100L)
                .itemName("Laptop")
                .images(new ArrayList<>())
                .build();

        image1 = activeImage(1L, 0, "asset-1");
        image2 = activeImage(2L, 1, "asset-2");

        image1.setItem(item);
        image2.setItem(item);

        item.getImages().add(image1);
        item.getImages().add(image2);
    }

    @Test
    void shouldDoNothingWhenUpdateRequestsAreNull() {
        itemImageService.applyUpdateImages(item, null, null);

        verifyNoInteractions(itemImageRepository, applicationEventPublisher);
        assertEquals(2, item.getImages().size());
    }

    @Test
    void shouldRejectRemovedImageIdsWhenRequestsAreNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        null,
                        List.of(1L)
                )
        );

        assertEquals(
                "removedImageIds requires images to be provided",
                exception.getMessage()
        );

        verifyNoInteractions(itemImageRepository, applicationEventPublisher);
    }

    @Test
    void shouldRejectMoreThanEightImages() {
        List<ItemImageRequest> requests = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            requests.add(newImageRequest("asset-" + i, i));
        }

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(item, requests, null)
        );

        assertEquals(
                "A listing can contain at most 8 images",
                exception.getMessage()
        );

        verifyNoInteractions(itemImageRepository, applicationEventPublisher);
    }

    @Test
    void shouldAllowExactlyEightImages() {
        item.getImages().clear();

        List<ItemImageRequest> requests = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            requests.add(newImageRequest("asset-" + i, i));
        }

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of());

        itemImageService.applyUpdateImages(item, requests, null);

        assertEquals(8, item.getImages().size());
        assertEquals(
                List.of(
                        "asset-0",
                        "asset-1",
                        "asset-2",
                        "asset-3",
                        "asset-4",
                        "asset-5",
                        "asset-6",
                        "asset-7"
                ),
                item.getImages().stream()
                        .map(ItemImageEntity::getAssetId)
                        .toList()
        );

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldRejectDuplicateRetainedImageIds() {
        ItemImageRequest first = existingImageRequest(1L);
        ItemImageRequest duplicate = existingImageRequest(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        List.of(first, duplicate),
                        List.of(2L)
                )
        );

        assertEquals(
                "Duplicate item image IDs are not allowed",
                exception.getMessage()
        );

        verifyNoInteractions(itemImageRepository, applicationEventPublisher);
    }

    @Test
    void shouldRejectDuplicateRemovedImageIds() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        List.of(existingImageRequest(1L), existingImageRequest(2L)),
                        Arrays.asList(1L, 1L)
                )
        );

        assertEquals(
                "Duplicate removed image IDs are not allowed",
                exception.getMessage()
        );

        verifyNoInteractions(itemImageRepository, applicationEventPublisher);
    }

    @Test
    void shouldRejectImageThatIsBothRetainedAndRemoved() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        List.of(
                                existingImageRequest(1L),
                                existingImageRequest(2L)
                        ),
                        List.of(2L)
                )
        );

        assertEquals(
                "An image cannot be both retained and removed",
                exception.getMessage()
        );

        verifyNoInteractions(itemImageRepository, applicationEventPublisher);
    }

    @Test
    void shouldRequireEveryActiveExistingImageToBeRetainedOrRemoved() {
        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of(image1, image2));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        List.of(existingImageRequest(1L)),
                        null
                )
        );

        assertEquals(
                "The image update must explicitly retain or remove every active existing image",
                exception.getMessage()
        );

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldIgnorePendingDeletionImagesWhenCheckingExistingActiveImages() {
        ItemImageEntity pendingImage = activeImage(2L, 1, "asset-2");
        pendingImage.setStatus(ItemImageStatus.PENDING_DELETION);

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of(image1, pendingImage));

        when(itemImageRepository
                .findByIdAndItem_ItemIdAndStatus(
                        1L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                ))
                .thenReturn(Optional.of(image1));

        itemImageService.applyUpdateImages(
                item,
                List.of(existingImageRequest(1L)),
                null
        );

        assertEquals(0, image1.getSortOrder());
        assertEquals(ItemImageStatus.PENDING_DELETION, pendingImage.getStatus());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verify(itemImageRepository)
                .findByIdAndItem_ItemIdAndStatus(
                        1L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                );
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldReorderExistingImages() {
        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of(image1, image2));

        when(itemImageRepository
                .findByIdAndItem_ItemIdAndStatus(
                        1L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                ))
                .thenReturn(Optional.of(image1));

        when(itemImageRepository
                .findByIdAndItem_ItemIdAndStatus(
                        2L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                ))
                .thenReturn(Optional.of(image2));

        itemImageService.applyUpdateImages(
                item,
                List.of(
                        existingImageRequest(2L),
                        existingImageRequest(1L)
                ),
                null
        );

        assertEquals(0, image2.getSortOrder());
        assertEquals(1, image1.getSortOrder());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verify(itemImageRepository)
                .findByIdAndItem_ItemIdAndStatus(
                        2L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                );
        verify(itemImageRepository)
                .findByIdAndItem_ItemIdAndStatus(
                        1L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                );
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldAddNewImage() {
        item.getImages().clear();

        ItemImageRequest request = newImageRequest("asset-new", 0);

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of());

        itemImageService.applyUpdateImages(
                item,
                List.of(request),
                null
        );

        assertEquals(1, item.getImages().size());

        ItemImageEntity added = item.getImages().get(0);

        assertEquals(item, added.getItem());
        assertEquals("https://cdn.example.com/image.jpg", added.getSecureUrl());
        assertEquals("public-id", added.getPublicId());
        assertEquals("asset-new", added.getAssetId());
        assertEquals(1200, added.getWidth());
        assertEquals(800, added.getHeight());
        assertEquals("jpg", added.getFormat());
        assertEquals(123456L, added.getBytes());
        assertEquals(0, added.getSortOrder());
        assertEquals(ItemImageStatus.ACTIVE, added.getStatus());
        assertNotNull(added.getCreatedAt());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldRejectIncompleteNewImageMetadata() {
        item.getImages().clear();

        ItemImageRequest request = newImageRequest("asset-new", 0);
        request.setSecureUrl(" ");

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        List.of(request),
                        null
                )
        );

        assertEquals(
                "Cloudinary image metadata is incomplete",
                exception.getMessage()
        );

        assertTrue(item.getImages().isEmpty());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldRejectInvalidNewImageDimensionsOrBytes() {
        item.getImages().clear();

        ItemImageRequest request = newImageRequest("asset-new", 0);
        request.setWidth(0);

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        List.of(request),
                        null
                )
        );

        assertEquals(
                "Cloudinary image dimensions and size must be valid",
                exception.getMessage()
        );

        assertTrue(item.getImages().isEmpty());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldRejectDuplicateNewCloudinaryAssetIds() {
        item.getImages().clear();

        ItemImageRequest first = newImageRequest("duplicate-asset", 0);
        ItemImageRequest second = newImageRequest("duplicate-asset", 1);

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        List.of(first, second),
                        null
                )
        );

        assertEquals(
                "Duplicate Cloudinary asset IDs are not allowed",
                exception.getMessage()
        );

        assertEquals(1, item.getImages().size());
        assertEquals("duplicate-asset", item.getImages().get(0).getAssetId());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldAddNewImageAndMarkRemovedImageForDeletion() {
        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of(image1, image2));

        when(itemImageRepository
                .findByIdAndItem_ItemIdAndStatus(
                        1L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                ))
                .thenReturn(Optional.of(image1));

        when(itemImageRepository
                .findByIdAndItem_ItemIdAndStatus(
                        2L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                ))
                .thenReturn(Optional.of(image2));

        ItemImageRequest newImage = newImageRequest("asset-new", 1);

        itemImageService.applyUpdateImages(
                item,
                List.of(
                        existingImageRequest(1L),
                        newImage
                ),
                List.of(2L)
        );

        assertEquals(0, image1.getSortOrder());
        assertEquals(ItemImageStatus.PENDING_DELETION, image2.getStatus());
        assertNotNull(image2.getMarkedForDeletionAt());

        assertEquals(3, item.getImages().size());
        assertEquals("asset-new", item.getImages().get(2).getAssetId());
        assertEquals(1, item.getImages().get(2).getSortOrder());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());

        verify(itemImageRepository)
                .findByIdAndItem_ItemIdAndStatus(
                        1L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                );

        verify(itemImageRepository)
                .findByIdAndItem_ItemIdAndStatus(
                        2L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ItemImageEntity>> savedCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(itemImageRepository).saveAll(savedCaptor.capture());

        assertEquals(1, savedCaptor.getValue().size());
        assertSame(image2, savedCaptor.getValue().get(0));

        ArgumentCaptor<ItemImageCleanupRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(ItemImageCleanupRequestedEvent.class);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        assertEquals(
                List.of(2L),
                eventCaptor.getValue().imageIds()
        );
    }

    @Test
    void shouldThrowWhenReferencedExistingImageCannotBeLoaded() {
        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of(image1));

        when(itemImageRepository
                .findByIdAndItem_ItemIdAndStatus(
                        1L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                ForbiddenOperationException.class,
                () -> itemImageService.applyUpdateImages(
                        item,
                        List.of(),
                        List.of(1L)
                )
        );

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verify(itemImageRepository)
                .findByIdAndItem_ItemIdAndStatus(
                        1L,
                        item.getItemId(),
                        ItemImageStatus.ACTIVE
                );
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldDoNothingWhenThereAreNoImagesToMarkForDeletion() {
        item.getImages().clear();

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of());

        List<Long> result =
                itemImageService.markAllImagesForDeletion(item);

        assertTrue(result.isEmpty());
        assertTrue(item.getImages().isEmpty());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verifyNoMoreInteractions(itemImageRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldMarkAllImagesForDeletionAndPublishCleanupEvent() {
        LocalDateTime existingMarkedAt = LocalDateTime.now().minusHours(2);
        image1.setMarkedForDeletionAt(existingMarkedAt);
        image2.setMarkedForDeletionAt(null);

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of(image1, image2));

        List<Long> result =
                itemImageService.markAllImagesForDeletion(item);

        assertEquals(
                2,
                result.size()
        );
        assertTrue(result.containsAll(List.of(1L, 2L)));

        assertEquals(ItemImageStatus.PENDING_DELETION, image1.getStatus());
        assertEquals(ItemImageStatus.PENDING_DELETION, image2.getStatus());

        assertSame(existingMarkedAt, image1.getMarkedForDeletionAt());
        assertNotNull(image2.getMarkedForDeletionAt());

        assertNull(image1.getItem());
        assertNull(image2.getItem());

        assertTrue(item.getImages().isEmpty());

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ItemImageEntity>> imagesCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(itemImageRepository).saveAll(imagesCaptor.capture());

        assertEquals(2, imagesCaptor.getValue().size());
        assertTrue(imagesCaptor.getValue().containsAll(List.of(image1, image2)));

        verify(itemImageRepository).flush();

        ArgumentCaptor<ItemImageCleanupRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(ItemImageCleanupRequestedEvent.class);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        assertEquals(2, eventCaptor.getValue().imageIds().size());
        assertTrue(
                eventCaptor.getValue()
                        .imageIds()
                        .containsAll(List.of(1L, 2L))
        );

        verifyNoMoreInteractions(itemImageRepository, applicationEventPublisher);
    }

    @Test
    void shouldSetDeletionTimestampForImagesWithoutOneButPreserveExistingTimestamp() {
        LocalDateTime existingMarkedAt = LocalDateTime.now().minusDays(1);
        image1.setMarkedForDeletionAt(existingMarkedAt);
        image2.setMarkedForDeletionAt(null);

        when(itemImageRepository.findByItem_ItemId(item.getItemId()))
                .thenReturn(List.of(image1, image2));

        itemImageService.markAllImagesForDeletion(item);

        assertSame(existingMarkedAt, image1.getMarkedForDeletionAt());
        assertNotNull(image2.getMarkedForDeletionAt());
        assertFalse(image2.getMarkedForDeletionAt().isBefore(existingMarkedAt));

        verify(itemImageRepository).findByItem_ItemId(item.getItemId());
        verify(itemImageRepository).saveAll(anyList());
        verify(itemImageRepository).flush();
        verify(applicationEventPublisher).publishEvent(any(ItemImageCleanupRequestedEvent.class));
    }

    private ItemImageEntity activeImage(
            Long id,
            int sortOrder,
            String assetId
    ) {
        return ItemImageEntity.builder()
                .id(id)
                .secureUrl("https://cdn.example.com/" + assetId + ".jpg")
                .publicId("public-" + assetId)
                .assetId(assetId)
                .width(1200)
                .height(800)
                .format("jpg")
                .bytes(123456L)
                .sortOrder(sortOrder)
                .status(ItemImageStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private ItemImageRequest existingImageRequest(Long id) {
        ItemImageRequest request = new ItemImageRequest();
        request.setId(id);
        return request;
    }

    private ItemImageRequest newImageRequest(
            String assetId,
            int sortOrder
    ) {
        ItemImageRequest request = new ItemImageRequest();
        request.setSecureUrl("https://cdn.example.com/image.jpg");
        request.setPublicId("public-id");
        request.setAssetId(assetId);
        request.setWidth(1200);
        request.setHeight(800);
        request.setFormat("jpg");
        request.setBytes(123456L);
        request.setSortOrder(sortOrder);
        return request;
    }
}
