
package br.com.agrogestao.repo;

import br.com.agrogestao.domain.Supply;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplyRepository extends JpaRepository<Supply, UUID> {

    @Query("""
            select s
            from Supply s
            where s.farmId = :farm
              and s.archived = false
            order by s.name
            """)
    List<Supply> findActive(@Param("farm") UUID farm);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from Supply s
            where s.id = :id
              and s.farmId = :farm
              and s.archived = false
            """)
    Optional<Supply> findForUpdate(
            @Param("id") UUID id,
            @Param("farm") UUID farm
    );

    Optional<Supply> findByIdAndFarmId(UUID id, UUID farm);

}
