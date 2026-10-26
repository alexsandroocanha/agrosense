
package br.com.agrogestao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "supply",
        indexes = @Index(
                name = "idx_supply_farm",
                columnList = "farmId"
        )
)
public class Supply {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID farmId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal minimumStock = BigDecimal.ZERO;

    private String supplier;

    private String location;

    @Column(precision = 16, scale = 4)
    private BigDecimal unitCost;

    private boolean archived = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Version
    private long version;

    protected Supply() {
    }

    public Supply(
            UUID farmId,
            String name,
            String category,
            String unit,
            BigDecimal quantity,
            BigDecimal minimumStock,
            String supplier,
            String location,
            BigDecimal unitCost) {

        this.farmId = farmId;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.quantity = quantity;
        this.minimumStock = minimumStock;
        this.supplier = supplier;
        this.location = location;
        this.unitCost = unitCost;
    }

    public void update(
            String name,
            String category,
            String unit,
            BigDecimal minimumStock,
            String supplier,
            String location,
            BigDecimal unitCost) {

        this.name = name;
        this.category = category;
        this.unit = unit;
        this.minimumStock = minimumStock;
        this.supplier = supplier;
        this.location = location;
        this.unitCost = unitCost;
    }

    public void adjust(BigDecimal delta) {
        this.quantity = this.quantity.add(delta);
    }

    public void archive() {
        this.archived = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFarmId() {
        return farmId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public boolean isArchived() {
        return archived;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
