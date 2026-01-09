package com.financial.analytics.bonds.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.financial.analytics.bonds.domain.Bond;

@SpringBootTest
public class BondRiskServiceAdditionalTests {

    @Autowired
    private BondRiskService bondRiskService;

    @Test
    void parBondYtmEqualsCouponRate() {
        Bond b = new Bond("P1", LocalDate.now().plusYears(5), 0.05, 1000.0, 1000.0);
        double ytm = bondRiskService.calculateYTM(b);
    // For a par bond, YTM should equal the coupon rate (within tolerance)
    assertThat(ytm).isCloseTo(0.05, org.assertj.core.data.Offset.offset(1e-4));
    }

    @Test
    void discountBondYtmAboveCoupon() {
        Bond b = new Bond("D1", LocalDate.now().plusYears(7), 0.04, 1000.0, 950.0);
        double ytm = bondRiskService.calculateYTM(b);
        assertThat(Double.isFinite(ytm)).isTrue();
        assertThat(ytm).isGreaterThan(0.04);
    }

    @Test
    void premiumBondYtmBelowCoupon() {
        Bond b = new Bond("PR1", LocalDate.now().plusYears(3), 0.06, 1000.0, 1030.0);
        double ytm = bondRiskService.calculateYTM(b);
        assertThat(Double.isFinite(ytm)).isTrue();
        assertThat(ytm).isLessThan(0.06).isGreaterThan(-0.5);
    }

    @Test
    void semiAnnualBondYtmComputes() {
        Bond b = new Bond("S1", LocalDate.now().plusYears(10), 0.05, 1000.0, 980.0, java.util.List.of(), Bond.Frequency.SEMI_ANNUAL);
        double ytm = bondRiskService.calculateYTM(b);
        assertThat(Double.isFinite(ytm)).isTrue();
    }

    @Test
    void modifiedDurationNonNegative() {
        Bond b = new Bond("M2", LocalDate.now().plusYears(4), 0.03, 1000.0, 990.0);
        double md = bondRiskService.calculateModifiedDuration(b);
        assertThat(md).isGreaterThanOrEqualTo(0.0);
    }
}
