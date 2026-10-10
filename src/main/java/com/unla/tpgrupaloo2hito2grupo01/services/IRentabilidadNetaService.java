package com.unla.tpgrupaloo2hito2grupo01.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaPeriodoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;

public interface IRentabilidadNetaService {

	Optional<ReporteRentabilidadNetaDTO> getReporte(long festivalId, long unidadVentaId);

	List<ErrorCampoDTO> validarPeriodo(Festival festival, LocalDate desde, LocalDate hasta);

	Optional<ReporteRentabilidadNetaPeriodoDTO> getReportePeriodo(Festival festival, long unidadVentaId,
			LocalDate desde, LocalDate hasta);

	// Rentabilidad de la unidad del Responsable que inició sesión ("username") en el festival al que asiste;
	// vacío si todavía no tiene unidad o la unidad no tiene festival
	Optional<ReporteRentabilidadNetaDTO> getReporteDeUnidadDelResponsable(String username);
}
