package com.financial.analytics.bonds.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Small numeric helper utilities.
 */
public final class NumberUtils {

    private NumberUtils() {}

    /**
     * Round a double to the specified number of decimal places using HALF_UP.
     * Returns NaN/Infinity unchanged.
     */
    public static double round(double value, int places) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return value;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
