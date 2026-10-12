package smarttrade.backend.entities.item;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ItemSpecification {

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 20)
    private SpecificationTier tier;

    @Column(name = "specification", nullable = false, columnDefinition = "TEXT")
    private String specification;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
