package com.financial.analytics.bonds.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.financial.analytics.bonds.domain.Bond;

@SpringBootTest
public class BondRiskServiceFuzzTests {

    @Autowired
    private BondRiskService bondRiskService;

    @Test
    void exerciseManyScenarios() {
        double[] coupons = {0.0, 0.01, 0.03, 0.05};
        double[] prices = {800.0, 950.0, 1000.0, 1050.0, 1200.0};
        int[] years = {1, 3, 5, 10};

        for (double c : coupons) {
            for (double p : prices) {
                for (int y : years) {
                    Bond b = new Bond("FZ", LocalDate.now().plusYears(y), c, 1000.0, p);
                    double out = bondRiskService.calculateYTM(b);
                    assertThat(Double.isFinite(out)).isTrue();
                    // ensure periodic-rate safety bound
                    assertThat(out).isGreaterThanOrEqualTo(-0.999);
                }
                // also try semi-annual
                for (int y : years) {
                    Bond b2 = new Bond("FZ2", LocalDate.now().plusYears(y), 0.05, 1000.0, p, java.util.List.of(), Bond.Frequency.SEMI_ANNUAL);
                    double out2 = bondRiskService.calculateYTM(b2);
                    assertThat(Double.isFinite(out2)).isTrue();
                    assertThat(out2).isGreaterThanOrEqualTo(-0.999);
                }
            }
        }
    }
}
