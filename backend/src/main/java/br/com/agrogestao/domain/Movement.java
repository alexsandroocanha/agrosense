
package br.com.agrogestao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "stock_movement",
        indexes = @Index(
                name = "idx_movement_supply",
                columnList = "supplyId"
        )
)
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID farmId;

    @Column(nullable = false)
    private UUID supplyId;

    @Column(nullable = false)
    private String supplyName;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private LocalDate movementDate;

    private String counterparty;

    private String responsible;

    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Movement() {
    }

    public Movement(
            UUID farmId,
            UUID supplyId,
            String supplyName,
            String type,
            BigDecimal quantity,
            String unit,
            LocalDate date,
            String counterparty,
            String responsible,
            String notes) {

        this.farmId = farmId;
        this.supplyId = supplyId;
        this.supplyName = supplyName;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.movementDate = date;
        this.counterparty = counterparty;
        this.responsible = responsible;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFarmId() {
        return farmId;
    }

    public UUID getSupplyId() {
        return supplyId;
    }

    public String getSupplyName() {
        return supplyName;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public String getResponsible() {
        return responsible;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
