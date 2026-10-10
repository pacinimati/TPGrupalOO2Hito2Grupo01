package com.unla.tpgrupaloo2hito2grupo01.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PeriodoRentabilidadNetaFormDTO {

	@NotNull(message = "La fecha inicial es obligatoria.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate desde;

	@NotNull(message = "La fecha final es obligatoria.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate hasta;
}
