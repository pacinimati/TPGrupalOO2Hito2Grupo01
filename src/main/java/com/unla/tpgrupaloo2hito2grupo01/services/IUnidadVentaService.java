package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.RankingUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.RegistroUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteMayoresCanonDTO;
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

	// Regla de negocio del reporte de mayores canon: la cantidad pedida debe ser al menos 1.
	// Lista vacía si es válida
	List<ErrorCampoDTO> validarCantidadMayoresCanon(int cantidad);

	// Las "cantidad" unidades del festival que más pagan de canon, de mayor a menor.
	// El canon de cada unidad depende de su tipo y de los costos que define el festival
	List<ReporteMayoresCanonDTO> getUnidadesConMayorCanon(long festivalId, int cantidad);
}
