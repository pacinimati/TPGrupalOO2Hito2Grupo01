package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cierre de caja enviado por la API REST: indica el cajero y la jornada, sin un usuario logueado (no persiste)
@Getter @Setter @NoArgsConstructor
public class CierreCajaRegistroDTO {

	@NotNull(message = "Indicá el id del cajero")
	@Schema(description = "Id del cajero que cierra la caja", example = "22")
	private Long cajeroId;

	@NotNull(message = "Indicá la fecha de la jornada")
	@PastOrPresent(message = "No se puede cerrar la caja de una fecha futura")
	@Schema(description = "Jornada que se cierra (aaaa-mm-dd), dentro de las fechas del festival", example = "2026-10-02")
	private LocalDate fecha;

	@NotNull(message = "Ingresá el monto de efectivo contado")
	@DecimalMin(value = "0.00", message = "El monto contado no puede ser negativo")
	@Digits(integer = 10, fraction = 2, message = "Ingresá un monto válido con hasta dos decimales")
	@Schema(description = "Efectivo contado al final de la jornada", example = "1280000.00")
	private BigDecimal montoDeclarado;
}
