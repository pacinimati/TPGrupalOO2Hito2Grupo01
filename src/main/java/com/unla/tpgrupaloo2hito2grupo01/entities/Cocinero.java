package com.unla.tpgrupaloo2hito2grupo01.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Cocinero extends Personal {

	private String especialidadCulinaria;

	// Plus fijo por categoría (cocinero, ayudante, lavaplatos)
	private float plus;
}
