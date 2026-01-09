package com.financial.analytics.bonds.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Portfolio;

import java.time.LocalDate;

@SpringBootTest
public class PortfolioRiskServiceUnitTests {

    @Autowired
    private PortfolioRiskService portfolioRiskService;

    @Test
    void loadValidJsonCreatesPortfolio() throws Exception {
        String json = "[{'isin':'X1','maturityDate':'" + LocalDate.now().plusYears(2).toString() + "','couponRate':0.05,'faceValue':1000.0,'marketPrice':980.0}]";
        // use single quotes for readability then replace
        json = json.replace("'", "\"");
        var p = portfolioRiskService.loadPortfolioFromJson(json);
        assertThat(p).isNotNull();
        assertThat(p.getBonds()).hasSize(1);
        Bond b = p.getBonds().get(0);
        assertThat(b.getIsin()).isEqualTo("X1");
    }

    @Test
    void loadInvalidJsonThrows() {
        String bad = "{not a json]";
        try {
            portfolioRiskService.loadPortfolioFromJson(bad);
            assertThat(true).isFalse();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    void weightedAverageDurationHandlesEmpty() {
        Portfolio p = new Portfolio(java.util.List.of());
        double val = portfolioRiskService.weightedAverageDuration(p);
        assertThat(val).isEqualTo(0.0);
    }
}
