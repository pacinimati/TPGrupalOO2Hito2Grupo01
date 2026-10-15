package com.unla.tpgrupaloo2hito2grupo01.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.unla.tpgrupaloo2hito2grupo01.configuration.SecurityConfiguration;
import com.unla.tpgrupaloo2hito2grupo01.dtos.SituacionLaboralActualDTO;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUserService;

@WebMvcTest(controllers = EmpleadoPortalController.class)
@Import(SecurityConfiguration.class)
class EmpleadoPortalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IEmpleadoService empleadoService;

	@MockBean
	private IUserService userService;

	@MockBean
	private IPersonalRepository personalRepository;

	@Test
	@WithMockUser(username = "empleado@epicentro.test", roles = "EMPLEADO")
	void muestraLaSituacionLaboralDelEmpleadoAutenticado() throws Exception {
		SituacionLaboralActualDTO situacion = new SituacionLaboralActualDTO(
				"Ana Pérez", true, "Festival activo",
				LocalDate.now(), LocalDate.now().plusDays(4), "Food Truck", "ABC1234567");
		when(empleadoService.getSituacionLaboralActual("empleado@epicentro.test")).thenReturn(situacion);

		mockMvc.perform(get("/empleado/actualidad"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.EMPLEADO_ACTUALIDAD))
				.andExpect(model().attribute("situacion", situacion));

		verify(empleadoService).getSituacionLaboralActual("empleado@epicentro.test");
	}

	@Test
	void requiereAutenticacion() throws Exception {
		mockMvc.perform(get("/empleado/actualidad")).andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(roles = "RESPONSABLE")
	void restringeLaConsultaAlRolEmpleado() throws Exception {
		mockMvc.perform(get("/empleado/actualidad")).andExpect(status().isForbidden());
	}
}
