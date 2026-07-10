package smarttrade.backend.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.Mappers.ItemMapper;
import smarttrade.backend.dto.item.UpdateItemRequest;
import smarttrade.backend.entities.ItemEntity;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.ItemRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.ItemService;
import smarttrade.backend.service.PriceEstimationService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private ItemService itemService;

    private UserEntity user;
    private ItemEntity item;

    @BeforeEach
    void setup() {
        user = UserEntity.builder()
                .userId(1L)
                .email("test@test.com")
                .build();

        item = ItemEntity.builder()
                .itemId(1L)
                .itemName("Laptop")
                .condition("used - good")
                .userPrice(500)
                .user(user)
                .available(true)
                .build();
    }

    @Test
    void shouldReturnAllItems() {

        when(itemRepo.findAll()).thenReturn(List.of(item));

        List<ItemEntity> result = itemService.GetAllItems();

        assertEquals(1, result.size());
        verify(itemRepo).findAll();
    }

    @Test
    void shouldReturnItemById() {

        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        Optional<ItemEntity> result = itemService.GetItem(1L);

        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getItemName());
    }

    @Test
    void shouldAddItem() {

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);

        when(priceEstimationService.fetchEstimatedNewPrice(any(), any()))
                .thenReturn(1000);

        when(priceEstimationService.generateUsedPrice(any(), any()))
                .thenReturn(700);

        when(itemRepo.save(any(ItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ItemEntity saved = itemService.addItems(item);

        assertEquals(user, saved.getUser());
        assertEquals(1000, saved.getNewPrice());
        assertEquals(700, saved.getGeneratedPrice());

        verify(itemRepo).save(any(ItemEntity.class));
    }

    @Test
    void shouldUpdateItem() {

        UpdateItemRequest request = new UpdateItemRequest();
        request.setItemName("Updated Laptop");

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);

        when(itemRepo.findByItemIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(item));

        when(itemRepo.save(any(ItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ItemEntity updated = itemService.updateItem(1L, request);

        verify(itemMapper).updateEntityFromRequest(request, item);
        verify(itemRepo).save(item);
    }

    @Test
    void shouldThrowWhenUpdatingForeignItem() {

        UpdateItemRequest request = new UpdateItemRequest();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);

        when(itemRepo.findByItemIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ForbiddenOperationException.class,
                () -> itemService.updateItem(1L, request)
        );
    }

    @Test
    void shouldDeleteItem() {

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);

        when(itemRepo.findByItemIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(item));

        itemService.deleteItem(1L);

        verify(itemRepo).delete(item);
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
    void shouldSearchItems() {

        when(itemRepo.searchItems("electronics", "lap"))
                .thenReturn(List.of(item));

        List<ItemEntity> result =
                itemService.searchItems("electronics", null, "lap");

        assertEquals(1, result.size());
    }
}
