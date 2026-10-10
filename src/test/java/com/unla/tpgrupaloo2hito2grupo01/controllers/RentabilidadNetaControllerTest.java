package com.unla.tpgrupaloo2hito2grupo01.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.unla.tpgrupaloo2hito2grupo01.configuration.SecurityConfiguration;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaPeriodoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.UnidadVentaOpcionDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IEmpleadoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IFestivalService;
import com.unla.tpgrupaloo2hito2grupo01.services.IPedidoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IRentabilidadNetaService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUnidadVentaService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUserService;

@WebMvcTest(controllers = { RentabilidadNetaController.class, AdminController.class })
@Import(SecurityConfiguration.class)
class RentabilidadNetaControllerTest {

	private static final String RESPONSABLE_PERIODO = "/responsable/rentabilidad/periodo";
	private static final String ADMIN_PERIODO = "/admin/festivales/1/unidades/2/rentabilidad/periodo";
	private static final LocalDate DESDE = LocalDate.of(2026, 1, 10);
	private static final LocalDate HASTA = LocalDate.of(2026, 1, 20);

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IEmpleadoService empleadoService;

	@MockBean
	private IRentabilidadNetaService rentabilidadNetaService;

	@MockBean
	private IFestivalService festivalService;

	@MockBean
	private IUnidadVentaService unidadVentaService;

	@MockBean
	private IPedidoService pedidoService;

	@MockBean
	private IUserService userService;

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void getResponsablePresentaElFormularioParaSuUnidad() throws Exception {
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.of(unidadConFestival()));

