package com.financial.analytics.bonds.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Cashflow;

public class AnnualCashflowStrategyTests {

    @Test
    void generatesAnnualScheduleWhenNoExplicitDates() {
        AnnualCashflowStrategy s = new AnnualCashflowStrategy();
        Bond b = new Bond("A1", LocalDate.now().plusYears(3), 0.05, 1000.0, 995.0);
        List<Cashflow> cfs = s.generateCashflows(b);
        assertThat(cfs).isNotEmpty();
        // basic sanity checks
        double prevT = -1.0;
        for (Cashflow cf : cfs) {
            assertThat(cf.amount()).isGreaterThan(0);
            assertThat(cf.timeYears()).isGreaterThan(prevT);
            prevT = cf.timeYears();
        }
    }

    @Test
    void handlesExplicitDatesCorrectly() {
        AnnualCashflowStrategy s = new AnnualCashflowStrategy();
        Bond b = new Bond("A2", LocalDate.now().plusYears(2), 0.03, 1000.0, 1000.0,
                java.util.List.of(LocalDate.now().plusYears(1), LocalDate.now().plusYears(2)), Bond.Frequency.ANNUAL);
        List<Cashflow> cfs = s.generateCashflows(b);
        assertThat(cfs).hasSize(2);
    }
}
