package com.unla.tpgrupaloo2hito2grupo01.dtos;

import com.unla.tpgrupaloo2hito2grupo01.entities.TipoUnidad;

import lombok.Getter;
import lombok.Setter;

// Una posición del ranking de unidades de un festival (no persiste).
// La recaudación es la suma de los totales de los pedidos de la unidad.
// Los constructores van escritos a mano: los que genera el Lombok de la extensión
// de Java de VS Code salen rotos ("Flexible Constructor Bodies" / ClassFormatError).
@Getter @Setter
public class RankingUnidadDTO {

	private long id;

	private String nombreComercial;

	private String codigoUnico;

	private TipoUnidad tipo;

	private String responsable;

	private int cantidadPedidos;

	private double recaudacion;

	public RankingUnidadDTO() {
	}

	public RankingUnidadDTO(long id, String nombreComercial, String codigoUnico, TipoUnidad tipo,
			String responsable, int cantidadPedidos, double recaudacion) {
		this.id = id;
		this.nombreComercial = nombreComercial;
		this.codigoUnico = codigoUnico;
		this.tipo = tipo;
		this.responsable = responsable;
		this.cantidadPedidos = cantidadPedidos;
		this.recaudacion = recaudacion;
	}
}
