package com.financial.analytics.bonds.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PortfolioRiskServiceTests {

    @Autowired
    private PortfolioRiskService portfolioRiskService;

    @Test
    void testInvalidJsonLoad() throws Exception {
        String badJson = "{not a valid json]";
        try {
            portfolioRiskService.loadPortfolioFromJson(badJson);
            // if no exception, fail
            assertThat(true).isFalse();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }
}
