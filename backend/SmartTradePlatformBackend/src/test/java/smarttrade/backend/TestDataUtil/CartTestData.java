package smarttrade.backend.TestDataUtil;

import smarttrade.backend.dto.CartItemDto;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.entities.ItemEntity;



public class CartTestData {
    public static CartItemDto CreateCartA(UserEntity user, ItemEntity item, int Quantity){
        CartItemDto dto= new CartItemDto();
        dto.setUserId(user.getUserId());
        dto.setItemId(item.getItemId());
        dto.setQuantity(Quantity);
        return dto;
    }
}
