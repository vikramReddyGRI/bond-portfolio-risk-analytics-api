package com.financial.analytics.bonds.service.strategy;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.financial.analytics.bonds.domain.Bond;

@Component
public class CashflowStrategyFactory {

    @Autowired
    private AnnualCashflowStrategy annual;

    @Autowired
    private SemiAnnualCashflowStrategy semiAnnual;

    public CashflowStrategy getStrategy(Bond bond) {
        return switch (bond.getFrequency()) {
            case SEMI_ANNUAL -> semiAnnual;
            case ANNUAL -> annual;
        };
    }
}
