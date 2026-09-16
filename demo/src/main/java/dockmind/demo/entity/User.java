package dockmind.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.validator.constraints.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String uid;
}
