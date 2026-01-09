package com.financial.analytics.bonds.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Cashflow;

public class SemiAnnualCashflowStrategyTests {

    @Test
    void generatesSemiAnnualScheduleWhenNoExplicitDates() {
        SemiAnnualCashflowStrategy s = new SemiAnnualCashflowStrategy();
        Bond b = new Bond("S1", LocalDate.now().plusYears(1), 0.05, 1000.0, 990.0);
        List<Cashflow> cfs = s.generateCashflows(b);
        // for ~1 year maturity expect at least 1 semi-annual period (implementation dependent)
        assertThat(cfs).isNotEmpty();
        // verify amounts are positive and times are increasing
        double prevT = -1.0;
        for (Cashflow cf : cfs) {
            assertThat(cf.amount()).isGreaterThan(0);
            assertThat(cf.timeYears()).isGreaterThan(prevT);
            prevT = cf.timeYears();
        }
    }

    @Test
    void usesExplicitCouponDatesIfProvided() {
        SemiAnnualCashflowStrategy s = new SemiAnnualCashflowStrategy();
        Bond b = new Bond("S2", LocalDate.now().plusYears(1), 0.04, 1000.0, 1000.0,
                java.util.List.of(LocalDate.now().plusMonths(6), LocalDate.now().plusYears(1)), Bond.Frequency.SEMI_ANNUAL);
        List<Cashflow> cfs = s.generateCashflows(b);
        assertThat(cfs).hasSize(2);
    }
}
