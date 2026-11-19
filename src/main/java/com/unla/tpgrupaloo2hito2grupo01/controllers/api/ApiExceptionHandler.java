package com.unla.tpgrupaloo2hito2grupo01.controllers.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.exceptions.EnvioMailException;

// Respuestas de error de la API REST con el mismo formato que las reglas de negocio (lista de ErrorCampoDTO).
// Solo aplica a los controllers REST de este paquete, no a las vistas Thymeleaf
@RestControllerAdvice(basePackages = "com.unla.tpgrupaloo2hito2grupo01.controllers.api")
public class ApiExceptionHandler {

	// Bean Validation del body (@Valid): un error por cada campo inválido
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<List<ErrorCampoDTO>> datosInvalidos(MethodArgumentNotValidException ex) {
		return ResponseEntity.badRequest().body(ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new ErrorCampoDTO(error.getField(), error.getCode(), error.getDefaultMessage()))
				.toList());
	}

	// JSON mal formado o con valores que no se pueden convertir (fecha inválida, tipo inexistente, etc.)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<List<ErrorCampoDTO>> jsonInvalido(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest().body(List.of(new ErrorCampoDTO("body", "formato",
				"El JSON enviado no es válido: revisá el formato de fechas (aaaa-mm-dd) y los valores de los enums")));
	}

	// Parámetro de la URL con un valor que no se puede convertir (ej. dni=abc, tipo=MOZO)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<List<ErrorCampoDTO>> parametroInvalido(MethodArgumentTypeMismatchException ex) {
		return ResponseEntity.badRequest().body(List.of(new ErrorCampoDTO(ex.getName(), "formato",
				"Valor inválido para el parámetro " + ex.getName() + ": " + ex.getValue())));
	}

	// El alta se revirtió porque no se pudo enviar el mail con las credenciales
	@ExceptionHandler(EnvioMailException.class)
	public ResponseEntity<List<ErrorCampoDTO>> errorMail(EnvioMailException ex) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(List.of(new ErrorCampoDTO("email", "envioMail",
				ex.getMessage())));
	}
}
