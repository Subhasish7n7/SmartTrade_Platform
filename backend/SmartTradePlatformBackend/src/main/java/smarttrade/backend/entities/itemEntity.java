package smarttrade.backend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="items")
public class itemEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long item_id;
    private String item_name;
    private int item_NewPrice;
    private int item_GeneratedPrice;
    private int item_UserPrice;
    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "userID")
    private userEntity user;

}
