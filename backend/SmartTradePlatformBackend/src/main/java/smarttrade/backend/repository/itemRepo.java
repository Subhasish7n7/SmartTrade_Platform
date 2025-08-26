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

}
