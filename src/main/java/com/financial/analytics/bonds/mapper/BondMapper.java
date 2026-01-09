package com.financial.analytics.bonds.mapper;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.dto.BondDTO;

@Component
public class BondMapper {

    public Bond toDomain(BondDTO dto) {
        LocalDate dt = LocalDate.parse(dto.maturityDate);
        Bond.Frequency freq = Bond.Frequency.ANNUAL;
        if (dto.frequency != null) {
            try {
                freq = Bond.Frequency.valueOf(dto.frequency);
            } catch (Exception e) {
                freq = Bond.Frequency.ANNUAL;
            }
        }

        List<java.time.LocalDate> couponDates = null;
        if (dto.couponDates != null) {
            couponDates = dto.couponDates.stream().map(java.time.LocalDate::parse).toList();
        }

        return new Bond(dto.isin, dt, dto.couponRate, dto.faceValue, dto.marketPrice, couponDates, freq);
    }

}
