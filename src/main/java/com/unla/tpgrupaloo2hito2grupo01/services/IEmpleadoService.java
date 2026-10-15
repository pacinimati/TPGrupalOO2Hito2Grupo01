package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;
import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFiltroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.SituacionLaboralActualDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaOpcionDTO;

// ABM de los empleados de la unidad de venta del Responsable que inició sesión ("username").
// Un Responsable solo ve y modifica los empleados de su propia unidad
public interface IEmpleadoService {

	// Unidad asignada al responsable; vacío si todavía no tiene una
	Optional<UnidadVentaOpcionDTO> getUnidadDelResponsable(String username);

	// Situación laboral del empleado autenticado según las fechas del festival de su unidad
	SituacionLaboralActualDTO getSituacionLaboralActual(String username);

	// Empleados de la unidad según los filtros (todos opcionales): activos, o dados de baja si filtro.eliminados
	List<EmpleadoDTO> filtrar(String username, EmpleadoFiltroDTO filtro);

	// Datos de un empleado activo de la unidad para precargar el formulario de modificación; vacío si no existe
	Optional<EmpleadoFormDTO> getParaModificar(String username, long id);

	// Reglas de negocio del alta (dto.id nulo) o de la modificación: dni y email sin duplicar, mayoría de edad,
	// ingreso posterior al nacimiento y datos propios del tipo de empleado. Lista vacía si es válido
	List<ErrorCampoDTO> validarReglasDeNegocio(EmpleadoFormDTO dto, String username);

	// Crea el usuario (rol EMPLEADO, con contraseña aleatoria) y el empleado en la unidad, y le envía un mail con
	// sus credenciales. Si el mail falla (EnvioMailException) no se guarda nada
	void altaEmpleado(EmpleadoFormDTO dto, String username);

	// Actualiza los datos de un empleado activo de la unidad; false si no existe o es de otra unidad
	boolean modificarEmpleado(EmpleadoFormDTO dto, String username);

	// Baja lógica: el empleado se conserva marcado como eliminado y pierde el acceso al sistema.
	// False si no existe, es de otra unidad o ya estaba eliminado
	boolean eliminarEmpleado(long id, String username);

	// Alta de un empleado eliminado lógicamente: lo reactiva, le genera una contraseña nueva y se la envía por mail
	// (EnvioMailException si falla y no se reactiva). False si no existe, es de otra unidad o no estaba eliminado
	boolean reactivarEmpleado(long id, String username);
}
