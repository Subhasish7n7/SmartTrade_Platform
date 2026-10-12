package smarttrade.backend.UnitTest.Item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import smarttrade.backend.Mappers.ItemMapper;
import smarttrade.backend.dto.item.ItemSearchRequest;
import smarttrade.backend.dto.item.ItemSearchResponse;
import smarttrade.backend.dto.item.ItemSort;
import smarttrade.backend.dto.item.UpdateItemRequest;
import smarttrade.backend.entities.item.*;
import smarttrade.backend.entities.user.*;
import smarttrade.backend.events.itemPriceFetch.ItemPersistedEvent;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.item.ItemImageService;
import smarttrade.backend.service.item.ItemService;
import smarttrade.backend.service.item.PublicItemVisibilityService;
import smarttrade.backend.service.pricing.*;
import smarttrade.backend.service.user.UserPreferenceService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepo itemRepo;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private PriceEstimationService priceEstimationService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PricingJobService pricingJobService;

    @Mock
    private PricingBackgroundService pricingBackgroundService;

    @Mock
    private ItemImageService itemImageService;
    @Mock
    private UserPreferenceService userPreferenceService;
    @Mock
    private PublicItemVisibilityService publicItemVisibilityService;

    @InjectMocks
    private ItemService itemService;

    private UserEntity user;
    private ItemEntity item;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .userId(1L)
                .email("test@test.com")
                .password("encoded-password")
                .name("Test User")
                .trustScore(4.5)
                .totalListings(3)
                .successfulTrades(2)
                .build();

        item = ItemEntity.builder()
                .itemId(1L)
                .itemName("Gaming Laptop")
                .newPrice(80000)
                .generatedPrice(55000)
                .userPrice(50000)
                .pricingStatus(ItemPricingStatus.READY)
                .priceFetchedAt(LocalDateTime.now().minusHours(2))
                .pricingVersion(1L)
                .pricingErrorCode(null)
                .pricingErrorMessage(null)
                .user(user)
                .description("Gaming laptop")
                .category(ItemCategory.COMPUTERS)
                .condition(ItemCondition.GOOD)
                .labels(List.of("gaming", "laptop"))
                .available(true)
                .forSale(true)
                .forTrade(false)
                .build();
    }


    @Test
    void shouldReturnAllItems() {
        when(itemRepo.findAllByModerationStatus(ItemModerationStatus.ACTIVE)).thenReturn(List.of(item));

        List<ItemEntity> result = itemService.GetAllItems();

        assertEquals(List.of(item), result);
        verify(itemRepo).findAllByModerationStatus(ItemModerationStatus.ACTIVE);
    }

    @Test
    void shouldReturnInventoryForUser() {
        when(itemRepo.findByUser_UserIdAndModerationStatus(1L, ItemModerationStatus.ACTIVE)).thenReturn(List.of(item));

        List<ItemEntity> result = itemService.getInventory(1L);

        assertEquals(List.of(item), result);
        verify(itemRepo).findByUser_UserIdAndModerationStatus(1L, ItemModerationStatus.ACTIVE);
    }

    @Test
    void shouldReturnItemById() {
        when(itemRepo.findByItemIdAndModerationStatus(1L, ItemModerationStatus.ACTIVE)).thenReturn(Optional.of(item));

        Optional<ItemEntity> result = itemService.GetItem(1L);

        assertTrue(result.isPresent());
        assertSame(item, result.get());
    }

    @Test
    void shouldAddItemAsPendingAndPublishPersistedEvent() {
        ItemEntity newItem = ItemEntity.builder()
                .itemName("Phone")
                .userPrice(30000)
                .category(ItemCategory.PHONES)
                .condition(ItemCondition.GOOD)
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.save(any(ItemEntity.class))).thenAnswer(invocation -> {
            ItemEntity entity = invocation.getArgument(0);
            entity.setItemId(10L);
            return entity;
        });

        ItemEntity saved = itemService.addItems(newItem);

        assertSame(user, saved.getUser());
        assertEquals(1L, saved.getPricingVersion());
        assertEquals(ItemPricingStatus.PENDING, saved.getPricingStatus());
        assertNull(saved.getPricingErrorCode());
        assertNull(saved.getPricingErrorMessage());

        verify(itemRepo).save(newItem);
        verify(applicationEventPublisher).publishEvent(any(ItemPersistedEvent.class));
        verifyNoInteractions(priceEstimationService);
    }

    @Test
    void shouldPreservePositivePricingVersionWhenAddingItem() {
        item.setPricingVersion(7L);
        item.setPricingStatus(ItemPricingStatus.FAILED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.save(any(ItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemEntity saved = itemService.addItems(item);

        assertEquals(7L, saved.getPricingVersion());
        assertEquals(ItemPricingStatus.PENDING, saved.getPricingStatus());
        verify(applicationEventPublisher).publishEvent(any(ItemPersistedEvent.class));
    }

    @Test
    void shouldRejectItemWithoutCategory() {
        item.setCategory(null);
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemService.addItems(item)
        );

        assertEquals("Category is required", exception.getMessage());
        verify(itemRepo, never()).save(any());
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldRejectOtherCategoryWithoutCustomCategory() {
        item.setCategory(ItemCategory.OTHER);
        item.setCustomCategory("   ");
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemService.addItems(item)
        );

        assertEquals("customCategory is required when category is OTHER", exception.getMessage());
        verify(itemRepo, never()).save(any());
    }

    @Test
    void shouldClearCustomCategoryForNonOtherCategory() {
        item.setCategory(ItemCategory.COMPUTERS);
        item.setCustomCategory("Old custom value");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.save(any(ItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemEntity saved = itemService.addItems(item);

        assertNull(saved.getCustomCategory());
    }

    @Test
    void shouldUpdateItemWithoutPricingInputChange() {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setDescription("Updated description");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserIdForUpdate(1L, 1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(any(ItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doAnswer(invocation -> {
            UpdateItemRequest dto = invocation.getArgument(0);
            ItemEntity entity = invocation.getArgument(1);
            entity.setDescription(dto.getDescription());
            return null;
        }).when(itemMapper).updateEntityFromRequest(request, item);

        ItemEntity updated = itemService.updateItem(1L, request);

        assertEquals("Updated description", updated.getDescription());
        assertEquals(1L, updated.getPricingVersion());
        assertEquals(ItemPricingStatus.READY, updated.getPricingStatus());
        assertEquals(55000, updated.getGeneratedPrice());

        verify(itemImageService).applyUpdateImages(item, null, null);
        verify(itemRepo).save(item);
        verify(applicationEventPublisher, never()).publishEvent(any(ItemPersistedEvent.class));
        verify(priceEstimationService, never()).generateUsedPrice(any(), any());
    }

    @Test
    void shouldRegenerateUsedPriceWhenConditionChanges() {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setCondition(ItemCondition.LIKE_NEW);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserIdForUpdate(1L, 1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(any(ItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(priceEstimationService.generateUsedPrice(80000, ItemCondition.LIKE_NEW))
                .thenReturn(65000);

        doAnswer(invocation -> {
            ItemEntity entity = invocation.getArgument(1);
            entity.setCondition(ItemCondition.LIKE_NEW);
            return null;
        }).when(itemMapper).updateEntityFromRequest(request, item);

        ItemEntity updated = itemService.updateItem(1L, request);

        assertEquals(ItemCondition.LIKE_NEW, updated.getCondition());
        assertEquals(65000, updated.getGeneratedPrice());
        assertEquals(1L, updated.getPricingVersion());
        assertEquals(ItemPricingStatus.READY, updated.getPricingStatus());

        verify(priceEstimationService).generateUsedPrice(80000, ItemCondition.LIKE_NEW);
        verify(itemRepo).save(item);
        verify(applicationEventPublisher, never()).publishEvent(any(ItemPersistedEvent.class));
    }

    @Test
    void shouldInvalidatePricingAndPublishEventWhenPricingInputsChange() {
        item.setPricingVersion(4L);
        item.setPricingStatus(ItemPricingStatus.FAILED);
        item.setPricingErrorCode("SERP_500");
        item.setPricingErrorMessage("Old pricing failure");

        UpdateItemRequest request = new UpdateItemRequest();
        request.setItemName("New Gaming Laptop");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserIdForUpdate(1L, 1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(any(ItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doAnswer(invocation -> {
            ItemEntity entity = invocation.getArgument(1);
            entity.setItemName("New Gaming Laptop");
            return null;
        }).when(itemMapper).updateEntityFromRequest(request, item);

        ItemEntity updated = itemService.updateItem(1L, request);

        assertEquals("New Gaming Laptop", updated.getItemName());
        assertEquals(5L, updated.getPricingVersion());
        assertEquals(ItemPricingStatus.PENDING, updated.getPricingStatus());
        assertNull(updated.getPricingErrorCode());
        assertNull(updated.getPricingErrorMessage());

        ArgumentCaptor<ItemPersistedEvent> eventCaptor = ArgumentCaptor.forClass(ItemPersistedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(1L, eventCaptor.getValue().itemId());

        verify(itemImageService).applyUpdateImages(item, null, null);
        verify(itemRepo).save(item);
    }

    @Test
    void shouldUseNewPricingVersionWhenExistingVersionIsNull() {
        item.setPricingVersion(null);

        UpdateItemRequest request = new UpdateItemRequest();
        request.setItemName("Renamed Laptop");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserIdForUpdate(1L, 1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(any(ItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doAnswer(invocation -> {
            ItemEntity entity = invocation.getArgument(1);
            entity.setItemName("Renamed Laptop");
            return null;
        }).when(itemMapper).updateEntityFromRequest(request, item);

        ItemEntity updated = itemService.updateItem(1L, request);

        assertEquals(1L, updated.getPricingVersion());
        assertEquals(ItemPricingStatus.PENDING, updated.getPricingStatus());
        verify(applicationEventPublisher).publishEvent(any(ItemPersistedEvent.class));
    }

    @Test
    void shouldThrowWhenUpdatingForeignItem() {
        UpdateItemRequest request = new UpdateItemRequest();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserIdForUpdate(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ForbiddenOperationException.class,
                () -> itemService.updateItem(1L, request)
        );

        verify(itemMapper, never()).updateEntityFromRequest(any(), any());
        verify(itemRepo, never()).save(any());
    }

    @Test
    void shouldDeleteItemAndMarkImagesForDeletion() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(item));
        when(itemImageService.markAllImagesForDeletion(item))
                .thenReturn(List.of(10L, 11L));

        itemService.deleteItem(1L);

        verify(itemImageService).markAllImagesForDeletion(item);
        verify(itemRepo).delete(item);
    }

    @Test
    void shouldThrowWhenDeletingForeignItem() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ForbiddenOperationException.class,
                () -> itemService.deleteItem(1L)
        );

        verify(itemImageService, never()).markAllImagesForDeletion(any());
        verify(itemRepo, never()).delete(any());
    }

    @Test
    void shouldUpdateAvailability() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(item));

        itemService.updateAvailability(1L, false);

        assertFalse(item.isAvailable());
        verify(itemRepo).save(item);
    }

    @Test
    void shouldThrowWhenUpdatingAvailabilityForForeignItem() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(itemRepo.findByItemIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ForbiddenOperationException.class,
                () -> itemService.updateAvailability(1L, false)
        );

        verify(itemRepo, never()).save(any());
    }

    @Test
    void shouldRefreshPriceAndExecuteReturnedJob() {
        PricingJobRequest job = new PricingJobRequest(
                1L,
                2L,
                "Gaming Laptop",
                ItemCategory.COMPUTERS,
                null,
                List.of("gaming"),
                List.of(), // primarySpecifications
                List.of(), // secondarySpecifications
                List.of()  // tertiarySpecifications
        );

        PricingRefreshResponse response = new PricingRefreshResponse(
                1L,
                PricingRefreshOutcome.STARTED,
                ItemPricingStatus.IN_PROGRESS,
                2L,
                50000,
                80000,
                55000,
                null,
                null,
                null,
                null,
                "Market pricing update started."
        );

        PricingJobDecision decision = new PricingJobDecision(response, job);
        when(pricingJobService.requestRefresh(1L)).thenReturn(decision);

        PricingRefreshResponse result = itemService.refreshItemPrice(1L);

        assertSame(response, result);
        verify(pricingJobService).requestRefresh(1L);
        verify(pricingBackgroundService).execute(job);
    }

    @Test
    void shouldNotExecuteBackgroundJobWhenRefreshDecisionHasNoJob() {
        PricingRefreshResponse response = new PricingRefreshResponse(
                1L,
                PricingRefreshOutcome.ALREADY_FRESH,
                ItemPricingStatus.READY,
                2L,
                50000,
                80000,
                55000,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(100),
                null,
                null,
                "Market pricing is already up to date."
        );

        when(pricingJobService.requestRefresh(1L))
                .thenReturn(new PricingJobDecision(response, null));

        PricingRefreshResponse result = itemService.refreshItemPrice(1L);

        assertSame(response, result);
        verify(pricingBackgroundService, never()).execute(any());
    }

    @Test
    void shouldReturnNotFoundRefreshDecisionWithoutExecutingJob() {
        PricingRefreshResponse response = new PricingRefreshResponse(
                999L,
                PricingRefreshOutcome.NOT_FOUND,
                null,
                0L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Item not found."
        );

        when(pricingJobService.requestRefresh(999L))
                .thenReturn(new PricingJobDecision(response, null));

        PricingRefreshResponse result = itemService.refreshItemPrice(999L);

        assertEquals(PricingRefreshOutcome.NOT_FOUND, result.outcome());
        assertEquals(999L, result.itemId());
        verify(pricingBackgroundService, never()).execute(any());
    }

    @Test
    void shouldRejectNullSearchRequest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemService.searchItems(null)
        );

        assertEquals("Search request cannot be null", exception.getMessage());
        verifyNoInteractions(itemRepo);
    }

    @Test
    void shouldRejectInvalidPriceRange() {
        ItemSearchRequest request = new ItemSearchRequest();
        request.setMinPrice(50000);
        request.setMaxPrice(10000);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemService.searchItems(request)
        );

        assertEquals("minPrice cannot be greater than maxPrice", exception.getMessage());
        verifyNoInteractions(itemRepo);
    }

    @Test
    void shouldRejectNonPositiveRadius() {
        ItemSearchRequest request = new ItemSearchRequest();
        request.setRadiusKm(0.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemService.searchItems(request)
        );

        assertEquals("radiusKm must be greater than 0", exception.getMessage());
        verifyNoInteractions(itemRepo);
    }

    @Test
    void shouldRejectIncompleteCoordinates() {
        ItemSearchRequest request = new ItemSearchRequest();
        request.setLatitude(20.2961);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemService.searchItems(request)
        );

        assertEquals("latitude and longitude must be provided together", exception.getMessage());
        verifyNoInteractions(itemRepo);
    }

    @Test
    void shouldRejectDistanceSortWithoutCoordinates() {
        ItemSearchRequest request = new ItemSearchRequest();
        request.setSort(ItemSort.DISTANCE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> itemService.searchItems(request)
        );

        assertEquals(
                "latitude and longitude are required when sorting by distance",
                exception.getMessage()
        );
        verifyNoInteractions(itemRepo);
    }

    @Test
    void shouldSearchWithNormalizedFiltersAndPagination() {
        when(publicItemVisibilityService.filterUnavailableListings(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ItemSearchRequest request = new ItemSearchRequest();
        request.setName("  gaming laptop  ");
        request.setCategory(ItemCategory.COMPUTERS);
        request.setConditions(Arrays.asList(ItemCondition.GOOD, null, ItemCondition.LIKE_NEW));
        request.setLabels(Arrays.asList(" Gaming ", "LAPTOP", "gaming", " ", null));
        request.setMinPrice(40000);
        request.setMaxPrice(70000);
        request.setLatitude(20.2961);
        request.setLongitude(85.8245);
        request.setRadiusKm(10.0);
        request.setSort(ItemSort.DISTANCE);
        request.setPage(-3);
        request.setSize(100);

        Pageable expectedPageable = PageRequest.of(0, 50);
        Page<ItemEntity> page = new PageImpl<>(List.of(item), expectedPageable, 1);

        when(itemRepo.searchItems(
                eq("gaming laptop"),
                eq("COMPUTERS"),
                eq(List.of("GOOD", "LIKE_NEW")),
                eq(true),
                eq(List.of("gaming", "laptop")),
                eq(true),
                eq(40000),
                eq(70000),
                isNull(),
                eq(20.2961),
                eq(85.8245),
                eq(10000.0),
                eq(true),
                eq(true),
                eq("DISTANCE"),
                eq(expectedPageable)
        )).thenReturn(page);

        when(itemMapper.mapFrom(item)).thenReturn(null);

        ItemSearchResponse response = itemService.searchItems(request);

        assertEquals(0, response.getPage());
        assertEquals(50, response.getSize());
        assertEquals(1, response.getTotalItems());
        assertFalse(response.isHasNext());
        assertEquals(Collections.singletonList(null), response.getItems());

        verify(itemRepo).searchItems(
                eq("gaming laptop"),
                eq("COMPUTERS"),
                eq(List.of("GOOD", "LIKE_NEW")),
                eq(true),
                eq(List.of("gaming", "laptop")),
                eq(true),
                eq(40000),
                eq(70000),
                isNull(),
                eq(20.2961),
                eq(85.8245),
                eq(10000.0),
                eq(true),
                eq(true),
                eq("DISTANCE"),
                eq(expectedPageable)
        );
    }

    @Test
    void shouldSendSentinelValuesWhenConditionAndLabelFiltersAreInactive() {
        when(publicItemVisibilityService.filterUnavailableListings(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ItemSearchRequest request = new ItemSearchRequest();
        request.setName("   ");
        request.setConditions(List.of());
        request.setLabels(Arrays.asList(null, " ", "  \t"));
        request.setSort(null);

        Pageable expectedPageable = PageRequest.of(0, 50);
        Page<ItemEntity> page = new PageImpl<>(List.of(item), expectedPageable, 1);

        when(itemRepo.searchItems(
                isNull(),
                isNull(),
                eq(List.of("__NO_CONDITION_FILTER__")),
                eq(false),
                eq(List.of("__NO_LABEL_FILTER__")),
                eq(false),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                eq(false),
                eq("RELEVANCE"),
                eq(expectedPageable)
        )).thenReturn(page);

        ItemSearchResponse response = itemService.searchItems(request);

        assertEquals(ItemSort.RELEVANCE.name(), "RELEVANCE");
        assertEquals(1, response.getTotalItems());

        verify(itemRepo).searchItems(
                isNull(),
                isNull(),
                eq(List.of("__NO_CONDITION_FILTER__")),
                eq(false),
                eq(List.of("__NO_LABEL_FILTER__")),
                eq(false),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                eq(false),
                eq("RELEVANCE"),
                eq(expectedPageable)
        );
    }
}
