package com.unla.tpgrupaloo2hito2grupo01.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.unla.tpgrupaloo2hito2grupo01.configuration.SecurityConfiguration;
import com.unla.tpgrupaloo2hito2grupo01.controllers.api.ApiExceptionHandler;
import com.unla.tpgrupaloo2hito2grupo01.controllers.api.v1.CierreCajaRestController;
import com.unla.tpgrupaloo2hito2grupo01.dtos.CierreCajaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.ICierreCajaService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUserService;

// La API es pública (/api/v1/**): se prueba sin usuario logueado
@WebMvcTest(CierreCajaRestController.class)
@Import({ SecurityConfiguration.class, ApiExceptionHandler.class })
class CierreCajaRestControllerTest {

	private static final String URL = "/api/v1/cierres-caja";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ICierreCajaService cierreCajaService;

	@MockBean
	private IUserService userService;

	@MockBean
	private IPersonalRepository personalRepository;

	@Test
	void buscarPasaLosFiltrosAlServiceYDevuelveJson() throws Exception {
		when(cierreCajaService.validarFiltro(any())).thenReturn(List.of());
		when(cierreCajaService.filtrar(any())).thenReturn(List.of(cierre()));

		mockMvc.perform(get(URL).param("festivalId", "6").param("unidadVentaId", "12")
				.param("fechaDesde", "2026-10-01").param("fechaHasta", "2026-10-31").param("conDiferencia", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombreUnidad").value("La Esquina"))
				.andExpect(jsonPath("$[0].diferencia").value(-50.0));

		verify(cierreCajaService).filtrar(argThat(filtro -> filtro.getFestivalId() == 6
				&& filtro.getUnidadVentaId() == 12 && filtro.getCajeroId() == null
				&& LocalDate.of(2026, 10, 1).equals(filtro.getFechaDesde())
				&& LocalDate.of(2026, 10, 31).equals(filtro.getFechaHasta()) && filtro.getConDiferencia()));
	}

	@Test
	void buscarConFiltrosInvalidosDevuelve400() throws Exception {
		when(cierreCajaService.validarFiltro(any()))
				.thenReturn(List.of(new ErrorCampoDTO("fechaHasta", "rangoInvalido", "Rango inválido")));

		mockMvc.perform(get(URL).param("fechaDesde", "2026-10-31").param("fechaHasta", "2026-10-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$[0].codigo").value("rangoInvalido"));
		verify(cierreCajaService, never()).filtrar(any());
	}

	@Test
	void buscarConUnaFechaMalFormadaDevuelve400() throws Exception {
		mockMvc.perform(get(URL).param("fechaDesde", "01/10/2026"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$[0].campo").value("fechaDesde"));
	}

	@Test
	void registrarDevuelve201ConElCierre() throws Exception {
		when(cierreCajaService.existeCajero(7)).thenReturn(true);
		when(cierreCajaService.validarRegistro(any())).thenReturn(List.of());
		when(cierreCajaService.registrarCierre(any())).thenReturn(cierre());

		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(7, "2026-10-02", "450.00")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.recaudacionPedidos").value(500.0));
	}

	@Test
	void registrarConDatosInvalidosDevuelve400SinLlegarAlService() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
				.content("{\"cajeroId\": 7, \"montoDeclarado\": -1}"))
				.andExpect(status().isBadRequest());
		verify(cierreCajaService, never()).registrarCierre(any());
	}

	@Test
	void registrarConUnaReglaDeNegocioIncumplidaDevuelve400() throws Exception {
		when(cierreCajaService.existeCajero(7)).thenReturn(true);
		when(cierreCajaService.validarRegistro(any()))
				.thenReturn(List.of(new ErrorCampoDTO("fecha", "cajaCerrada", "Ya cerrada")));

		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(7, "2026-10-02", "450.00")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$[0].codigo").value("cajaCerrada"));
		verify(cierreCajaService, never()).registrarCierre(any());
	}

	@Test
	void registrarConUnCajeroInexistenteDevuelve404() throws Exception {
		when(cierreCajaService.existeCajero(eq(99L))).thenReturn(false);

		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(99, "2026-10-02", "450.00")))
				.andExpect(status().isNotFound());
	}

	private CierreCajaDTO cierre() {
		return new CierreCajaDTO(1, LocalDate.of(2026, 10, 2), 7, "Ana Paz", 12, "La Esquina", "FT12345678",
				"Festival", new BigDecimal("500.00"), new BigDecimal("450.00"), new BigDecimal("-50.00"), null);
	}

	private String json(long cajeroId, String fecha, String monto) {
		return "{\"cajeroId\": " + cajeroId + ", \"fecha\": \"" + fecha + "\", \"montoDeclarado\": " + monto + "}";
	}
}
