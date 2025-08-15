package smarttrade.backend.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users")
public class userEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long user_id;
    private String email;
    private String name;
    private  String phone_no;

}
