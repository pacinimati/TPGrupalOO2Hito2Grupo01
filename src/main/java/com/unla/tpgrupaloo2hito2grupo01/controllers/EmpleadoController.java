package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFiltroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.entities.TurnoTrabajo;
import com.unla.tpgrupaloo2hito2grupo01.exceptions.EnvioMailException;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;

import jakarta.validation.Valid;

// ABM de empleados de la unidad de venta del Responsable
@Controller
@PreAuthorize("hasRole('ROLE_RESPONSABLE')")
@RequestMapping("/responsable/empleados")
public class EmpleadoController {

	private static final String MENSAJE_NO_ENCONTRADO = "El empleado no existe o no pertenece a tu unidad de venta";

	private final IEmpleadoService empleadoService;

	public EmpleadoController(IEmpleadoService empleadoService) {
		this.empleadoService = empleadoService;
	}

	//GET: SERVER/responsable/empleados?nombre=&apellido=&dni=&tipo=&eliminados=false
	@GetMapping
	public ModelAndView listado(@ModelAttribute("filtro") EmpleadoFiltroDTO filtro, Principal principal) {
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.EMPLEADOS_LISTADO);
		mAV.addObject("unidad", empleadoService.getUnidadDelResponsable(principal.getName()).orElse(null));
		mAV.addObject("empleados", empleadoService.filtrar(principal.getName(), filtro));
		mAV.addObject("tipos", TipoEmpleado.values());
		return mAV;
	}

	//GET: SERVER/responsable/empleados/nuevo
	@GetMapping("/nuevo")
	public ModelAndView formularioAlta(Principal principal) {
		return formulario(new EmpleadoFormDTO(), principal);
	}

	//POST: SERVER/responsable/empleados/nuevo
	@PostMapping("/nuevo")
	public ModelAndView alta(@Valid @ModelAttribute("empleado") EmpleadoFormDTO empleado, BindingResult result,
							 Principal principal, RedirectAttributes redirectAttributes) {
		empleado.setId(null);
		validarReglasDeNegocio(empleado, principal.getName(), result);
		if (!result.hasErrors()) {
			try {
				empleadoService.altaEmpleado(empleado, principal.getName());
				redirectAttributes.addFlashAttribute("exito", "Empleado dado de alta. Se envió un mail a "
						+ empleado.getEmail() + " con su usuario y contraseña.");
				return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT);
			} catch (EnvioMailException e) {
				result.reject("mail", "No se pudo enviar el mail al empleado, por lo que no se dio de alta. "
						+ "Revisá la casilla e intentá nuevamente.");
			}
		}
		return formulario(empleado, principal);
	}

	//GET: SERVER/responsable/empleados/{id}/modificar
	@GetMapping("/{id}/modificar")
	public ModelAndView formularioModificacion(@PathVariable long id, Principal principal) {
		EmpleadoFormDTO empleado = empleadoService.getParaModificar(principal.getName(), id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MENSAJE_NO_ENCONTRADO));
		return formulario(empleado, principal);
	}

	//POST: SERVER/responsable/empleados/{id}/modificar
	@PostMapping("/{id}/modificar")
	public ModelAndView modificacion(@PathVariable long id, @Valid @ModelAttribute("empleado") EmpleadoFormDTO empleado,
									 BindingResult result, Principal principal, RedirectAttributes redirectAttributes) {
		empleado.setId(id);
		validarReglasDeNegocio(empleado, principal.getName(), result);
		if (!result.hasErrors()) {
			if (!empleadoService.modificarEmpleado(empleado, principal.getName())) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, MENSAJE_NO_ENCONTRADO);
			}
			redirectAttributes.addFlashAttribute("exito", "Datos del empleado actualizados.");
			return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT);
		}
		return formulario(empleado, principal);
	}

	//POST: SERVER/responsable/empleados/{id}/eliminar
	@PostMapping("/{id}/eliminar")
	public ModelAndView baja(@PathVariable long id, Principal principal, RedirectAttributes redirectAttributes) {
		if (empleadoService.eliminarEmpleado(id, principal.getName())) {
			redirectAttributes.addFlashAttribute("exito", "Empleado eliminado. Podés reactivarlo desde el listado de eliminados.");
		} else {
			redirectAttributes.addFlashAttribute("error", MENSAJE_NO_ENCONTRADO);
		}
		return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT);
	}

	//POST: SERVER/responsable/empleados/{id}/reactivar
	@PostMapping("/{id}/reactivar")
	public ModelAndView reactivacion(@PathVariable long id, Principal principal, RedirectAttributes redirectAttributes) {
		try {
			if (empleadoService.reactivarEmpleado(id, principal.getName())) {
				redirectAttributes.addFlashAttribute("exito",
						"Empleado reactivado. Se le envió por mail su usuario y una contraseña nueva.");
			} else {
				redirectAttributes.addFlashAttribute("error", MENSAJE_NO_ENCONTRADO);
			}
		} catch (EnvioMailException e) {
			redirectAttributes.addFlashAttribute("error",
					"No se pudo enviar el mail al empleado, por lo que no se reactivó. Intentá nuevamente.");
		}
		return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT + "?eliminados=true");
	}

	// Las reglas de negocio las decide el service; acá solo se vuelcan al formulario.
	// Si el campo ya falló la validación de formato, se conserva ese error
	private void validarReglasDeNegocio(EmpleadoFormDTO empleado, String username, BindingResult result) {
		for (ErrorCampoDTO error : empleadoService.validarReglasDeNegocio(empleado, username)) {
			if (!result.hasFieldErrors(error.campo())) {
				result.rejectValue(error.campo(), error.codigo(), error.mensaje());
			}
		}
	}

	// Es el mismo objeto que Spring ya asoció a sus errores (BindingResult), así que los errores se conservan
	private ModelAndView formulario(EmpleadoFormDTO empleado, Principal principal) {
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.EMPLEADOS_FORM);
		mAV.addObject("empleado", empleado);
		mAV.addObject("unidad", empleadoService.getUnidadDelResponsable(principal.getName()).orElse(null));
		mAV.addObject("tipos", TipoEmpleado.values());
		mAV.addObject("categorias", CategoriaCocina.values());
		mAV.addObject("turnos", TurnoTrabajo.values());
		return mAV;
	}
}
