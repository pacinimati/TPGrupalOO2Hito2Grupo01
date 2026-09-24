package com.unla.tpgrupaloo2hito2grupo01.dtos;

import com.unla.tpgrupaloo2hito2grupo01.entities.TipoUnidad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una posición del ranking de unidades de un festival (no persiste).
// La recaudación es la suma de los totales de los pedidos de la unidad.
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RankingUnidadDTO {

	private long id;

	private String nombreComercial;

	private String codigoUnico;

	private TipoUnidad tipo;

	private String responsable;

	private int cantidadPedidos;

	private double recaudacion;
}
