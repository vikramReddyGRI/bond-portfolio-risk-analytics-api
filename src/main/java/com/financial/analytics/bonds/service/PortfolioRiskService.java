
package com.financial.analytics.bonds.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Portfolio;

@Service
public class PortfolioRiskService {

    @Autowired
    private BondRiskService bondRiskService;
    private final ObjectMapper mapper = new ObjectMapper();

    // Weighted average of modified durations (weights by market price)
    public double weightedAverageDuration(Portfolio portfolio) {
    if (portfolio == null || portfolio.getBonds() == null || portfolio.getBonds().isEmpty()) return 0.0;

    // Use only positive, finite market prices for weighting to avoid NaN/Infinite/negative weights
    double total = portfolio.getBonds().stream()
        .filter(b -> Double.isFinite(b.getMarketPrice()) && b.getMarketPrice() > 0.0)
        .mapToDouble(Bond::getMarketPrice)
        .sum();
    if (total <= 0.0) return 0.0;

    return portfolio.getBonds().stream()
        .filter(b -> Double.isFinite(b.getMarketPrice()) && b.getMarketPrice() > 0.0)
        .mapToDouble(b -> (b.getMarketPrice() / total) * bondRiskService.calculateModifiedDuration(b))
        .sum();
    }

    // Load a portfolio from a JSON string containing an array of bond objects
    public Portfolio loadPortfolioFromJson(String json) throws IOException {
        // Expecting JSON array of objects with fields: isin, maturityDate (ISO), couponRate, faceValue, marketPrice
    JsonNode root = mapper.readTree(json);
        if (!root.isArray()) throw new IOException("Expected JSON array of bonds");

        List<Bond> bonds = mapper.convertValue(root, new TypeReference<List<java.util.Map<String, Object>>>() {})
                .stream()
                .map(m -> {
                    String isin = (String) m.get("isin");
                    String mDate = (String) m.get("maturityDate");
                    double couponRate = ((Number) m.getOrDefault("couponRate", 0)).doubleValue();
                    double face = ((Number) m.getOrDefault("faceValue", 0)).doubleValue();
                    double price = ((Number) m.getOrDefault("marketPrice", 0)).doubleValue();
                    LocalDate dt = LocalDate.parse(mDate);
                    return new Bond(isin, dt, couponRate, face, price);
                }).toList();

        return new Portfolio(bonds);
    }
}
