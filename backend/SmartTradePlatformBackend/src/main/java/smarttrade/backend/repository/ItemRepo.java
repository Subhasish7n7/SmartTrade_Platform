package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smarttrade.backend.entities.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepo extends JpaRepository<ItemEntity,Long> {
    @Query(value = """
    SELECT * FROM items
    WHERE is_available = true
      AND ST_DWithin(
        location,
        ST_MakePoint(:lng, :lat)::geography,
        :radiusMeters
    )""", nativeQuery = true)
    List<ItemEntity> findNearbyItems(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters
    );

    @Query(value = """
    SELECT * FROM items
    WHERE is_available = true
    AND (:category IS NULL OR category = :category)
    AND (:name IS NULL OR item_name ILIKE CONCAT('%', :name, '%'))
    """, nativeQuery = true)
    List<ItemEntity> searchItems(
            @Param("category") String category,
            @Param("name") String name
    );

    Optional<ItemEntity> findByItemIdAndUser_UserId(
            Long itemId,
            Long userId
    );
}
