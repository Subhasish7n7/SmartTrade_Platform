package smarttrade.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class itemEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long item_id;
    private String item_name;
    private int item_NewPrice;
    private int item_GeneratedPrice;
    private int item_UserPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false) // foreign key column
    private userEntity user;

}
