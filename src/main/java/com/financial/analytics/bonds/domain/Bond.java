
package com.financial.analytics.bonds.domain;

import java.time.LocalDate;
import java.util.List;

public class Bond {

    public enum Frequency { ANNUAL, SEMI_ANNUAL }

    private final String isin;
    private final LocalDate maturityDate;
    private final double couponRate;
    private final double faceValue;
    private final double marketPrice;
    private final List<LocalDate> couponDates; // optional explicit coupon dates
    private final Frequency frequency;

    public Bond(String isin, LocalDate maturityDate, double couponRate, double faceValue, double marketPrice) {
        this(isin, maturityDate, couponRate, faceValue, marketPrice, null, Frequency.ANNUAL);
    }

    public Bond(String isin, LocalDate maturityDate, double couponRate, double faceValue, double marketPrice,
                List<LocalDate> couponDates, Frequency frequency) {
        this.isin = isin;
        this.maturityDate = maturityDate;
        this.couponRate = couponRate;
        this.faceValue = faceValue;
        this.marketPrice = marketPrice;
        this.couponDates = couponDates;
        this.frequency = frequency == null ? Frequency.ANNUAL : frequency;
    }

    public String getIsin() { return isin; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public double getCouponRate() { return couponRate; }
    public double getFaceValue() { return faceValue; }
    public double getMarketPrice() { return marketPrice; }
    public List<LocalDate> getCouponDates() { return couponDates; }
    public Frequency getFrequency() { return frequency; }
}
