
package br.com.agrogestao.repo;

import br.com.agrogestao.domain.Movement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovementRepository extends JpaRepository<Movement, UUID> {

    List<Movement> findTop100ByFarmIdOrderByMovementDateDescCreatedAtDesc(
            UUID farm
    );

    List<Movement> findByFarmIdAndSupplyIdOrderByMovementDateAsc(
            UUID farm,
            UUID supplyId
    );

    long countByFarmIdAndSupplyIdAndType(
            UUID farm,
            UUID supplyId,
            String type
    );

}
