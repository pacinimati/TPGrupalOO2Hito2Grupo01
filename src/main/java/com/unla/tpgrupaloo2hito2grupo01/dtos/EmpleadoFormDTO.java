package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.entities.TurnoTrabajo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Formulario de alta y modificación de un empleado. En la modificación el email (usuario) no cambia
@Getter @Setter @NoArgsConstructor
public class EmpleadoFormDTO {

	// Null en el alta
	private Long id;

	@NotNull(message = "Seleccione el tipo de empleado")
	private TipoEmpleado tipo;

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

	@NotNull(message = "La fecha de ingreso es obligatoria")
	@PastOrPresent(message = "La fecha de ingreso no puede ser futura")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fechaIngreso;

	// Es el usuario con el que inicia sesión; a esta casilla se envía la contraseña generada
	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email no es válido")
	@Size(max = 45, message = "El email admite hasta 45 caracteres")
	private String email;

	// Cocina
	private CategoriaCocina categoriaCocina;

	@Size(max = 100, message = "La tarea admite hasta 100 caracteres")
	private String especialidadCulinaria;

	// Cajero
	private TurnoTrabajo turnoTrabajo;
}
