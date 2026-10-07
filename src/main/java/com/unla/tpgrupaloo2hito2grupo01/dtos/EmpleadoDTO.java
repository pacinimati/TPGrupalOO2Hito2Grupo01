package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;

// Fila del listado de empleados de una unidad de venta (no persiste)
public record EmpleadoDTO(long id, String nombre, String apellido, int dni, String email, TipoEmpleado tipo,
		String detalle, LocalDate fechaIngreso) {
}
