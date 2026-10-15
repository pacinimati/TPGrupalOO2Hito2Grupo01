package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;

@Controller
@PreAuthorize("hasRole('ROLE_EMPLEADO')")
@RequestMapping("/empleado")
public class EmpleadoPortalController {

	private final IEmpleadoService empleadoService;

	public EmpleadoPortalController(IEmpleadoService empleadoService) {
		this.empleadoService = empleadoService;
	}

	@GetMapping("/actualidad")
	public ModelAndView actualidad(Principal principal) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_ACTUALIDAD);
		modelAndView.addObject("situacion", empleadoService.getSituacionLaboralActual(principal.getName()));
		return modelAndView;
	}
}
