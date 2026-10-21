package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;
import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.HistorialLaboralDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReciboSueldoDTO;

// Información laboral del empleado (Cocinero o Cajero) que inició sesión ("username")
public interface IHistorialLaboralService {

	// Festivales y unidades de venta en las que trabajó el empleado, con el sueldo de cada festival
	List<HistorialLaboralDTO> getHistorialLaboral(String username);

	// Recibo de sueldo del empleado en un festival en el que trabajó; vacío si no trabajó en ese festival
	// o si el sueldo no se puede calcular
	Optional<ReciboSueldoDTO> getReciboSueldo(String username, long festivalId);
}
