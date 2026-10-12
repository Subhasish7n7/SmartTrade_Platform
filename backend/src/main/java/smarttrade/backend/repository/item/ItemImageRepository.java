package smarttrade.backend.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;

import java.util.List;
import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImageEntity, Long> {

    Optional<ItemImageEntity> findByIdAndItem_ItemId(Long id, Long itemId);

    Optional<ItemImageEntity> findByIdAndItem_ItemIdAndStatus(
            Long id,
            Long itemId,
            ItemImageStatus status
    );

    List<ItemImageEntity> findByItem_ItemId(Long itemId);

    List<ItemImageEntity> findByItem_ItemIdAndStatusOrderBySortOrderAsc(
            Long itemId,
            ItemImageStatus status
    );

    List<ItemImageEntity> findTop50ByStatusOrderByMarkedForDeletionAtAsc(
            ItemImageStatus status
    );

    boolean existsByAssetId(String assetId);
}