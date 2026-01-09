package com.financial.analytics.bonds.service.strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Cashflow;

@Component
public class SemiAnnualCashflowStrategy implements CashflowStrategy {

	@Override
	public List<Cashflow> generateCashflows(Bond bond) {
		List<Cashflow> cfs = new ArrayList<>();
		LocalDate now = LocalDate.now();
		LocalDate maturity = bond.getMaturityDate();
		// If maturity is in the past or today, no future cashflows
		if (!maturity.isAfter(now)) {
			return cfs;
		}
		double coupon = bond.getFaceValue() * bond.getCouponRate();

		final int MONTHS_PER_PERIOD = 6;
		final double DAYS_PER_YEAR = 365.0;

		// Use provided coupon dates if available
		if (bond.getCouponDates() != null && !bond.getCouponDates().isEmpty()) {
			for (LocalDate d : bond.getCouponDates()) {
				if (!d.isBefore(now)) {
					double timeYears = ChronoUnit.DAYS.between(now, d) / DAYS_PER_YEAR;
					double amount = d.equals(maturity) ? coupon + bond.getFaceValue() : coupon / 2.0;
					cfs.add(new Cashflow(timeYears, amount));
				}
			}
			return cfs;
		}

		// Semi-annual schedule until maturity
		LocalDate paymentDate = now.plusMonths(MONTHS_PER_PERIOD);

		while (!paymentDate.isAfter(maturity)) {
			double timeYears = ChronoUnit.DAYS.between(now, paymentDate) / DAYS_PER_YEAR;
			boolean isMaturity = paymentDate.equals(maturity);
			double amount = (coupon / 2.0) + (isMaturity ? bond.getFaceValue() : 0.0);
			cfs.add(new Cashflow(timeYears, amount));

			if (isMaturity)
				break; // stop exactly at maturity
			paymentDate = paymentDate.plusMonths(MONTHS_PER_PERIOD);
			if (paymentDate.isAfter(maturity)) {
				paymentDate = maturity; // adjust last date if overshoots
			}
		}

		return cfs;
	}
}
