package smarttrade.backend.repository;

import smarttrade.backend.entities.itemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface itemRepo extends JpaRepository<itemEntity,Long> {

}
