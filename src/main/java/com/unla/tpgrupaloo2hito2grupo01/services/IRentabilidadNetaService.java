package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaDTO;

public interface IRentabilidadNetaService {

	Optional<ReporteRentabilidadNetaDTO> getReporte(long festivalId, long unidadVentaId);
}
