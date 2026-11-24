package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;

import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaFiltroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaRegistroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EstadoCajaDTO;

public interface ICierreCajaService {

	EstadoCajaDTO getEstadoActual(String username);

	List<ErrorCampoDTO> validarCierre(CierreCajaFormDTO form, String username);

	CierreCajaDTO cerrarCaja(CierreCajaFormDTO form, String username);

	// --- API REST (Hito 3): el cajero y la jornada se indican en el pedido, sin un usuario que haya iniciado sesión ---

	// True si existe un cajero activo (no eliminado) con ese id
	boolean existeCajero(long cajeroId);

	// Reglas de negocio del cierre de la jornada indicada: el cajero tiene unidad con festival, la fecha está dentro
	// del festival y la unidad todavía no cerró esa fecha
	List<ErrorCampoDTO> validarRegistro(CierreCajaRegistroDTO registro);

	// Registra el cierre de la jornada con la recaudación de los pedidos de ese día (IllegalArgumentException si no
	// cumple las reglas de validarRegistro)
	CierreCajaDTO registrarCierre(CierreCajaRegistroDTO registro);

	// Errores de los filtros de búsqueda (rango de fechas invertido)
	List<ErrorCampoDTO> validarFiltro(CierreCajaFiltroDTO filtro);

	// Cierres que cumplen los filtros (todos opcionales), del más reciente al más antiguo
	List<CierreCajaDTO> filtrar(CierreCajaFiltroDTO filtro);
}
