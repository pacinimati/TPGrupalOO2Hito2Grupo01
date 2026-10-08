package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PedidoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PedidoResumenDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteVentaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaOpcionDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;

public interface IPedidoService {

	// Platos que se pueden pedir, ordenados por nombre
	List<Plato> getPlatos();

	// Unidades en las que el usuario puede registrar pedidos: todas si es Administrador,
	// si no solo la unidad que tiene asignada (lista vacía si no tiene ninguna)
	List<UnidadVentaOpcionDTO> getUnidadesHabilitadas(String username);

	// Reglas de negocio del pedido (unidad habilitada, fecha dentro del festival, al menos un plato).
	// Devuelve todos los errores encontrados; lista vacía si el pedido es válido
	List<ErrorCampoDTO> validarReglasDeNegocio(PedidoFormDTO dto, String username);

	// Registra el pedido con un item por cada plato pedido
	PedidoResumenDTO registrar(PedidoFormDTO dto, String username);

	// Recaudación total del festival, sumando el total de sus pedidos
	ReporteVentaDTO getReporteRecaudacion(long festivalId);
}
