
package br.com.agrogestao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_alert")
public class StockAlert {

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

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false, length = 1200)
    private String explanation;

    @Column(nullable = false, length = 1200)
    private String recommendation;

    @Column(nullable = false)
    private String status = "NOVO";

    @Column(nullable = false)
    private LocalDateTime analyzedAt = LocalDateTime.now();

    protected StockAlert() {
    }

    public StockAlert(
            UUID farmId,
            UUID supplyId,
            String supplyName,
            String type,
            String priority,
            String explanation,
            String recommendation) {

        this.farmId = farmId;
        this.supplyId = supplyId;
        this.supplyName = supplyName;
        this.type = type;
        this.priority = priority;
        this.explanation = explanation;
        this.recommendation = recommendation;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getPriority() {
        return priority;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }
}
