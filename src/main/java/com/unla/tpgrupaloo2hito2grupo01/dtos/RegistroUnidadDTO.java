package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Formulario de registro de una Unidad de Venta junto con su Responsable
@Getter @Setter @NoArgsConstructor
public class RegistroUnidadDTO {

	// Datos de acceso del responsable
	@NotBlank(message = "El usuario es obligatorio")
	@Size(max = 45, message = "El usuario admite hasta 45 caracteres")
	private String usuario;

	@NotBlank(message = "La contraseña es obligatoria")
	@Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
	private String contrasenia;

	// Datos personales del responsable
	@NotBlank(message = "El nombre es obligatorio")
	private String nombre;

	@NotBlank(message = "El apellido es obligatorio")
	private String apellido;

	@Positive(message = "El DNI es obligatorio")
	private int dni;

	@NotNull(message = "La fecha de nacimiento es obligatoria")
	@Past(message = "La fecha de nacimiento debe ser pasada")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fechaNacimiento;

	// Datos de la unidad de venta
	@NotBlank(message = "El nombre comercial es obligatorio")
	private String nombreComercial;

	@Positive(message = "La superficie debe ser mayor a 0")
	private float superficieM2;

	// 3 letras mayúsculas seguidas de 7 dígitos (10 caracteres)
	@Pattern(regexp = "^[A-Z]{3}[0-9]{7}$", message = "El código debe tener 3 letras mayúsculas y 7 dígitos (ej: ABC1234567)")
	private String codigoUnico;

	@NotNull(message = "Seleccione un festival")
	private Long festivalId;

	@NotNull(message = "Seleccione el tipo de unidad")
	private TipoUnidad tipo;

	// Food Truck
	private String patente;

	private boolean conexionElectrica;

	// Puesto Desarmable
	private int carpasComponen;

	private int tiempoMontaje;
}
