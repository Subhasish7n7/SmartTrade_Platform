package smarttrade.backend.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemSearchResponse {
    private List<ItemResponse> items;
    private int page;
    private int size;
    private long totalItems;
    private boolean hasNext;
}