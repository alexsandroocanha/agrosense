
package br.com.agrogestao.service;

import br.com.agrogestao.domain.*;
import br.com.agrogestao.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StockService {

    private final SupplyRepository supplies;
    private final MovementRepository movements;
    private final AlertRepository alerts;

    public StockService(
            SupplyRepository supplies,
            MovementRepository movements,
            AlertRepository alerts) {

        this.supplies = supplies;
        this.movements = movements;
        this.alerts = alerts;
    }

    public record SupplyInput(
            String name,
            String category,
            String unit,
            BigDecimal quantity,
            BigDecimal minimumStock,
            String supplier,
            String location,
            BigDecimal unitCost) {
    }

    public record MovementInput(
            UUID supplyId,
            String type,
            BigDecimal quantity,
            LocalDate movementDate,
            String counterparty,
            String responsible,
            String notes,
            BigDecimal unitCost) {
    }

    public List<Supply> list(UUID farm) {
        return supplies.findActive(farm);
    }

    public Supply create(UUID farm, SupplyInput input) {
        validateSupply(input);

        return supplies.save(
                new Supply(
                        farm,
                        input.name().trim(),
                        input.category(),
                        input.unit(),
                        input.quantity() == null
                                ? BigDecimal.ZERO
                                : input.quantity(),
                        input.minimumStock() == null
                                ? BigDecimal.ZERO
                                : input.minimumStock(),
                        input.supplier(),
                        input.location(),
                        input.unitCost()
                )
        );
    }

    public Supply update(UUID farm, UUID id, SupplyInput input) {
        validateSupply(input);

        Supply supply = supplies.findByIdAndFarmId(id, farm)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Insumo não encontrado."
                        )
                );

        supply.update(
                input.name().trim(),
                input.category(),
                input.unit(),
                input.minimumStock() == null
                        ? BigDecimal.ZERO
                        : input.minimumStock(),
                input.supplier(),
                input.location(),
                input.unitCost()
        );

        return supplies.save(supply);
    }

    public void archive(UUID farm, UUID id) {
        Supply supply = supplies.findByIdAndFarmId(id, farm)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Insumo não encontrado."
                        )
                );

        supply.archive();
        supplies.save(supply);
    }

    private void validateSupply(SupplyInput input) {
        if (input.name() == null
                || input.name().isBlank()
                || input.category() == null
                || input.unit() == null
                || input.unit().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome, categoria e unidade são obrigatórios."
            );
        }

        if (input.quantity() != null
                && input.quantity().signum() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantidade inicial não pode ser negativa."
            );
        }

        if (input.minimumStock() != null
                && input.minimumStock().signum() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estoque mínimo não pode ser negativo."
            );
        }

        if (input.unitCost() != null
                && input.unitCost().signum() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preço não pode ser negativo."
            );
        }
    }

    @Transactional
    public Movement movement(
            UUID farm,
            UUID responsibleId,
            MovementInput input) {

        if (input.quantity() == null
                || input.quantity().signum() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A quantidade deve ser maior que zero."
            );
        }

        String type = input.type() == null
                ? ""
                : input.type().toUpperCase();

        if (!List.of("ENTRADA", "SAIDA", "AJUSTE").contains(type)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo deve ser ENTRADA, SAIDA ou AJUSTE."
            );
        }

        Supply supply = supplies.findForUpdate(input.supplyId(), farm)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Insumo não encontrado."
                        )
                );

        if (type.equals("SAIDA")
                && supply.getQuantity().compareTo(input.quantity()) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Saída maior que o saldo disponível."
            );
        }

        if (type.equals("ENTRADA")) {
            supply.adjust(input.quantity());
        } else if (type.equals("SAIDA")) {
            supply.adjust(input.quantity().negate());
        } else {
            supply.adjust(input.quantity());
        }

        if (supply.getQuantity().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O saldo não pode ficar negativo."
            );
        }

        supplies.save(supply);

        Movement movement = movements.save(
                new Movement(
                        farm,
                        supply.getId(),
                        supply.getName(),
                        type,
                        input.quantity(),
                        supply.getUnit(),
                        input.movementDate() == null
                                ? LocalDate.now()
                                : input.movementDate(),
                        input.counterparty(),
                        input.responsible(),
                        input.notes()
                )
        );

        reanalyze(farm);

        return movement;
    }

    public List<Movement> history(UUID farm) {
        return movements
                .findTop100ByFarmIdOrderByMovementDateDescCreatedAtDesc(farm);
    }

    public List<Movement> historyFor(UUID farm, UUID supplyId) {
        if (supplies.findByIdAndFarmId(supplyId, farm).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Insumo não encontrado."
            );
        }

        return movements
                .findByFarmIdAndSupplyIdOrderByMovementDateAsc(
                        farm,
                        supplyId
                );
    }

    @Transactional
    public List<StockAlert> reanalyze(UUID farm) {
        alerts.deleteByFarmId(farm);

        List<StockAlert> result = new ArrayList<>();

        for (Supply supply : supplies.findActive(farm)) {

            if (supply.getQuantity()
                    .compareTo(supply.getMinimumStock()) <= 0) {

                String priority = supply.getQuantity().signum() == 0
                        ? "CRITICO"
                        : "ATENCAO";

                result.add(
                        new StockAlert(
                                farm,
                                supply.getId(),
                                supply.getName(),
                                "ESTOQUE_MINIMO",
                                priority,
                                "Saldo atual: "
                                        + supply.getQuantity()
                                                .stripTrailingZeros()
                                                .toPlainString()
                                        + " " + supply.getUnit()
                                        + "; mínimo configurado: "
                                        + supply.getMinimumStock()
                                                .stripTrailingZeros()
                                                .toPlainString()
                                        + " " + supply.getUnit() + ".",
                                "Conferir o saldo físico e avaliar reposição."
                        )
                );
            }

            if (supply.getQuantity().signum() < 0) {
                result.add(
                        new StockAlert(
                                farm,
                                supply.getId(),
                                supply.getName(),
                                "DADO_INCONSISTENTE",
                                "CRITICO",
                                "O saldo calculado está negativo; "
                                        + "previsões foram suspensas.",
                                "Investigar as movimentações e corrigir "
                                        + "por ajuste auditável."
                        )
                );
            }
        }

        for (Supply supply : supplies.findActive(farm)) {

            List<Movement> outs = movements
                    .findByFarmIdAndSupplyIdOrderByMovementDateAsc(
                            farm,
                            supply.getId()
                    )
                    .stream()
                    .filter(movement ->
                            movement.getType().equals("SAIDA")
                                    && !movement.getMovementDate()
                                            .isAfter(LocalDate.now())
                    )
                    .toList();

            if (outs.size() >= 4) {
                LocalDate first = outs.get(0).getMovementDate();

                long days = Math.max(
                        1,
                        ChronoUnit.DAYS.between(
                                first,
                                LocalDate.now()
                        ) + 1
                );

                BigDecimal total = outs.stream()
                        .map(Movement::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal daily = total.divide(
                        BigDecimal.valueOf(days),
                        6,
                        RoundingMode.HALF_UP
                );

                if (daily.signum() > 0) {
                    BigDecimal coverage = supply.getQuantity().divide(
                            daily,
                            1,
                            RoundingMode.HALF_UP
                    );

                    if (coverage.compareTo(BigDecimal.valueOf(15)) <= 0) {
                        String priority = coverage.compareTo(
                                BigDecimal.valueOf(7)
                        ) <= 0
                                ? "CRITICO"
                                : "ATENCAO";

                        result.add(
                                new StockAlert(
                                        farm,
                                        supply.getId(),
                                        supply.getName(),
                                        "RISCO_RUPTURA",
                                        priority,
                                        "Cobertura estimada de "
                                                + coverage.toPlainString()
                                                + " dias com base em "
                                                + outs.size()
                                                + " saídas desde "
                                                + first
                                                + ". Estimativa simples, "
                                                + "sem ajuste de sazonalidade.",
                                        "Avaliar reposição e revisar "
                                                + "o histórico de consumo."
                                )
                        );
                    }
                }
            }
        }

        return alerts.saveAll(result);
    }

    public List<StockAlert> getAlerts(UUID farm) {
        return alerts.findByFarmIdOrderByAnalyzedAtDesc(farm);
    }

    public Map<String, Object> dashboard(UUID farm) {
        List<Supply> all = supplies.findActive(farm);
        List<StockAlert> currentAlerts = getAlerts(farm);

        BigDecimal known = all.stream()
                .filter(supply -> supply.getUnitCost() != null)
                .map(supply ->
                        supply.getQuantity().multiply(supply.getUnitCost())
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long unknown = all.stream()
                .filter(supply -> supply.getUnitCost() == null)
                .count();

        return Map.of(
                "items", all.size(),
                "critical", currentAlerts.stream()
                        .filter(alert ->
                                alert.getPriority().equals("CRITICO"))
                        .count(),
                "attention", currentAlerts.stream()
                        .filter(alert ->
                                alert.getPriority().equals("ATENCAO"))
                        .count(),
                "knownStockValue", known,
                "itemsWithoutCost", unknown,
                "alerts", currentAlerts,
                "supplies", all
        );
    }
}
