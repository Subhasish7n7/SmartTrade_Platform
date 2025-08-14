package smarttrade.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class userEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long user_id;
    private String email;
    private String name;
    private  String phone_no;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<itemEntity> items;

}
