package com.unla.tpgrupaloo2hito2grupo01.controllers.api.v1;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFiltroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Hito 3 - Matías Pacini: consulta y alta de empleados (Cocinero / Cajero) de una unidad de venta.
// Reutiliza la lógica del ABM de empleados (CU6) indicando la unidad por id en lugar del Responsable logueado
@RestController
@RequestMapping("/api/v1/empleados")
@Tag(name = "Empleados", description = "Consulta y alta de empleados (Cocinero / Cajero) de una unidad de venta")
public class EmpleadoRestController {

	private final IEmpleadoService empleadoService;

	public EmpleadoRestController(IEmpleadoService empleadoService) {
		this.empleadoService = empleadoService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Buscar empleados de una unidad de venta",
			description = "Devuelve los empleados de la unidad indicada. Los filtros son opcionales y se combinan; "
					+ "nombre y apellido buscan por coincidencia parcial, sin distinguir mayúsculas.")
	@ApiResponse(responseCode = "200", description = "Empleados que cumplen los filtros (puede ser una lista vacía)")
	@ApiResponse(responseCode = "404", description = "No existe la unidad de venta", content = @Content)
	public ResponseEntity<List<EmpleadoDTO>> buscar(
			@Parameter(description = "Id de la unidad de venta", example = "1") @RequestParam long unidadVentaId,
			@Parameter(description = "Nombre o parte del nombre") @RequestParam(required = false) String nombre,
			@Parameter(description = "Apellido o parte del apellido") @RequestParam(required = false) String apellido,
			@Parameter(description = "DNI exacto") @RequestParam(required = false) Integer dni,
			@Parameter(description = "Tipo de empleado") @RequestParam(required = false) TipoEmpleado tipo,
			@Parameter(description = "true: solo los dados de baja; false: solo los activos")
			@RequestParam(defaultValue = "false") boolean eliminados) {
		if (empleadoService.getUnidad(unidadVentaId).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		EmpleadoFiltroDTO filtro = new EmpleadoFiltroDTO();
		filtro.setNombre(nombre);
		filtro.setApellido(apellido);
		filtro.setDni(dni);
		filtro.setTipo(tipo);
		filtro.setEliminados(eliminados);
		return ResponseEntity.ok(empleadoService.filtrarPorUnidad(unidadVentaId, filtro));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Dar de alta un empleado en una unidad de venta",
			description = "Crea el empleado y su usuario (el email, con rol EMPLEADO) y le envía por mail una "
					+ "contraseña aleatoria. Si es COCINERO requiere categoriaCocina; si es CAJERO, turnoTrabajo. "
					+ "Si el mail no se puede enviar, no se guarda nada.")
	@ApiResponse(responseCode = "201", description = "Empleado creado",
			content = @Content(schema = @Schema(implementation = EmpleadoDTO.class)))
	@ApiResponse(responseCode = "400", description = "Datos inválidos o reglas de negocio no cumplidas",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = ErrorCampoDTO.class))))
	@ApiResponse(responseCode = "404", description = "No existe la unidad de venta", content = @Content)
	@ApiResponse(responseCode = "502", description = "No se pudo enviar el mail con las credenciales",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = ErrorCampoDTO.class))))
	public ResponseEntity<?> alta(
			@Parameter(description = "Id de la unidad de venta", example = "1") @RequestParam long unidadVentaId,
			@Valid @RequestBody EmpleadoFormDTO empleado) {
		if (empleadoService.getUnidad(unidadVentaId).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		List<ErrorCampoDTO> errores = empleadoService.validarAltaEnUnidad(empleado, unidadVentaId);
		if (!errores.isEmpty()) {
			return ResponseEntity.badRequest().body(errores);
		}
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(empleadoService.altaEmpleadoEnUnidad(empleado, unidadVentaId));
	}
}
