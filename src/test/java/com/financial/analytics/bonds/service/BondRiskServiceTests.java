package com.financial.analytics.bonds.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import com.financial.analytics.bonds.domain.Bond;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BondRiskServiceTests {

    @Autowired
    private BondRiskService bondRiskService;

    @Test
    void testMaturedBondYtmAndDuration() {
        Bond b = new Bond("M1", LocalDate.now().minusDays(1), 0.05, 1000.0, 1000.0);
        double ytm = bondRiskService.calculateYTM(b);
        double dur = bondRiskService.calculateDuration(b);
        assertThat(ytm).isEqualTo(0.0);
        assertThat(dur).isEqualTo(0.0);
    }

    @Test
    void testZeroMarketPrice() {
        Bond b = new Bond("Z1", LocalDate.now().plusYears(5), 0.05, 1000.0, 0.0);
        // zero market price may cause solver issues; expect non-negative but finite result
        double ytm = bondRiskService.calculateYTM(b);
        assertThat(Double.isFinite(ytm)).isTrue();
    }
}
