package com.unla.tpgrupaloo2hito2grupo01.controllers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Expone el usuario autenticado (o null si es anónimo) y si es Administrador a todas las vistas, para el navbar
@ControllerAdvice
public class GlobalModelAdvice {

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final String ROLE_RESPONSABLE = "ROLE_RESPONSABLE";
	private static final String ROLE_EMPLEADO = "ROLE_EMPLEADO";

	@ModelAttribute("username")
	public String username() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken
				|| !authentication.isAuthenticated()) {
			return null;
		}
		return authentication.getName();
	}

	@ModelAttribute("esAdmin")
	public boolean esAdmin() {
		return tieneRol(ROLE_ADMIN);
	}

	@ModelAttribute("esResponsable")
	public boolean esResponsable() {
		return tieneRol(ROLE_RESPONSABLE);
	}

	@ModelAttribute("esEmpleado")
	public boolean esEmpleado() {
		return tieneRol(ROLE_EMPLEADO);
	}

	private boolean tieneRol(String rol) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> rol.equals(a.getAuthority()));
	}
}
