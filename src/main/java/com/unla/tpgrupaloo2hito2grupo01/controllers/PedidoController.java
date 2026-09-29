package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
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
	public ModelAndView formulario(Principal principal) {
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.PEDIDO_NUEVO);
		mAV.addObject("pedido", new PedidoFormDTO());
		cargarOpciones(mAV, principal.getName());
		return mAV;
	}

	//POST: SERVER/pedidos/nuevo
	@PostMapping("/nuevo")
	public ModelAndView registrar(@Valid @ModelAttribute("pedido") PedidoFormDTO pedido, BindingResult result,
								  Principal principal, RedirectAttributes redirectAttributes) {
		validarReglasDeNegocio(pedido, principal.getName(), result);
		if (result.hasErrors()) {
			// Es el mismo objeto que Spring ya asoció a sus errores (BindingResult), así que los errores se conservan
			ModelAndView mAV = new ModelAndView(ViewRouteHelper.PEDIDO_NUEVO);
			mAV.addObject("pedido", pedido);
			cargarOpciones(mAV, principal.getName());
			return mAV;
		}
		redirectAttributes.addFlashAttribute("pedidoRegistrado", pedidoService.registrar(pedido, principal.getName()));
		return new ModelAndView("redirect:" + ViewRouteHelper.PEDIDO_ROOT);
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

	private void cargarOpciones(ModelAndView mAV, String username) {
		mAV.addObject("unidades", pedidoService.getUnidadesHabilitadas(username));
		mAV.addObject("platos", pedidoService.getPlatos());
	}
}
