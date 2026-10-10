package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaDTO;

public interface IRentabilidadNetaService {

	Optional<ReporteRentabilidadNetaDTO> getReporte(long festivalId, long unidadVentaId);

	// Rentabilidad de la unidad del Responsable que inició sesión ("username") en el festival al que asiste;
	// vacío si todavía no tiene unidad o la unidad no tiene festival
	Optional<ReporteRentabilidadNetaDTO> getReporteDeUnidadDelResponsable(String username);
}
