package com.unla.tpgrupaloo2hito2grupo01.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cierre_caja", uniqueConstraints = @UniqueConstraint(
		name = "uk_cierre_caja_unidad_fecha", columnNames = { "unidad_venta_id", "fecha" }))
@Getter @Setter @NoArgsConstructor
public class CierreCaja {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private LocalDate fecha;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal montoDeclarado;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal recaudacionPedidos;

	@CreationTimestamp
	private LocalDateTime fechaCierre;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cajero_id", nullable = false)
	private Cajero cajero;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "unidad_venta_id", nullable = false)
	private UnidadVenta unidadVenta;
}
