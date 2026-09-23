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
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
	}
}
