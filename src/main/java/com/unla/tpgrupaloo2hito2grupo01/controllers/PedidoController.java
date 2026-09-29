package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PedidoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IPedidoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

	private final IPedidoService pedidoService;

	public PedidoController(IPedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	//GET: SERVER/pedidos/nuevo
	@GetMapping("/nuevo")
	public String formulario(Model model, Principal principal) {
		model.addAttribute("pedido", new PedidoFormDTO());
		cargarOpciones(model, principal.getName());
		return ViewRouteHelper.PEDIDO_NUEVO;
	}

	//POST: SERVER/pedidos/nuevo
	@PostMapping("/nuevo")
	public String registrar(@Valid @ModelAttribute("pedido") PedidoFormDTO pedido, BindingResult result,
							Model model, Principal principal, RedirectAttributes redirectAttributes) {
		validarReglasDeNegocio(pedido, principal.getName(), result);
		if (result.hasErrors()) {
			cargarOpciones(model, principal.getName());
			return ViewRouteHelper.PEDIDO_NUEVO;
		}
		redirectAttributes.addFlashAttribute("pedidoRegistrado", pedidoService.registrar(pedido, principal.getName()));
		return "redirect:" + ViewRouteHelper.PEDIDO_ROOT;
	}

	// Las reglas de negocio las decide el service; acá solo se vuelcan al formulario.
	// Si el campo ya falló la validación de formato, se conserva ese error
	private void validarReglasDeNegocio(PedidoFormDTO pedido, String username, BindingResult result) {
		for (ErrorCampoDTO error : pedidoService.validarReglasDeNegocio(pedido, username)) {
			if (!result.hasFieldErrors(error.campo())) {
				result.rejectValue(error.campo(), error.codigo(), error.mensaje());
			}
		}
	}

	private void cargarOpciones(Model model, String username) {
		model.addAttribute("unidades", pedidoService.getUnidadesHabilitadas(username));
		model.addAttribute("platos", pedidoService.getPlatos());
	}
}
