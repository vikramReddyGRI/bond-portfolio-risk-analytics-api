package com.financial.analytics.bonds.service.strategy;

import java.util.List;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Cashflow;

public interface CashflowStrategy {
    List<Cashflow> generateCashflows(Bond bond);
}
