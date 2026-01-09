package com.financial.analytics.bonds.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Portfolio;
import com.financial.analytics.bonds.dto.BondDTO;
import com.financial.analytics.bonds.service.BondRiskService;
import com.financial.analytics.bonds.service.PortfolioRiskService;

@SpringBootTest
@AutoConfigureMockMvc
public class BondControllerIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BondRiskService bondRiskService;

    @Autowired
    private PortfolioRiskService portfolioRiskService;

    @Test
    void testYtmDurationEndpoints() throws Exception {
        BondDTO dto = new BondDTO();
        dto.isin = "TESTISIN";
        dto.maturityDate = LocalDate.now().plusYears(5).toString();
        dto.couponRate = 0.05;
        dto.faceValue = 1000.0;
        dto.marketPrice = 950.0;
        dto.frequency = "ANNUAL";

        String json = mapper.writeValueAsString(dto);

        MvcResult ytmRes = mvc.perform(post("/api/bonds/ytm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        // parse structured response { "isin":..., "ytm": ... }
        var node = mapper.readTree(ytmRes.getResponse().getContentAsString());
        double ytmFromApi = node.get("ytm").asDouble();

        Bond bond = new Bond(dto.isin, LocalDate.parse(dto.maturityDate), dto.couponRate, dto.faceValue, dto.marketPrice);
        double ytmExpected = bondRiskService.calculateYTM(bond);

        // allow small numerical differences (controller rounds to 4 decimals)
        double ytmExpectedRounded = round4(ytmExpected);
        org.assertj.core.api.Assertions.assertThat(ytmFromApi).isCloseTo(ytmExpectedRounded, org.assertj.core.data.Offset.offset(1e-6));

        MvcResult durRes = mvc.perform(post("/api/bonds/duration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        var nodeDur = mapper.readTree(durRes.getResponse().getContentAsString());
        double durationFromApi = nodeDur.get("duration").asDouble();
        double durationExpected = bondRiskService.calculateDuration(bond);
        double durationExpectedRounded = round4(durationExpected);
        org.assertj.core.api.Assertions.assertThat(durationFromApi).isCloseTo(durationExpectedRounded, org.assertj.core.data.Offset.offset(1e-6));

        MvcResult modRes = mvc.perform(post("/api/bonds/modified-duration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        var nodeMod = mapper.readTree(modRes.getResponse().getContentAsString());
                double modFromApi = nodeMod.get("modifiedDuration").asDouble();
                double modExpected = bondRiskService.calculateModifiedDuration(bond);
                double modExpectedRounded = round4(modExpected);
                org.assertj.core.api.Assertions.assertThat(modFromApi).isCloseTo(modExpectedRounded, org.assertj.core.data.Offset.offset(1e-6));
    }

    @Test
    void testPortfolioDurationEndpoint() throws Exception {
        BondDTO d1 = new BondDTO();
        d1.isin = "B1";
        d1.maturityDate = LocalDate.now().plusYears(3).toString();
        d1.couponRate = 0.04;
        d1.faceValue = 1000.0;
        d1.marketPrice = 980.0;
        d1.frequency = "ANNUAL";

        BondDTO d2 = new BondDTO();
        d2.isin = "B2";
        d2.maturityDate = LocalDate.now().plusYears(7).toString();
        d2.couponRate = 0.06;
        d2.faceValue = 1000.0;
        d2.marketPrice = 1020.0;
        d2.frequency = "ANNUAL";

        String json = mapper.writeValueAsString(new BondDTO[] { d1, d2 });

        MvcResult res = mvc.perform(post("/api/bonds/portfolio/duration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        var nodeP = mapper.readTree(res.getResponse().getContentAsString());
        double apiVal = nodeP.get("portfolioDuration").asDouble();

        Bond b1 = new Bond(d1.isin, LocalDate.parse(d1.maturityDate), d1.couponRate, d1.faceValue, d1.marketPrice);
        Bond b2 = new Bond(d2.isin, LocalDate.parse(d2.maturityDate), d2.couponRate, d2.faceValue, d2.marketPrice);
        Portfolio p = new Portfolio(java.util.List.of(b1, b2));
                double expected = portfolioRiskService.weightedAverageDuration(p);
                double expectedRounded = round4(expected);
                org.assertj.core.api.Assertions.assertThat(apiVal).isCloseTo(expectedRounded, org.assertj.core.data.Offset.offset(1e-6));
    }

        private static double round4(double value) {
                return java.math.BigDecimal.valueOf(value)
                                .setScale(4, java.math.RoundingMode.HALF_UP)
                                .doubleValue();
        }
}
