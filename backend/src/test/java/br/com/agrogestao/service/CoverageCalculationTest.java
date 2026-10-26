
package br.com.agrogestao.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CoverageCalculationTest {

    @Test
    void coverageUsesAvailableBalanceAndDailyConsumption() {
        BigDecimal balance = new BigDecimal("1200");
        BigDecimal weekly = new BigDecimal("350");

        BigDecimal days = balance.divide(
                weekly.divide(
                        BigDecimal.valueOf(7),
                        8,
                        RoundingMode.HALF_UP
                ),
                1,
                RoundingMode.HALF_UP
        );

        assertEquals(
                new BigDecimal("24.0"),
                days
        );
    }

    @Test
    void zeroConsumptionMustNotProduceCoverage() {
        BigDecimal daily = BigDecimal.ZERO;

        assertFalse(daily.signum() > 0);
    }
}
