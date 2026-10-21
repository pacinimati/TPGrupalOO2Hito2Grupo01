package com.unla.tpgrupaloo2hito2grupo01.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.unla.tpgrupaloo2hito2grupo01.configuration.SecurityConfiguration;
import com.unla.tpgrupaloo2hito2grupo01.dtos.HistorialLaboralDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReciboSueldoDTO;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IHistorialLaboralService;
import com.unla.tpgrupaloo2hito2grupo01.services.IReciboSueldoPdfService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUserService;

@WebMvcTest(controllers = HistorialLaboralController.class)
@Import(SecurityConfiguration.class)
class HistorialLaboralControllerTest {

	private static final String USERNAME = "empleado@epicentro.test";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IHistorialLaboralService historialLaboralService;

	@MockBean
	private IReciboSueldoPdfService reciboSueldoPdfService;

	@MockBean
	private IUserService userService;

	@MockBean
	private IPersonalRepository personalRepository;

	@Test
	@WithMockUser(username = USERNAME, roles = "EMPLEADO")
	void muestraElHistorialDelEmpleadoAutenticado() throws Exception {
		List<HistorialLaboralDTO> historial = List.of(new HistorialLaboralDTO(1L, "Festival", "Invierno",
				LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 8), "Finalizado", "Food Truck", "ABC1234567",
				"Food Truck", "Cocina · Lavaplatos", 225_000.0, null));
		when(historialLaboralService.getHistorialLaboral(USERNAME)).thenReturn(historial);

		mockMvc.perform(get("/empleado/historial"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.EMPLEADO_HISTORIAL))
				.andExpect(model().attribute("historial", historial));
	}

	@Test
	@WithMockUser(username = USERNAME, roles = "EMPLEADO")
	void descargaElReciboEnPdf() throws Exception {
		ReciboSueldoDTO recibo = new ReciboSueldoDTO("Ana Pérez", 30101101, USERNAME, "Cocina · Lavaplatos",
				LocalDate.of(2023, 2, 1), "Festival", "Invierno", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 8),
				38, "Food Truck", "ABC1234567", "Food Truck", 100_000, "Plus por categoría (Lavaplatos)", 50_000,
				1.5, 225_000, LocalDate.now());
		byte[] pdf = "%PDF-prueba".getBytes();
		when(historialLaboralService.getReciboSueldo(USERNAME, 1L)).thenReturn(Optional.of(recibo));
		when(reciboSueldoPdfService.generarPdf(recibo)).thenReturn(pdf);
		when(reciboSueldoPdfService.nombreArchivo(recibo)).thenReturn("recibo-sueldo-festival.pdf");

		mockMvc.perform(get("/empleado/historial/1/recibo"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF))
				.andExpect(header().string("Content-Disposition",
						"attachment; filename=\"recibo-sueldo-festival.pdf\""))
				.andExpect(content().bytes(pdf));
	}

	@Test
	@WithMockUser(username = USERNAME, roles = "EMPLEADO")
	void respondeNotFoundSiNoHayReciboParaEseFestival() throws Exception {
		when(historialLaboralService.getReciboSueldo(USERNAME, 99L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/empleado/historial/99/recibo")).andExpect(status().isNotFound());
	}

	@Test
	void requiereAutenticacion() throws Exception {
		mockMvc.perform(get("/empleado/historial")).andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(roles = "RESPONSABLE")
	void restringeElHistorialAlRolEmpleado() throws Exception {
		mockMvc.perform(get("/empleado/historial")).andExpect(status().isForbidden());
		mockMvc.perform(get("/empleado/historial/1/recibo")).andExpect(status().isForbidden());
	}
}
