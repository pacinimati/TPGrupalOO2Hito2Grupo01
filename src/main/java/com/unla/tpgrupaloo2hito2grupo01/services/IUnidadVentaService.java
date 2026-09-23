package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;

import com.unla.tpgrupaloo2hito2grupo01.dtos.RegistroUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaDTO;

public interface IUnidadVentaService {

	boolean existeUsuario(String usuario);

	boolean existeCodigoUnico(String codigoUnico);

	// Crea la unidad de venta, su responsable y el usuario (rol RESPONSABLE) con el que inicia sesión
	void registrarConResponsable(RegistroUnidadDTO dto);

	// Unidades de venta de un festival, ordenadas por nombre comercial
	List<UnidadVentaDTO> getByFestival(long festivalId);
}
