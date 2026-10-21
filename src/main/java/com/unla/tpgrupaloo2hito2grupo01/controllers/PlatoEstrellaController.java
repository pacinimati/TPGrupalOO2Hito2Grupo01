package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IPlatoEstrellaService;

// Plato estrella de la unidad de venta del Responsable en el festival al que asiste
@Controller
@PreAuthorize("hasRole('ROLE_RESPONSABLE')")
@RequestMapping("/responsable/plato-estrella")
public class PlatoEstrellaController {

	private final IEmpleadoService empleadoService;
	private final IPlatoEstrellaService platoEstrellaService;

	public PlatoEstrellaController(IEmpleadoService empleadoService, IPlatoEstrellaService platoEstrellaService) {
		this.empleadoService = empleadoService;
		this.platoEstrellaService = platoEstrellaService;
	}

	//GET: SERVER/responsable/plato-estrella
	@GetMapping
	public ModelAndView platoEstrella(Principal principal) {
		String username = principal.getName();
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_PLATO_ESTRELLA);
		mAV.addObject("unidad", empleadoService.getUnidadDelResponsable(username).orElse(null));
		mAV.addObject("plato", platoEstrellaService.getPlatoEstrellaDeUnidadDelResponsable(username).orElse(null));
		return mAV;
	}
}
