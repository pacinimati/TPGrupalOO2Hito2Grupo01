package com.unla.tpgrupaloo2hito2grupo01.services;

import com.unla.tpgrupaloo2hito2grupo01.dtos.RegistroUnidadDTO;

public interface IUnidadVentaService {

	boolean existeUsuario(String usuario);

	boolean existeCodigoUnico(String codigoUnico);

	// Crea la unidad de venta, su responsable y el usuario (rol RESPONSABLE) con el que inicia sesión
	void registrarConResponsable(RegistroUnidadDTO dto);
}
