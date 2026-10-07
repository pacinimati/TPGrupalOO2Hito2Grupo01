package com.unla.tpgrupaloo2hito2grupo01.dtos;

import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Filtros opcionales del listado de empleados; los que vienen vacíos no filtran (no persiste)
@Getter @Setter @NoArgsConstructor
public class EmpleadoFiltroDTO {

	private String nombre;

	private String apellido;

	private Integer dni;

	private TipoEmpleado tipo;

	// false: empleados activos; true: los dados de baja lógica (candidatos a reactivar)
	private boolean eliminados;
}
