package com.unla.tpgrupaloo2hito2grupo01.dtos;

import com.unla.tpgrupaloo2hito2grupo01.entities.TipoUnidad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una unidad del reporte de mayores canon de un festival (no persiste)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReporteMayoresCanonDTO {

	private String nombreComercial;

	private String codigoUnico;

	private TipoUnidad tipo;

	private double canon;
}
