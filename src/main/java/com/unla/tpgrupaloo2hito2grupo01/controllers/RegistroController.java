package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.unla.tpgrupaloo2hito2grupo01.dtos.RegistroUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.TipoUnidad;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IFestivalService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUnidadVentaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/registro/unidad")
public class RegistroController {

	private static final int EDAD_MINIMA = 18;

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

	private void validarReglasDeNegocio(RegistroUnidadDTO registro, BindingResult result) {
		if (registro.getUsuario() != null && unidadVentaService.existeUsuario(registro.getUsuario())) {
			result.rejectValue("usuario", "duplicado", "Ese usuario ya está en uso");
		}
		if (registro.getCodigoUnico() != null && !result.hasFieldErrors("codigoUnico")
				&& unidadVentaService.existeCodigoUnico(registro.getCodigoUnico())) {
			result.rejectValue("codigoUnico", "duplicado", "Ya existe una unidad con ese código");
		}
		if (registro.getFechaNacimiento() != null && !result.hasFieldErrors("fechaNacimiento")
				&& Period.between(registro.getFechaNacimiento(), LocalDate.now()).getYears() < EDAD_MINIMA) {
			result.rejectValue("fechaNacimiento", "menor", "El responsable debe ser mayor de edad");
		}
		if (registro.getTipo() == TipoUnidad.FOOD_TRUCK
				&& (registro.getPatente() == null || registro.getPatente().isBlank())) {
			result.rejectValue("patente", "requerido", "La patente es obligatoria para un Food Truck");
		}
		if (registro.getTipo() == TipoUnidad.PUESTO_DESARMABLE) {
			if (registro.getCarpasComponen() <= 0) {
				result.rejectValue("carpasComponen", "requerido", "Indique la cantidad de carpas");
			}
			if (registro.getTiempoMontaje() <= 0) {
				result.rejectValue("tiempoMontaje", "requerido", "Indique el tiempo de montaje en minutos");
			}
		}
	}

	private void cargarOpciones(Model model) {
		model.addAttribute("festivales", festivalService.getAll());
		model.addAttribute("tipos", TipoUnidad.values());
	}
}
