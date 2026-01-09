package com.financial.analytics.bonds.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class BondDTO {
    @NotBlank
    public String isin;

    @NotBlank
    public String maturityDate;

    @NotNull
    @PositiveOrZero
    public Double couponRate;

    @NotNull
    @PositiveOrZero
    public Double faceValue;

    @NotNull
    @PositiveOrZero
    public Double marketPrice;

    // optional explicit coupon dates as ISO strings
    public List<String> couponDates;

    // frequency: "ANNUAL" or "SEMI_ANNUAL" (default ANNUAL)
    public String frequency;
}
