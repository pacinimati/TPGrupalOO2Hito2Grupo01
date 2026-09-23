package com.unla.tpgrupaloo2hito2grupo01.controllers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Expone el usuario autenticado (o null si es anónimo) a todas las vistas, para el navbar
@ControllerAdvice
public class GlobalModelAdvice {

	@ModelAttribute("username")
	public String username() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken
				|| !authentication.isAuthenticated()) {
			return null;
		}
		return authentication.getName();
	}
}
