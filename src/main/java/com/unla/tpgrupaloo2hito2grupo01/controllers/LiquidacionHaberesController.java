package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;
import com.unla.tpgrupaloo2hito2grupo01.services.ILiquidacionHaberesService;

@Controller
@PreAuthorize("hasRole('ROLE_RESPONSABLE')")
@RequestMapping("/responsable/liquidaciones")
public class LiquidacionHaberesController {

	private final IEmpleadoService empleadoService;
	private final ILiquidacionHaberesService liquidacionHaberesService;

	public LiquidacionHaberesController(IEmpleadoService empleadoService,
			ILiquidacionHaberesService liquidacionHaberesService) {
		this.empleadoService = empleadoService;
		this.liquidacionHaberesService = liquidacionHaberesService;
	}

	@GetMapping
	public ModelAndView listado(Principal principal) {
		String username = principal.getName();
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_LIQUIDACIONES);
		modelAndView.addObject("unidad", empleadoService.getUnidadDelResponsable(username).orElse(null));
		modelAndView.addObject("liquidaciones",
				liquidacionHaberesService.getLiquidacionesDeUnidadDelResponsable(username));
		return modelAndView;
	}
}
