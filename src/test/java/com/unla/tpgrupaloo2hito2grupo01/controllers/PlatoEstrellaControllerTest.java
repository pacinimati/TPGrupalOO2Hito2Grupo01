package com.unla.tpgrupaloo2hito2grupo01.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.unla.tpgrupaloo2hito2grupo01.configuration.SecurityConfiguration;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PlatoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaOpcionDTO;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IPlatoEstrellaService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUserService;

@WebMvcTest(controllers = PlatoEstrellaController.class)
@Import(SecurityConfiguration.class)
class PlatoEstrellaControllerTest {

	private static final String PLATO_ESTRELLA = "/responsable/plato-estrella";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IEmpleadoService empleadoService;

	@MockBean
	private IPlatoEstrellaService platoEstrellaService;

	@MockBean
	private IUserService userService;

	@MockBean
	private IPersonalRepository personalRepository;

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void muestraElPlatoEstrellaDeLaUnidadDelResponsable() throws Exception {
		PlatoDTO plato = new PlatoDTO(10, "Choripán", 3500, 1200, 7, 315);
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.of(unidadConFestival()));
		when(platoEstrellaService.getPlatoEstrellaDeUnidadDelResponsable("responsable"))
				.thenReturn(Optional.of(plato));

		mockMvc.perform(get(PLATO_ESTRELLA))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.RESPONSABLE_PLATO_ESTRELLA))
				.andExpect(model().attribute("plato", plato))
				.andExpect(model().attributeExists("unidad"));
	}

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void sinPedidosLaVistaNoRecibePlato() throws Exception {
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.of(unidadConFestival()));
		when(platoEstrellaService.getPlatoEstrellaDeUnidadDelResponsable("responsable"))
				.thenReturn(Optional.empty());

		mockMvc.perform(get(PLATO_ESTRELLA))
				.andExpect(status().isOk())
				.andExpect(model().attributeDoesNotExist("plato"));
	}

	@Test
	@WithMockUser(username = "empleado", roles = "EMPLEADO")
	void unEmpleadoNoPuedeVerElPlatoEstrella() throws Exception {
		mockMvc.perform(get(PLATO_ESTRELLA))
				.andExpect(status().isForbidden());
	}

	private UnidadVentaOpcionDTO unidadConFestival() {
		return new UnidadVentaOpcionDTO(2, "Unidad propia", "ABCDEF1234", "Festival",
				LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 30));
	}
}
