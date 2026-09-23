package com.unla.tpgrupaloo2hito2grupo01.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Administrador extends Personal {

	@Column(unique = true, nullable = false)
	private String usuario;

	// Se guarda hasheada (BCrypt)
	@Column(nullable = false)
	private String contrasenia;
}
