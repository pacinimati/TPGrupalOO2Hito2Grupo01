package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CierreCajaFormDTO {

	@NotNull(message = "Ingresá el monto de efectivo contado")
	@DecimalMin(value = "0.00", message = "El monto contado no puede ser negativo")
	@Digits(integer = 10, fraction = 2, message = "Ingresá un monto válido con hasta dos decimales")
	private BigDecimal montoDeclarado;
}
