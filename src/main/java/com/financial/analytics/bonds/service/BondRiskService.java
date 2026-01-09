
package com.financial.analytics.bonds.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Cashflow;
import com.financial.analytics.bonds.service.strategy.CashflowStrategyFactory;

@Service
public class BondRiskService {

	@Autowired
	private CashflowStrategyFactory strategyFactory;

	public double calculateYTM(Bond bond) {
		List<Cashflow> cashflows = strategyFactory.getStrategy(bond).generateCashflows(bond);
		if (cashflows.isEmpty())
			return 0.0;

		double marketPrice = bond.getMarketPrice();
		int periodsPerYear = bond.getFrequency() == Bond.Frequency.SEMI_ANNUAL ? 2 : 1;

		// --- Initial guess for YTM ---
		double initialGuess = bond.getCouponRate();
		if (marketPrice > 0) {
			double lastCashflowTime = Math.max(1.0, cashflows.get(cashflows.size() - 1).timeYears());
			double averagePrice = (bond.getFaceValue() + marketPrice) / 2.0;
			initialGuess = (bond.getCouponRate() * bond.getFaceValue()
					+ (bond.getFaceValue() - marketPrice) / lastCashflowTime) / averagePrice;
		}
		initialGuess = Math.max(initialGuess, -0.999);

		final double TOLERANCE = 1e-10;
		final int MAX_ITERATIONS = 100;

		double yield = initialGuess;

		for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
			double presentValue = calculatePresentValue(cashflows, yield, periodsPerYear);
			double derivative = calculatePresentValueDerivative(cashflows, yield, periodsPerYear);

			if (Double.isNaN(derivative) || Math.abs(derivative) < 1e-14)
				break;

			double step = (presentValue - marketPrice) / derivative;
			yield -= step;

			if (Math.abs(step) < TOLERANCE)
				return yield;
			if (yield <= -0.999 || Double.isNaN(yield) || Double.isInfinite(yield)) {
				yield = -0.999;
				break;
			}
		}

		return yield;
	}

	// --- Helper: Calculate present value of cashflows ---
	private double calculatePresentValue(List<Cashflow> cashflows, double annualYield, int periodsPerYear) {
		double periodRate = annualYield / periodsPerYear;
		if (1 + periodRate <= 0)
			return Double.NaN;

		double presentValue = 0.0;
		for (Cashflow cashflow : cashflows) {
			double totalPeriods = cashflow.timeYears() * periodsPerYear;
			presentValue += cashflow.amount() / Math.pow(1 + periodRate, totalPeriods);
		}
		return presentValue;
	}

	// --- Helper: Calculate derivative of present value w.r.t YTM ---
	private double calculatePresentValueDerivative(List<Cashflow> cashflows, double annualYield, int periodsPerYear) {
		double periodRate = annualYield / periodsPerYear;
		if (1 + periodRate <= 0)
			return Double.NaN;

		double derivative = 0.0;
		for (Cashflow cashflow : cashflows) {
			double totalPeriods = cashflow.timeYears() * periodsPerYear;
			double discountFactor = Math.pow(1 + periodRate, totalPeriods);
			derivative += -(totalPeriods / periodsPerYear) * cashflow.amount() / (discountFactor * (1 + periodRate));
		}
		return derivative;
	}

	// Backwards-compatible wrapper expected by older tests (reflection).
	@SuppressWarnings("unused")
	private double presentValue(List<Cashflow> cashflows, double annualYield, int periodsPerYear) {
		return calculatePresentValue(cashflows, annualYield, periodsPerYear);
	}

	@SuppressWarnings("unused")
	private double presentValueDerivative(List<Cashflow> cashflows, double annualYield, int periodsPerYear) {
		return calculatePresentValueDerivative(cashflows, annualYield, periodsPerYear);
	}

	// Macaulay duration in years using generated cashflows
	public double calculateDuration(Bond bond) {
		List<Cashflow> cfs = strategyFactory.getStrategy(bond).generateCashflows(bond);
		if (cfs.isEmpty())
			return 0.0;

		double price = bond.getMarketPrice();
		int freq = bond.getFrequency() == Bond.Frequency.SEMI_ANNUAL ? 2 : 1;
		double annualYield = calculateYTM(bond);

		double weightedSum = 0.0;
		for (Cashflow cf : cfs) {
			double timeYears = cf.timeYears();
			double totalPeriods = timeYears * freq;
			double discountPow = Math.pow(1 + annualYield / freq, totalPeriods);
			weightedSum += timeYears * cf.amount() / discountPow;
		}
		return weightedSum / price;
	}

	public double calculateModifiedDuration(Bond bond) {
		double macaulay = calculateDuration(bond);
		int freq = bond.getFrequency() == Bond.Frequency.SEMI_ANNUAL ? 2 : 1;
		double annualYield = calculateYTM(bond);
		return macaulay / (1 + annualYield / freq);
	}
}
