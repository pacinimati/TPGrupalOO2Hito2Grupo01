package com.unla.tpgrupaloo2hito2grupo01.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Cajero extends Personal {

	@Enumerated(EnumType.STRING)
	private TurnoTrabajo turnoTrabajo;
}
