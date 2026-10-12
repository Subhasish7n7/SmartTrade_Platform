package smarttrade.backend.repository.item;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemModerationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepo extends JpaRepository<ItemEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemEntity i where i.itemId = :itemId")
    Optional<ItemEntity> findByIdForUpdate(@Param("itemId") Long itemId);

    @Query(
            value = """
                    SELECT i.*
                    FROM items i
                    WHERE i.is_available = true
                      AND (i.is_for_sale = true OR i.is_for_trade = true)
                      AND i.moderation_status = 'ACTIVE'
                    ORDER BY i.created_at DESC
                    """,
            nativeQuery = true
    )
    List<ItemEntity> findRecentMarketplaceItems(Pageable pageable);

    Optional<ItemEntity> findByItemIdAndModerationStatus(Long itemId, ItemModerationStatus moderationStatus);

    List<ItemEntity> findAllByModerationStatus(ItemModerationStatus moderationStatus);

    List<ItemEntity> findByUser_UserIdAndModerationStatus(Long userId, ItemModerationStatus moderationStatus);

    long countByModerationStatus(ItemModerationStatus moderationStatus);

    long countByAvailableTrue();

    @Query("""
            SELECT i
            FROM ItemEntity i
            JOIN i.user u
            WHERE
                (
                    LOWER(i.itemName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                )
                AND (:moderationStatus IS NULL
                     OR i.moderationStatus = :moderationStatus)
                AND (:available IS NULL
                     OR i.available = :available)
            ORDER BY i.createdAt DESC
            """)
    Page<ItemEntity> searchAdminItems(
            @Param("search") String search,
            @Param("moderationStatus") ItemModerationStatus moderationStatus,
            @Param("available") Boolean available,
            Pageable pageable
    );

    @Query("""
            SELECT i
            FROM ItemEntity i
            JOIN i.user u
            WHERE
                (:moderationStatus IS NULL
                 OR i.moderationStatus = :moderationStatus)
                AND (:available IS NULL
                     OR i.available = :available)
            ORDER BY i.createdAt DESC
            """)
    Page<ItemEntity> searchAdminItemsByFilters(
            @Param("moderationStatus") ItemModerationStatus moderationStatus,
            @Param("available") Boolean available,
            Pageable pageable
    );

    Page<ItemEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(
            value = """
                    SELECT i.*
                    FROM items i
                    WHERE i.is_available = true
                    AND i.moderation_status = 'ACTIVE'
                      -- Only marketplace listings.
                      AND (
                          i.is_for_sale = true
                          OR i.is_for_trade = true
                      )
                    
                      -- ---------------------------------------------------------
                      -- Name
                      -- ---------------------------------------------------------
                      AND (
                          :name IS NULL
                          OR LOWER(i.item_name) LIKE LOWER(CONCAT('%', :name, '%'))
                      )
                    
                      -- ---------------------------------------------------------
                      -- Category
                      -- ---------------------------------------------------------
                      AND (
                          :category IS NULL
                          OR i.category = :category
                      )
                    
                      -- ---------------------------------------------------------
                      -- Conditions
                      --
                      -- conditions is NEVER null from the service.
                      -- conditionsActive determines whether the filter is used.
                      -- ---------------------------------------------------------
                      AND (
                          :conditionsActive = false
                          OR i.condition IN (:conditions)
                      )
                    
                      -- ---------------------------------------------------------
                      -- Price
                      -- ---------------------------------------------------------
                      AND (
                          :minPrice IS NULL
                          OR i.item_user_price >= :minPrice
                      )
                    
                      AND (
                          :maxPrice IS NULL
                          OR i.item_user_price <= :maxPrice
                      )
                    
                      -- ---------------------------------------------------------
                      -- Listing type
                      -- ---------------------------------------------------------
                      AND (
                          :listingType IS NULL
                    
                          OR (
                              :listingType = 'SALE'
                              AND i.is_for_sale = true
                          )
                    
                          OR (
                              :listingType = 'TRADE'
                              AND i.is_for_trade = true
                          )
                    
                          OR (
                              :listingType = 'BOTH'
                              AND i.is_for_sale = true
                              AND i.is_for_trade = true
                          )
                      )
                    
                      -- ---------------------------------------------------------
                      -- Labels
                      --
                      -- labels is NEVER null from the service.
                      -- labelsActive determines whether the filter is used.
                      -- ---------------------------------------------------------
                      AND (
                          :labelsActive = false
                          OR EXISTS (
                              SELECT 1
                              FROM item_labels il
                              WHERE il.item_id = i.item_id
                                AND LOWER(il.label) IN (:labels)
                          )
                      )
                    
                      -- ---------------------------------------------------------
                      -- Location / radius
                      --
                      -- Three possible states:
                      --
                      -- 1. No coordinates
                      --    -> no location filtering.
                      --
                      -- 2. Coordinates + radius
                      --    -> nearby search.
                      --
                      -- 3. Coordinates + NULL radius
                      --    -> ANYWHERE, therefore no radius restriction.
                      --
                      -- Consequently location filtering is active ONLY when
                      -- all three values exist.
                      -- ---------------------------------------------------------
                      AND (
                          :locationFilterActive = false
                          OR (
                              i.location IS NOT NULL
                              AND ST_DWithin(
                                  i.location,
                                  ST_SetSRID(
                                      ST_MakePoint(
                                          :longitude,
                                          :latitude
                                      ),
                                      4326
                                  )::geography,
                                  :radiusMeters
                              )
                          )
                      )
                    
                    ORDER BY
                    
                        -- -----------------------------------------------------
                        -- Price ascending
                        -- -----------------------------------------------------
                        CASE
                            WHEN :sort = 'PRICE_LOW_TO_HIGH'
                            THEN i.item_user_price
                        END ASC,
                    
                        -- -----------------------------------------------------
                        -- Price descending
                        -- -----------------------------------------------------
                        CASE
                            WHEN :sort = 'PRICE_HIGH_TO_LOW'
                            THEN i.item_user_price
                        END DESC,
                    
                        -- -----------------------------------------------------
                        -- Newest
                        -- -----------------------------------------------------
                        CASE
                            WHEN :sort = 'NEWEST'
                            THEN i.created_at
                        END DESC,
                    
                        -- -----------------------------------------------------
                        -- Distance
                        --
                        -- Only relevant when coordinates are available.
                        -- We deliberately do NOT require radius here.
                        -- So DISTANCE + coordinates + ANYWHERE works.
                        -- -----------------------------------------------------
                        CASE
                            WHEN :sort = 'DISTANCE'
                                 AND :hasCoordinates = true
                                 AND i.location IS NOT NULL
                            THEN ST_Distance(
                                i.location,
                                ST_SetSRID(
                                    ST_MakePoint(
                                        :longitude,
                                        :latitude
                                    ),
                                    4326
                                )::geography
                            )
                        END ASC,
                    
                        -- -----------------------------------------------------
                        -- Relevance
                        -- -----------------------------------------------------
                        CASE
                            WHEN :sort = 'RELEVANCE'
                            THEN
                                CASE
                                    WHEN :name IS NOT NULL
                                         AND LOWER(i.item_name) =
                                             LOWER(:name)
                                    THEN 100
                    
                                    WHEN :name IS NOT NULL
                                         AND LOWER(i.item_name) LIKE
                                             LOWER(CONCAT('%', :name, '%'))
                                    THEN 60
                    
                                    ELSE 0
                                END
                        END DESC,
                    
                        -- Stable fallback ordering.
                        i.created_at DESC
                    
                    """,

            countQuery = """
                    SELECT COUNT(*)
                    FROM items i
                    WHERE i.is_available = true
                    
                    AND i.moderation_status = 'ACTIVE'
                      AND (
                          i.is_for_sale = true
                          OR i.is_for_trade = true
                      )
                    
                      AND (
                          :name IS NULL
                          OR LOWER(i.item_name) LIKE LOWER(CONCAT('%', :name, '%'))
                      )
                    
                      AND (
                          :category IS NULL
                          OR i.category = :category
                      )
                    
                      AND (
                          :conditionsActive = false
                          OR i.condition IN (:conditions)
                      )
                    
                      AND (
                          :minPrice IS NULL
                          OR i.item_user_price >= :minPrice
                      )
                    
                      AND (
                          :maxPrice IS NULL
                          OR i.item_user_price <= :maxPrice
                      )
                    
                      AND (
                          :listingType IS NULL
                    
                          OR (
                              :listingType = 'SALE'
                              AND i.is_for_sale = true
                          )
                    
                          OR (
                              :listingType = 'TRADE'
                              AND i.is_for_trade = true
                          )
                    
                          OR (
                              :listingType = 'BOTH'
                              AND i.is_for_sale = true
                              AND i.is_for_trade = true
                          )
                      )
                    
                      AND (
                          :labelsActive = false
                          OR EXISTS (
                              SELECT 1
                              FROM item_labels il
                              WHERE il.item_id = i.item_id
                                AND LOWER(il.label) IN (:labels)
                          )
                      )
                    
                      AND (
                          :locationFilterActive = false
                          OR (
                              i.location IS NOT NULL
                              AND ST_DWithin(
                                  i.location,
                                  ST_SetSRID(
                                      ST_MakePoint(
                                          :longitude,
                                          :latitude
                                      ),
                                      4326
                                  )::geography,
                                  :radiusMeters
                              )
                          )
                      )
                    """,

            nativeQuery = true
    )
    Page<ItemEntity> searchItems(

            @Param("name")
            String name,

            @Param("category")
            String category,

            @Param("conditions")
            List<String> conditions,

            @Param("conditionsActive")
            boolean conditionsActive,

            @Param("labels")
            List<String> labels,

            @Param("labelsActive")
            boolean labelsActive,

            @Param("minPrice")
            Integer minPrice,

            @Param("maxPrice")
            Integer maxPrice,

            @Param("listingType")
            String listingType,

            @Param("latitude")
            Double latitude,

            @Param("longitude")
            Double longitude,

            @Param("radiusMeters")
            Double radiusMeters,

            @Param("locationFilterActive")
            boolean locationFilterActive,

            @Param("hasCoordinates")
            boolean hasCoordinates,

            @Param("sort")
            String sort,

            Pageable pageable
    );

    Optional<ItemEntity> findByItemIdAndUser_UserId(
            Long itemId,
            Long userId
    );

    List<ItemEntity> findByUser_UserId(
            Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select i
                from ItemEntity i
                where i.itemId = :itemId
                  and i.user.userId = :userId
            """)
    Optional<ItemEntity> findByItemIdAndUser_UserIdForUpdate(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT i
            FROM ItemEntity i
            WHERE i.user.userId = :userId
              AND i.moderationStatus = smarttrade.backend.entities.item.ItemModerationStatus.ACTIVE
              AND i.forTrade = true
              AND i.available = true
              AND i.traded = false
              AND i.sold = false
            """)
    List<ItemEntity> findTradeableItemsByUserId(
            @Param("userId") Long userId
    );
}