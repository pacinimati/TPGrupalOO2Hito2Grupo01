package com.unla.tpgrupaloo2hito2grupo01.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.RegistroUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoUnidad;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IFestivalService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUnidadVentaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/registro/unidad")
public class RegistroController {

	private final IUnidadVentaService unidadVentaService;
	private final IFestivalService festivalService;

	public RegistroController(IUnidadVentaService unidadVentaService, IFestivalService festivalService) {
		this.unidadVentaService = unidadVentaService;
		this.festivalService = festivalService;
	}

	//GET: SERVER/registro/unidad
	@GetMapping
	public String formulario(Model model) {
		model.addAttribute("registro", new RegistroUnidadDTO());
		cargarOpciones(model);
		return ViewRouteHelper.REGISTRO_UNIDAD;
	}

	//POST: SERVER/registro/unidad
	@PostMapping
	public String registrar(@Valid @ModelAttribute("registro") RegistroUnidadDTO registro,
							BindingResult result, Model model) {
		validarReglasDeNegocio(registro, result);
		if (result.hasErrors()) {
			cargarOpciones(model);
			return ViewRouteHelper.REGISTRO_UNIDAD;
		}
		unidadVentaService.registrarConResponsable(registro);
		return "redirect:/login?registrado";
	}

	// Las reglas de negocio las decide el service; acá solo se vuelcan al formulario.
	// Si el campo ya falló la validación de formato, se conserva ese error
	private void validarReglasDeNegocio(RegistroUnidadDTO registro, BindingResult result) {
		for (ErrorCampoDTO error : unidadVentaService.validarReglasDeNegocio(registro)) {
			if (!result.hasFieldErrors(error.campo())) {
				result.rejectValue(error.campo(), error.codigo(), error.mensaje());
			}
		}
	}

	private void cargarOpciones(Model model) {
		model.addAttribute("festivales", festivalService.getAll());
		model.addAttribute("tipos", TipoUnidad.values());
	}
}
