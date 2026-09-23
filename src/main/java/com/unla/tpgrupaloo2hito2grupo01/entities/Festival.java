package com.unla.tpgrupaloo2hito2grupo01.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Festival {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String nombre;

	private String temporada;

	private LocalDate fechaInicio;

	private LocalDate fechaFin;

	private float costoPorSuperficie;

	private float costoPorMontaje;

	private float plusUsoElectricidad;

	private float sueldoBase;

	@OneToMany(mappedBy = "festivalAsistio")
	private List<UnidadVenta> lstUnidadVenta = new ArrayList<>();
}
