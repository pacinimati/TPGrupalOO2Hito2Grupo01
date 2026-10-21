package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.PlatoDTO;

public interface IPlatoEstrellaService {

	// Plato que aparece en más pedidos de la unidad en el festival; vacío si la unidad no tiene pedidos
	Optional<PlatoDTO> getPlatoEstrella(long festivalId, long unidadVentaId);

	// Plato estrella de la unidad del Responsable que inició sesión ("username") en el festival al que asiste;
	// vacío si todavía no tiene unidad, la unidad no tiene festival o no tiene pedidos
	Optional<PlatoDTO> getPlatoEstrellaDeUnidadDelResponsable(String username);
}
