package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.entities.TurnoTrabajo;

import io.swagger.v3.oas.annotations.media.Schema;
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

	// Null en el alta. La API REST solo da de alta, así que no se recibe por JSON
	@JsonIgnore
	private Long id;

	@NotNull(message = "Seleccione el tipo de empleado")
	@Schema(example = "COCINERO")
	private TipoEmpleado tipo;

	@NotBlank(message = "El nombre es obligatorio")
	@Schema(example = "Juan")
	private String nombre;

	@NotBlank(message = "El apellido es obligatorio")
	@Schema(example = "Pérez")
	private String apellido;

	@Positive(message = "El DNI es obligatorio")
	@Schema(example = "40123456")
	private int dni;

	@NotNull(message = "La fecha de nacimiento es obligatoria")
	@Past(message = "La fecha de nacimiento debe ser pasada")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@Schema(example = "1995-05-20")
	private LocalDate fechaNacimiento;

	@NotNull(message = "La fecha de ingreso es obligatoria")
	@PastOrPresent(message = "La fecha de ingreso no puede ser futura")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@Schema(example = "2026-01-10")
	private LocalDate fechaIngreso;

	// Es el usuario con el que inicia sesión; a esta casilla se envía la contraseña generada
	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email no es válido")
	@Size(max = 45, message = "El email admite hasta 45 caracteres")
	@Schema(example = "juan.perez@mail.com")
	private String email;

	// Cocina
	@Schema(description = "Obligatoria si tipo es COCINERO", example = "COCINERO_AYUDANTE")
	private CategoriaCocina categoriaCocina;

	@Size(max = 100, message = "La tarea admite hasta 100 caracteres")
	@Schema(example = "Parrilla")
	private String especialidadCulinaria;

	// Cajero
	@Schema(description = "Obligatorio si tipo es CAJERO", example = "MANIANA")
	private TurnoTrabajo turnoTrabajo;
}
