package com.financial.analytics.bonds.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Cashflow;
import com.financial.analytics.bonds.service.strategy.CashflowStrategyFactory;

@SpringBootTest
public class BondRiskServiceReflectionTests {

    @Autowired
    private BondRiskService bondRiskService;

    @Autowired
    private CashflowStrategyFactory strategyFactory;

    @Test
    void presentValueAndDerivativeEdgeCases() throws Exception {
        Bond bond = new Bond("R1", LocalDate.now().plusYears(5), 0.05, 1000.0, 950.0);
        List<Cashflow> cashflows = strategyFactory.getStrategy(bond).generateCashflows(bond);

    Method pvMethod = BondRiskService.class.getDeclaredMethod("calculatePresentValue", List.class, double.class, int.class);
        pvMethod.setAccessible(true);
    Method derivMethod = BondRiskService.class.getDeclaredMethod("calculatePresentValueDerivative", List.class, double.class, int.class);
        derivMethod.setAccessible(true);

        // normal yield -> pv finite
        Object pvNormal = pvMethod.invoke(bondRiskService, cashflows, 0.05, 1);
        assertThat(pvNormal).isInstanceOf(Double.class);
        double pvVal = (Double) pvNormal;
        assertThat(Double.isFinite(pvVal)).isTrue();

        // derivative at normal yield -> finite (or at least not NaN)
        Object dValObj = derivMethod.invoke(bondRiskService, cashflows, 0.05, 1);
        assertThat(dValObj).isInstanceOf(Double.class);
        double derivVal = (Double) dValObj;
        assertThat(Double.isFinite(derivVal)).isTrue();

        // extreme negative yield causing periodic base <= 0 should yield NaN
        Object pvNaN = pvMethod.invoke(bondRiskService, cashflows, -2.0, 1);
        assertThat(pvNaN).isInstanceOf(Double.class);
        assertThat(Double.isNaN((Double) pvNaN)).isTrue();

        Object derivNaN = derivMethod.invoke(bondRiskService, cashflows, -2.0, 1);
        assertThat(derivNaN).isInstanceOf(Double.class);
        assertThat(Double.isNaN((Double) derivNaN)).isTrue();
    }
}
