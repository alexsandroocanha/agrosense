
package br.com.agrogestao.repo;

import br.com.agrogestao.domain.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<StockAlert, UUID> {

    List<StockAlert> findByFarmIdOrderByAnalyzedAtDesc(UUID farm);

    void deleteByFarmId(UUID farm);

}
