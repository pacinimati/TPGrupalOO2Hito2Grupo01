package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.RankingUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.RegistroUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaDTO;

public interface IUnidadVentaService {

	// Reglas de negocio del registro (duplicados, mayoría de edad, datos según el tipo de unidad).
	// Devuelve todos los errores encontrados; lista vacía si el registro es válido
	List<ErrorCampoDTO> validarReglasDeNegocio(RegistroUnidadDTO dto);

	// Crea la unidad de venta, su responsable y el usuario (rol RESPONSABLE) con el que inicia sesión
	void registrarConResponsable(RegistroUnidadDTO dto);

	// Unidades de venta de un festival, ordenadas por nombre comercial
	List<UnidadVentaDTO> getByFestival(long festivalId);

	// Ranking de las unidades de un festival, de mayor a menor recaudación (suma de sus pedidos).
	// Incluye las unidades sin pedidos, con recaudación 0
	List<RankingUnidadDTO> getRankingPorRecaudacion(long festivalId);
}
