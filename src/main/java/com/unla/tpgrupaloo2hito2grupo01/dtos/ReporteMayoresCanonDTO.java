package com.unla.tpgrupaloo2hito2grupo01.dtos;

import com.unla.tpgrupaloo2hito2grupo01.entities.TipoUnidad;

import lombok.Getter;
import lombok.Setter;

// Una unidad del reporte de mayores canon de un festival (no persiste)
// Los constructores van escritos a mano: los que genera el Lombok de la extensión
// de Java de VS Code salen rotos ("Flexible Constructor Bodies" / ClassFormatError).
@Getter @Setter
public class ReporteMayoresCanonDTO {

	private String nombreComercial;

	private String codigoUnico;

	private TipoUnidad tipo;

	private double canon;

	public ReporteMayoresCanonDTO() {
	}

	public ReporteMayoresCanonDTO(String nombreComercial, String codigoUnico, TipoUnidad tipo, double canon) {
		this.nombreComercial = nombreComercial;
		this.codigoUnico = codigoUnico;
		this.tipo = tipo;
		this.canon = canon;
	}
}
