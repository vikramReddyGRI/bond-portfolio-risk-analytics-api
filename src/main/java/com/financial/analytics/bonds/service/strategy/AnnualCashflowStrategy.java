package com.financial.analytics.bonds.service.strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Cashflow;

@Component
public class AnnualCashflowStrategy implements CashflowStrategy {

	@Override
	public List<Cashflow> generateCashflows(Bond bond) {
		List<Cashflow> cfs = new ArrayList<>();
		LocalDate now = LocalDate.now();
		LocalDate maturity = bond.getMaturityDate();
		// If maturity is in the past or today, there are no future cashflows
		if (!maturity.isAfter(now)) {
			return cfs;
		}
		double coupon = bond.getFaceValue() * bond.getCouponRate();

		final double DAYS_PER_YEAR = 365.0;

		// Use custom coupon dates if provided
		if (bond.getCouponDates() != null && !bond.getCouponDates().isEmpty()) {
			for (LocalDate d : bond.getCouponDates()) {
				if (!d.isBefore(now)) {
					double timeYears = ChronoUnit.DAYS.between(now, d) / DAYS_PER_YEAR;
					boolean isMaturity = d.equals(maturity);
					double amount = coupon + (isMaturity ? bond.getFaceValue() : 0.0);
					cfs.add(new Cashflow(timeYears, amount));
				}
			}
			return cfs;
		}

		// Generate annual schedule until maturity (always include final maturity payment)
		LocalDate paymentDate = now.plusYears(1);
		while (true) {
			if (paymentDate.isAfter(maturity)) {
				// maturity occurs before the next regular anniversary -> add maturity payment and stop
				paymentDate = maturity;
				double timeYears = ChronoUnit.DAYS.between(now, paymentDate) / DAYS_PER_YEAR;
				double amount = coupon + bond.getFaceValue();
				cfs.add(new Cashflow(timeYears, amount));
				break;
			} else {
				// if paymentDate == maturity, add final payment and stop (avoid duplicate maturity)
				if (paymentDate.equals(maturity)) {
					double timeYears = ChronoUnit.DAYS.between(now, paymentDate) / DAYS_PER_YEAR;
					double amount = coupon + bond.getFaceValue();
					cfs.add(new Cashflow(timeYears, amount));
					break;
				}

				double timeYears = ChronoUnit.DAYS.between(now, paymentDate) / DAYS_PER_YEAR;
				double amount = coupon;
				cfs.add(new Cashflow(timeYears, amount));
				// advance to next anniversary
				paymentDate = paymentDate.plusYears(1);
			}
		}

		return cfs;
	}
}
