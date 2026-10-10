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
}
