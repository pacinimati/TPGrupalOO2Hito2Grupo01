package com.unla.tpgrupaloo2hito2grupo01.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UnidadVenta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String nombreComercial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "responsable_id")
	private Responsable responsable;

	private float superficieM2;

	@Column(length = 10, unique = true, nullable = false)
	private String codigoUnico;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "festival_id")
	private Festival festivalAsistio;

	@ManyToMany
	@JoinTable(name = "unidad_venta_plato",
		joinColumns = @JoinColumn(name = "unidad_venta_id"),
		inverseJoinColumns = @JoinColumn(name = "plato_id"))
	private List<Plato> lstPlatos = new ArrayList<>();

	@OneToMany(mappedBy = "unidadVenta")
	private List<Personal> lstPersonal = new ArrayList<>();
}