		mockMvc.perform(get(RESPONSABLE_PERIODO))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.RESPONSABLE_RENTABILIDAD_NETA_PERIODO))
				.andExpect(model().attributeExists("periodo", "unidad"))
				.andExpect(model().attribute("unidadConFestival", true));

		verify(empleadoService).getUnidadDelResponsable("responsable");
	}

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void getResponsableConservaLaPantallaDeCu6() throws Exception {
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.of(unidadConFestival()));
		when(rentabilidadNetaService.getReporteDeUnidadDelResponsable("responsable"))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/responsable/rentabilidad"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.RESPONSABLE_RENTABILIDAD_NETA));

		verify(rentabilidadNetaService).getReporteDeUnidadDelResponsable("responsable");
	}

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void postResponsableRechazaFechasBeanInvalidasSinLlamarAlServicioDeCalculo() throws Exception {
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.of(unidadConFestival()));

		mockMvc.perform(post(RESPONSABLE_PERIODO)
				.param("hasta", HASTA.toString()))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.RESPONSABLE_RENTABILIDAD_NETA_PERIODO))
				.andExpect(model().attributeHasFieldErrors("periodo", "desde"))
				.andExpect(model().attribute("periodo", hasProperty("hasta", equalTo(HASTA))));

		verify(rentabilidadNetaService, never())
				.validarPeriodoDeUnidadDelResponsable(any(), any(), any());
		verify(rentabilidadNetaService, never())
				.getReportePeriodoDeUnidadDelResponsable(any(), any(), any());
	}

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void postResponsableConservaFechasValidasCuandoHayErrorDeFormatoYNoCalcula() throws Exception {
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.of(unidadConFestival()));

		mockMvc.perform(post(RESPONSABLE_PERIODO)
				.param("desde", "fecha-invalida")
				.param("hasta", HASTA.toString()))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.RESPONSABLE_RENTABILIDAD_NETA_PERIODO))
				.andExpect(model().attributeHasFieldErrors("periodo", "desde"))
				.andExpect(model().attribute("periodo", hasProperty("hasta", equalTo(HASTA))));

		verify(rentabilidadNetaService, never())
				.validarPeriodoDeUnidadDelResponsable(any(), any(), any());
		verify(rentabilidadNetaService, never())
				.getReportePeriodoDeUnidadDelResponsable(any(), any(), any());
	}

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void postResponsableMuestraErroresDeNegocioYNoCalcula() throws Exception {
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.of(unidadConFestival()));
		when(rentabilidadNetaService.validarPeriodoDeUnidadDelResponsable("responsable", DESDE, HASTA))
				.thenReturn(Optional.of(List.of(new ErrorCampoDTO(
						"hasta", "fueraDelFestival", "La fecha final está fuera del festival."))));

		mockMvc.perform(post(RESPONSABLE_PERIODO)
				.param("desde", DESDE.toString())
				.param("hasta", HASTA.toString())
				.param("unidadVentaId", "999")
				.param("festivalId", "999"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.RESPONSABLE_RENTABILIDAD_NETA_PERIODO))
				.andExpect(model().attributeHasFieldErrors("periodo", "hasta"));

		verify(rentabilidadNetaService, never())
				.getReportePeriodoDeUnidadDelResponsable(any(), any(), any());
	}

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void postResponsableMuestraElReporteObtenidoParaSuUsername() throws Exception {
		ReporteRentabilidadNetaPeriodoDTO reporte = reportePeriodo();
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.of(unidadConFestival()));
		when(rentabilidadNetaService.validarPeriodoDeUnidadDelResponsable("responsable", DESDE, HASTA))
				.thenReturn(Optional.of(List.of()));
		when(rentabilidadNetaService.getReportePeriodoDeUnidadDelResponsable("responsable", DESDE, HASTA))
				.thenReturn(Optional.of(reporte));

		mockMvc.perform(post(RESPONSABLE_PERIODO)
				.param("desde", DESDE.toString())
				.param("hasta", HASTA.toString())
				.param("unidadVentaId", "999")
				.param("festivalId", "999"))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.RESPONSABLE_RENTABILIDAD_NETA_PERIODO))
				.andExpect(model().attribute("reporte", reporte));

		verify(rentabilidadNetaService)
				.getReportePeriodoDeUnidadDelResponsable("responsable", DESDE, HASTA);
	}

	@Test
	@WithMockUser(username = "responsable", roles = "RESPONSABLE")
	void muestraEstadoVacioCuandoElResponsableNoTieneUnidad() throws Exception {
		when(empleadoService.getUnidadDelResponsable("responsable")).thenReturn(Optional.empty());

		mockMvc.perform(get(RESPONSABLE_PERIODO))
				.andExpect(status().isOk())
				.andExpect(model().attribute("unidadConFestival", false))
				.andExpect(model().attribute("sinUnidadOFestival", true));
	}

	@Test
	void rutasDeResponsableRequierenAutenticacion() throws Exception {
		mockMvc.perform(get(RESPONSABLE_PERIODO)).andExpect(status().is3xxRedirection());
		mockMvc.perform(post(RESPONSABLE_PERIODO)).andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void administradorNoPuedeAccederALasRutasDelResponsable() throws Exception {
		mockMvc.perform(get(RESPONSABLE_PERIODO)).andExpect(status().isForbidden());
		mockMvc.perform(post(RESPONSABLE_PERIODO)).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "EMPLEADO")
	void empleadoNoPuedeAccederALasRutasDelResponsable() throws Exception {
		mockMvc.perform(get(RESPONSABLE_PERIODO)).andExpect(status().isForbidden());
		mockMvc.perform(post(RESPONSABLE_PERIODO)).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void conservaElFormularioAdministrativoDeCu7() throws Exception {
		when(festivalService.findById(1)).thenReturn(Optional.of(festival()));

		mockMvc.perform(get(ADMIN_PERIODO))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.ADMIN_RENTABILIDAD_NETA_PERIODO));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void conservaElPostAdministrativoDeCu7() throws Exception {
		Festival festival = festival();
		ReporteRentabilidadNetaPeriodoDTO reporte = reportePeriodo();
		when(festivalService.findById(1)).thenReturn(Optional.of(festival));
		when(rentabilidadNetaService.validarPeriodo(festival, DESDE, HASTA)).thenReturn(List.of());
		when(rentabilidadNetaService.getReportePeriodo(festival, 2, DESDE, HASTA))
				.thenReturn(Optional.of(reporte));

		mockMvc.perform(post(ADMIN_PERIODO)
				.param("desde", DESDE.toString())
				.param("hasta", HASTA.toString()))
				.andExpect(status().isOk())
				.andExpect(view().name(ViewRouteHelper.ADMIN_RENTABILIDAD_NETA_PERIODO))
				.andExpect(model().attribute("reporte", reporte));
	}

	@Test
	@WithMockUser(roles = "RESPONSABLE")
	void responsableNoPuedeAccederALasRutasAdministrativasDeCu7() throws Exception {
		mockMvc.perform(get(ADMIN_PERIODO)).andExpect(status().isForbidden());
	}

	private UnidadVentaOpcionDTO unidadConFestival() {
		return new UnidadVentaOpcionDTO(2, "Unidad propia", "ABCDEF1234", "Festival",
				LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 30));
	}

	private Festival festival() {
		Festival festival = new Festival();
		festival.setId(1);
		festival.setNombre("Festival");
		festival.setFechaInicio(LocalDate.of(2026, 1, 1));
		festival.setFechaFin(LocalDate.of(2026, 1, 30));
		return festival;
	}

	private ReporteRentabilidadNetaPeriodoDTO reportePeriodo() {
		return new ReporteRentabilidadNetaPeriodoDTO(2, "Unidad propia", "Festival", DESDE, HASTA,
				11, 30, 11.0 / 30.0, 0, 0, 0.0, 0, 0, 0.0, true, "Cálculo completo.");
	}
}
