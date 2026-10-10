package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IRentabilidadNetaService;

// Rentabilidad neta de la unidad de venta del Responsable en el festival al que asiste
@Controller
@PreAuthorize("hasRole('ROLE_RESPONSABLE')")
@RequestMapping("/responsable/rentabilidad")
public class RentabilidadNetaController {

	private final IEmpleadoService empleadoService;
	private final IRentabilidadNetaService rentabilidadNetaService;

	public RentabilidadNetaController(IEmpleadoService empleadoService,
			IRentabilidadNetaService rentabilidadNetaService) {
		this.empleadoService = empleadoService;
		this.rentabilidadNetaService = rentabilidadNetaService;
	}

	//GET: SERVER/responsable/rentabilidad
	@GetMapping
	public ModelAndView rentabilidadNeta(Principal principal) {
		String username = principal.getName();
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_RENTABILIDAD_NETA);
		mAV.addObject("unidad", empleadoService.getUnidadDelResponsable(username).orElse(null));
		mAV.addObject("reporte", rentabilidadNetaService.getReporteDeUnidadDelResponsable(username).orElse(null));
		return mAV;
	}
}
