package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.ICierreCajaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/empleado/caja")
@PreAuthorize("hasRole('ROLE_EMPLEADO')")
public class CierreCajaController {

	private final ICierreCajaService cierreCajaService;

	public CierreCajaController(ICierreCajaService cierreCajaService) {
		this.cierreCajaService = cierreCajaService;
	}

	@GetMapping
	public ModelAndView formulario(Principal principal) {
		return vista(new CierreCajaFormDTO(), principal.getName());
	}

	@PostMapping
	public ModelAndView cerrar(@Valid @ModelAttribute("form") CierreCajaFormDTO form, BindingResult result,
			Principal principal, RedirectAttributes redirectAttributes) {
		if (!result.hasErrors()) {
			for (ErrorCampoDTO error : cierreCajaService.validarCierre(form, principal.getName())) {
				if (!result.hasFieldErrors(error.campo())) {
					result.rejectValue(error.campo(), error.codigo(), error.mensaje());
				}
			}
		}
		if (result.hasErrors()) {
			return vista(form, principal.getName());
		}

		try {
			cierreCajaService.cerrarCaja(form, principal.getName());
		} catch (IllegalArgumentException e) {
			result.rejectValue("montoDeclarado", "cierreNoDisponible", e.getMessage());
			return vista(form, principal.getName());
		}
		redirectAttributes.addFlashAttribute("cierreRegistrado", true);
		return new ModelAndView("redirect:" + ViewRouteHelper.CIERRE_CAJA_ROOT);
	}

	private ModelAndView vista(CierreCajaFormDTO form, String username) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_CIERRE_CAJA);
		modelAndView.addObject("form", form);
		modelAndView.addObject("estado", cierreCajaService.getEstadoActual(username));
		return modelAndView;
	}
}
