package com.unla.tpgrupaloo2hito2grupo01.dtos;

import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;

public record LiquidacionHaberesDTO(
		long empleadoId,
		String nombre,
		String apellido,
		TipoEmpleado tipo,
		CategoriaCocina categoriaCocina,
		Double sueldoCalculado,
		String motivoNoCalculable) {
}
