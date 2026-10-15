package com.unla.tpgrupaloo2hito2grupo01.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.unla.tpgrupaloo2hito2grupo01.configuration.SecurityConfiguration;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EstadoCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.ICierreCajaService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUserService;

@WebMvcTest(CierreCajaController.class)
@Import(SecurityConfiguration.class)
class CierreCajaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ICierreCajaService cierreCajaService;

	@MockBean
	private IUserService userService;

	@MockBean
	private IPersonalRepository personalRepository;

	@Test
	@WithMockUser(username = "cajero", roles = "EMPLEADO")
	void cajeroPuedeConsultarElEstadoDeSuCaja() throws Exception {
		when(cierreCajaService.getEstadoActual("cajero")).thenReturn(
				new EstadoCajaDTO(LocalDate.now(), "Ana Paz", "La Esquina", "FT12345678",
						"Festival actual", true, new BigDecimal("120.00"), null, "Caja abierta."));

		mockMvc.perform(get("/empleado/caja"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.EMPLEADO_CIERRE_CAJA))
				.andExpect(model().attributeExists("form", "estado"));
	}

	@Test
	@WithMockUser(username = "cajero", roles = "EMPLEADO")
	void cajeroPuedeRegistrarElCierreYRecibeUnaRedireccion() throws Exception {
		when(cierreCajaService.validarCierre(any(), eq("cajero")))
				.thenReturn(List.of());
		when(cierreCajaService.cerrarCaja(any(), eq("cajero")))
				.thenReturn(new CierreCajaDTO(1, LocalDate.now(), "Ana Paz", "La Esquina", "FT12345678",
						new BigDecimal("120.00"), new BigDecimal("120.00"), BigDecimal.ZERO, null));

		mockMvc.perform(post("/empleado/caja").param("montoDeclarado", "120.00"))
				.andExpect(status().is3xxRedirection());

		verify(cierreCajaService).cerrarCaja(any(), eq("cajero"));
	}

	@Test
	@WithMockUser(username = "cajero", roles = "EMPLEADO")
	void noRegistraMontosNegativosYConservaElFormulario() throws Exception {
		when(cierreCajaService.getEstadoActual("cajero")).thenReturn(
				new EstadoCajaDTO(LocalDate.now(), "Ana Paz", "La Esquina", "FT12345678",
						"Festival actual", true, BigDecimal.ZERO, null, "Caja abierta."));

		mockMvc.perform(post("/empleado/caja").param("montoDeclarado", "-1.00"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.EMPLEADO_CIERRE_CAJA))
				.andExpect(model().attributeHasFieldErrors("form", "montoDeclarado"));

		verify(cierreCajaService, never()).validarCierre(any(), eq("cajero"));
		verify(cierreCajaService, never()).cerrarCaja(any(), eq("cajero"));
	}

	@Test
	@WithMockUser(username = "cajero", roles = "EMPLEADO")
	void conservaErroresDeNegocioEnElFormulario() throws Exception {
		when(cierreCajaService.validarCierre(any(), eq("cajero")))
				.thenReturn(List.of(new ErrorCampoDTO("montoDeclarado", "cajaCerrada", "La caja ya fue cerrada.")));
		when(cierreCajaService.getEstadoActual("cajero")).thenReturn(
				new EstadoCajaDTO(LocalDate.now(), "Ana Paz", "La Esquina", "FT12345678",
						"Festival actual", true, BigDecimal.ZERO, null, "Caja abierta."));

		mockMvc.perform(post("/empleado/caja").param("montoDeclarado", "50.00"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.EMPLEADO_CIERRE_CAJA))
				.andExpect(model().attributeHasFieldErrors("form", "montoDeclarado"));
	}

	@Test
	@WithMockUser(username = "cajero", roles = "EMPLEADO")
	void muestraElErrorSiLaCajaSeCierraEntreLaValidacionYElGuardado() throws Exception {
		when(cierreCajaService.validarCierre(any(), eq("cajero"))).thenReturn(List.of());
		doThrow(new IllegalArgumentException("La caja ya fue cerrada."))
				.when(cierreCajaService).cerrarCaja(any(), eq("cajero"));
		when(cierreCajaService.getEstadoActual("cajero")).thenReturn(
				new EstadoCajaDTO(LocalDate.now(), "Ana Paz", "La Esquina", "FT12345678",
						"Festival actual", true, BigDecimal.ZERO, null, "Caja abierta."));

		mockMvc.perform(post("/empleado/caja").param("montoDeclarado", "50.00"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.EMPLEADO_CIERRE_CAJA))
				.andExpect(model().attributeHasFieldErrors("form", "montoDeclarado"));
	}

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void responsableNoPuedeAccederAlCierreDeCaja() throws Exception {
		mockMvc.perform(get("/empleado/caja")).andExpect(status().isForbidden());
	}

	@Test
	void usuarioAnonimoDebeAutenticarseParaAccederAlCierre() throws Exception {
		mockMvc.perform(get("/empleado/caja")).andExpect(status().is3xxRedirection());
	}
}
