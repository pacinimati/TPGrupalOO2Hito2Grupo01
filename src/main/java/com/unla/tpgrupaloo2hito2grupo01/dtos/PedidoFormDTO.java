package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Formulario de alta de un pedido: la unidad que lo entrega, la fecha y cuántas unidades se piden de cada plato
@Getter @Setter @NoArgsConstructor
public class PedidoFormDTO {

	@NotNull(message = "Seleccione la unidad de venta")
	private Long unidadVentaId;

	@NotNull(message = "La fecha de la transacción es obligatoria")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fechaTransaccion;

	// Id del plato -> cantidad pedida. Los platos vacíos o en 0 no forman parte del pedido
	private Map<Long, Integer> cantidades = new HashMap<>();
}
