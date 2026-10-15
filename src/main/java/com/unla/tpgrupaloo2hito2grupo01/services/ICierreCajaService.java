package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;

import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EstadoCajaDTO;

public interface ICierreCajaService {

	EstadoCajaDTO getEstadoActual(String username);

	List<ErrorCampoDTO> validarCierre(CierreCajaFormDTO form, String username);

	CierreCajaDTO cerrarCaja(CierreCajaFormDTO form, String username);
}
