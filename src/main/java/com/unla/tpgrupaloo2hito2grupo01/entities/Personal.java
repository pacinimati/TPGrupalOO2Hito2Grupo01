package com.unla.tpgrupaloo2hito2grupo01.entities;

import java.time.LocalDate;
import java.time.Period;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Personal {

	private static final int EDAD_MINIMA = 18;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String nombre;

	private String apellido;

	private int dni;

	private LocalDate fechaNacimiento;

	private LocalDate fechaIngreso;

	private float sueldoBase;

	// Baja lógica: el personal nunca se borra de la base de datos
	private boolean eliminado = false;

	// Nullable porque el Responsable se crea antes que su unidad de venta
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "unidad_venta_id", nullable = true)
	private UnidadVenta unidadVenta;

	@PrePersist
	@PreUpdate
	private void validarMayoriaDeEdad() {
		if (fechaNacimiento != null && Period.between(fechaNacimiento, LocalDate.now()).getYears() < EDAD_MINIMA) {
			throw new IllegalStateException("El personal debe ser mayor de edad");
		}
	}
}
