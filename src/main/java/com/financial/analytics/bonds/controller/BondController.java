
package com.financial.analytics.bonds.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financial.analytics.bonds.domain.Bond;
import com.financial.analytics.bonds.domain.Portfolio;
import com.financial.analytics.bonds.dto.BondDTO;
import com.financial.analytics.bonds.dto.DurationResponse;
import com.financial.analytics.bonds.dto.ModifiedDurationResponse;
import com.financial.analytics.bonds.dto.PortfolioDurationResponse;
import com.financial.analytics.bonds.dto.YtmResponse;
import com.financial.analytics.bonds.mapper.BondMapper;
import com.financial.analytics.bonds.service.BondRiskService;
import com.financial.analytics.bonds.service.PortfolioRiskService;
import com.financial.analytics.bonds.util.NumberUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bonds")
public class BondController {

	@Autowired
	private BondRiskService bondRiskService;

	@Autowired
	private PortfolioRiskService portfolioRiskService;

	@Autowired
	private BondMapper bondMapper;

	@PostMapping("/ytm")
	public ResponseEntity<YtmResponse> calculateYtm(@Valid @RequestBody BondDTO dto) {
		Bond bond = bondMapper.toDomain(dto);
		double ytm = bondRiskService.calculateYTM(bond);
		double rounded = NumberUtils.round(ytm, 4);
		YtmResponse res = new YtmResponse(dto.isin, rounded);
		return ResponseEntity.ok(res);
	}

	@PostMapping("/duration")
	public ResponseEntity<DurationResponse> calculateDuration(@Valid @RequestBody BondDTO dto) {
		Bond bond = bondMapper.toDomain(dto);
		double duration = bondRiskService.calculateDuration(bond);
		double rounded = NumberUtils.round(duration, 4);
		DurationResponse res = new DurationResponse(dto.isin, rounded);
		return ResponseEntity.ok(res);
	}

	@PostMapping("/modified-duration")
	public ResponseEntity<ModifiedDurationResponse> calculateModifiedDuration(@Valid @RequestBody BondDTO dto) {
		Bond bond = bondMapper.toDomain(dto);
		double md = bondRiskService.calculateModifiedDuration(bond);
		double rounded = NumberUtils.round(md, 4);
		ModifiedDurationResponse res = new ModifiedDurationResponse(dto.isin, rounded);
		return ResponseEntity.ok(res);
	}

	@PostMapping("/portfolio/duration")
	public ResponseEntity<PortfolioDurationResponse> calculatePortfolioDuration(@RequestBody List<BondDTO> dtos) {
		List<Bond> bonds = dtos.stream().map(bondMapper::toDomain).collect(Collectors.toList());
		Portfolio p = new Portfolio(bonds);
		double val = portfolioRiskService.weightedAverageDuration(p);
		double rounded = NumberUtils.round(val, 4);
		PortfolioDurationResponse res = new PortfolioDurationResponse(rounded);
		return ResponseEntity.ok(res);
	}

	// Accept raw JSON string containing array of bond objects and return loaded
	// portfolio
	@PostMapping("/portfolio/load")
	public ResponseEntity<?> loadPortfolio(@RequestBody String json) {
		try {
			Portfolio p = portfolioRiskService.loadPortfolioFromJson(json);
			return ResponseEntity.ok(p);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON: " + e.getMessage());
		}
	}
}
