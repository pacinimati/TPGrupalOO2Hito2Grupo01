package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Filtros opcionales de la búsqueda de cierres de caja; los que vienen vacíos no filtran (no persiste)
@Getter @Setter @NoArgsConstructor
public class CierreCajaFiltroDTO {

	private Long festivalId;

	private Long unidadVentaId;

	private Long cajeroId;

	private LocalDate fechaDesde;

	private LocalDate fechaHasta;

	// true: solo los que no cuadran (faltante o sobrante); false: solo los que cuadran
	private Boolean conDiferencia;
}
