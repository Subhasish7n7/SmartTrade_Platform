package smarttrade.backend.repository;

import smarttrade.backend.entities.userEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface userRepo extends JpaRepository<userEntity,Long> {
}
