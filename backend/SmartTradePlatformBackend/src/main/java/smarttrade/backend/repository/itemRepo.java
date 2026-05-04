package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smarttrade.backend.entities.itemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface itemRepo extends JpaRepository<itemEntity,Long> {
    @Query(value = """
    SELECT * FROM items
    WHERE is_available = true
      AND ST_DWithin(
        location,
        ST_MakePoint(:lng, :lat)::geography,
        :radiusMeters
    )""", nativeQuery = true)
    List<itemEntity> findNearbyItems(
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
    List<itemEntity> searchItems(
            @Param("category") String category,
            @Param("name") String name
    );
}
