package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PeriodoRentabilidadNetaFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaOpcionDTO;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IRentabilidadNetaService;

import jakarta.validation.Valid;

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

	@GetMapping("/periodo")
	public ModelAndView formularioRentabilidadNetaPeriodo(Principal principal) {
		return vistaPeriodo(principal.getName(), new PeriodoRentabilidadNetaFormDTO());
	}

	@PostMapping("/periodo")
	public ModelAndView calcularRentabilidadNetaPeriodo(Principal principal,
			@Valid @ModelAttribute("periodo") PeriodoRentabilidadNetaFormDTO periodo, BindingResult result) {
		String username = principal.getName();
		UnidadVentaOpcionDTO unidad = empleadoService.getUnidadDelResponsable(username).orElse(null);
		ModelAndView mAV = vistaPeriodo(unidad, periodo);
		if (result.hasErrors()) {
			return mAV;
		}

		List<ErrorCampoDTO> errores = rentabilidadNetaService
				.validarPeriodoDeUnidadDelResponsable(username, periodo.getDesde(), periodo.getHasta())
				.orElse(null);
		if (errores == null) {
			mAV.addObject("sinUnidadOFestival", true);
			return mAV;
		}
		for (ErrorCampoDTO error : errores) {
			if (!result.hasFieldErrors(error.campo())) {
				result.rejectValue(error.campo(), error.codigo(), error.mensaje());
			}
		}
		if (result.hasErrors()) {
			return mAV;
		}

		mAV.addObject("reporte", rentabilidadNetaService
				.getReportePeriodoDeUnidadDelResponsable(username, periodo.getDesde(), periodo.getHasta())
				.orElse(null));
		return mAV;
	}

	private ModelAndView vistaPeriodo(String username, PeriodoRentabilidadNetaFormDTO periodo) {
		return vistaPeriodo(empleadoService.getUnidadDelResponsable(username).orElse(null), periodo);
	}

	private ModelAndView vistaPeriodo(UnidadVentaOpcionDTO unidad, PeriodoRentabilidadNetaFormDTO periodo) {
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_RENTABILIDAD_NETA_PERIODO);
		mAV.addObject("unidad", unidad);
		mAV.addObject("unidadConFestival", unidad != null && !"-".equals(unidad.festival()));
		mAV.addObject("sinUnidadOFestival", unidad == null || "-".equals(unidad.festival()));
		mAV.addObject("periodo", periodo);
		return mAV;
	}
}
