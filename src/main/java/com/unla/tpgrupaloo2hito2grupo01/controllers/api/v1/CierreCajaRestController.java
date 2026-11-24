package com.unla.tpgrupaloo2hito2grupo01.controllers.api.v1;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaFiltroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaRegistroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.services.ICierreCajaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Hito 3 - Alejo Almirón: búsqueda y registro de cierres de caja diarios.
// Reutiliza la lógica del cierre de caja (CU12) indicando el cajero y la jornada en lugar del Cajero logueado
@RestController
@RequestMapping("/api/v1/cierres-caja")
@Tag(name = "Cierres de caja", description = "Búsqueda y registro de los cierres de caja diarios de las unidades de venta")
public class CierreCajaRestController {

	private final ICierreCajaService cierreCajaService;

	public CierreCajaRestController(ICierreCajaService cierreCajaService) {
		this.cierreCajaService = cierreCajaService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Buscar cierres de caja",
			description = "Devuelve los cierres de caja, del más reciente al más antiguo. Todos los filtros son "
					+ "opcionales y se combinan; el rango de fechas es inclusivo.")
	@ApiResponse(responseCode = "200", description = "Cierres que cumplen los filtros (puede ser una lista vacía)")
	@ApiResponse(responseCode = "400", description = "Filtros inválidos (ej. fechaHasta anterior a fechaDesde)",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = ErrorCampoDTO.class))))
	public ResponseEntity<?> buscar(
			@Parameter(description = "Id del festival", example = "6") @RequestParam(required = false) Long festivalId,
			@Parameter(description = "Id de la unidad de venta") @RequestParam(required = false) Long unidadVentaId,
			@Parameter(description = "Id del cajero que cerró la caja") @RequestParam(required = false) Long cajeroId,
			@Parameter(description = "Desde esta jornada (aaaa-mm-dd)", example = "2026-10-01")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
			@Parameter(description = "Hasta esta jornada (aaaa-mm-dd)", example = "2026-10-31")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
			@Parameter(description = "true: solo los que no cuadran (faltante o sobrante); false: solo los que cuadran")
			@RequestParam(required = false) Boolean conDiferencia) {
		CierreCajaFiltroDTO filtro = new CierreCajaFiltroDTO();
		filtro.setFestivalId(festivalId);
		filtro.setUnidadVentaId(unidadVentaId);
		filtro.setCajeroId(cajeroId);
		filtro.setFechaDesde(fechaDesde);
		filtro.setFechaHasta(fechaHasta);
		filtro.setConDiferencia(conDiferencia);
		List<ErrorCampoDTO> errores = cierreCajaService.validarFiltro(filtro);
		if (!errores.isEmpty()) {
			return ResponseEntity.badRequest().body(errores);
		}
		return ResponseEntity.ok(cierreCajaService.filtrar(filtro));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Registrar el cierre de caja de una jornada",
			description = "El cajero declara el efectivo contado de una jornada. La recaudación se calcula con los "
					+ "pedidos de su unidad de ese día y la diferencia es efectivo - recaudación. La fecha tiene que "
					+ "estar dentro del festival de la unidad y no ser futura; solo se admite un cierre por unidad y fecha.")
	@ApiResponse(responseCode = "201", description = "Cierre registrado",
			content = @Content(schema = @Schema(implementation = CierreCajaDTO.class)))
	@ApiResponse(responseCode = "400", description = "Datos inválidos o reglas de negocio no cumplidas",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = ErrorCampoDTO.class))))
	@ApiResponse(responseCode = "404", description = "No existe un cajero activo con ese id", content = @Content)
	public ResponseEntity<?> registrar(@Valid @RequestBody CierreCajaRegistroDTO cierre) {
		if (!cierreCajaService.existeCajero(cierre.getCajeroId())) {
			return ResponseEntity.notFound().build();
		}
		List<ErrorCampoDTO> errores = cierreCajaService.validarRegistro(cierre);
		if (!errores.isEmpty()) {
			return ResponseEntity.badRequest().body(errores);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(cierreCajaService.registrarCierre(cierre));
	}
}
