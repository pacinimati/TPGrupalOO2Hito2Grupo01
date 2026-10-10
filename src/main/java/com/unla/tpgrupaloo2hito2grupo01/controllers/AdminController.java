package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PeriodoRentabilidadNetaFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteRentabilidadNetaPeriodoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.exceptions.UnidadNoPerteneceFestivalException;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IFestivalService;
import com.unla.tpgrupaloo2hito2grupo01.services.IPedidoService;
import com.unla.tpgrupaloo2hito2grupo01.services.IRentabilidadNetaService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUnidadVentaService;

import jakarta.validation.Valid;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin")
public class AdminController {

	private final IFestivalService festivalService;
	private final IUnidadVentaService unidadVentaService;
	private final IPedidoService pedidoService;
	private final IRentabilidadNetaService rentabilidadNetaService;

	public AdminController(IFestivalService festivalService, IUnidadVentaService unidadVentaService,
			IPedidoService pedidoService, IRentabilidadNetaService rentabilidadNetaService) {
		this.festivalService = festivalService;
		this.unidadVentaService = unidadVentaService;
		this.pedidoService = pedidoService;
		this.rentabilidadNetaService = rentabilidadNetaService;
	}

	//GET: SERVER/admin/festivales
	@GetMapping("/festivales")
	public ModelAndView festivales() {
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_FESTIVALES);
		mAV.addObject("festivales", festivalService.getAll());
		return mAV;
	}

	//GET: SERVER/admin/festivales/{id}/unidades
	@GetMapping("/festivales/{id}/unidades")
	public ModelAndView unidades(@PathVariable long id) {
		Festival festival = festivalService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El festival no existe"));
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_UNIDADES);
		mAV.addObject("festival", festival);
		mAV.addObject("unidades", unidadVentaService.getByFestival(id));
		return mAV;
	}

	//GET: SERVER/admin/festivales/{id}/ranking
	@GetMapping("/festivales/{id}/ranking")
	public ModelAndView ranking(@PathVariable long id) {
		Festival festival = festivalService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El festival no existe"));
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_RANKING);
		mAV.addObject("festival", festival);
		mAV.addObject("ranking", unidadVentaService.getRankingPorRecaudacion(id));
		return mAV;
	}

	//GET: SERVER/admin/festivales/{id}/recaudacion
	@GetMapping("/festivales/{id}/recaudacion")
	public ModelAndView recaudacion(@PathVariable long id) {
		Festival festival = festivalService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El festival no existe"));
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_RECAUDACION);
		mAV.addObject("festival", festival);
		mAV.addObject("reporte", pedidoService.getReporteRecaudacion(id));
		return mAV;
	}

	//GET: SERVER/admin/festivales/{id}/mayores-canon?cantidad=3
	@GetMapping("/festivales/{id}/mayores-canon")
	public ModelAndView mayoresCanon(@PathVariable long id, @RequestParam(defaultValue = "3") int cantidad) {
		Festival festival = festivalService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El festival no existe"));
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_MAYORES_CANON);
		mAV.addObject("festival", festival);
		mAV.addObject("cantidad", cantidad);
		List<ErrorCampoDTO> errores = unidadVentaService.validarCantidadMayoresCanon(cantidad);
		mAV.addObject("errores", errores);
		mAV.addObject("reporte", errores.isEmpty()
				? unidadVentaService.getUnidadesConMayorCanon(id, cantidad)
				: List.of());
		return mAV;
	}

	// GET: SERVER/admin/festivales/{festivalId}/unidades/{unidadVentaId}/rentabilidad
	@GetMapping("/festivales/{festivalId}/unidades/{unidadVentaId}/rentabilidad")
	public ModelAndView rentabilidadNeta(@PathVariable long festivalId, @PathVariable long unidadVentaId) {
		Festival festival = festivalService.findById(festivalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El festival no existe"));
		ReporteRentabilidadNetaDTO reporte;
		try {
			reporte = rentabilidadNetaService.getReporte(festivalId, unidadVentaId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La unidad de venta no existe"));
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La unidad no pertenece al festival", e);
		}

		ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_RENTABILIDAD_NETA);
		mAV.addObject("festival", festival);
		mAV.addObject("reporte", reporte);
		return mAV;
	}

	@GetMapping("/festivales/{festivalId}/unidades/{unidadVentaId}/rentabilidad/periodo")
	public ModelAndView formularioRentabilidadNetaPeriodo(@PathVariable long festivalId,
			@PathVariable long unidadVentaId) {
		Festival festival = festivalService.findById(festivalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El festival no existe"));
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_RENTABILIDAD_NETA_PERIODO);
		mAV.addObject("festival", festival);
		mAV.addObject("unidadVentaId", unidadVentaId);
		mAV.addObject("periodo", new PeriodoRentabilidadNetaFormDTO());
		return mAV;
	}

	@PostMapping("/festivales/{festivalId}/unidades/{unidadVentaId}/rentabilidad/periodo")
	public ModelAndView calcularRentabilidadNetaPeriodo(@PathVariable long festivalId,
			@PathVariable long unidadVentaId, @Valid @ModelAttribute("periodo") PeriodoRentabilidadNetaFormDTO periodo,
			BindingResult result) {
		Festival festival = obtenerFestival(festivalId);
		if (!result.hasErrors() && periodo.getDesde() != null && periodo.getHasta() != null) {
			agregarErroresPeriodo(festival, periodo, result);
		}
		if (result.hasErrors()) {
			return vistaRentabilidadNetaPeriodo(festival, unidadVentaId, periodo);
		}

		ReporteRentabilidadNetaPeriodoDTO reporte;
		try {
			reporte = rentabilidadNetaService.getReportePeriodo(
					festival, unidadVentaId, periodo.getDesde(), periodo.getHasta())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
							"La unidad de venta no existe"));
		} catch (UnidadNoPerteneceFestivalException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"La unidad no pertenece al festival", e);
		}
		ModelAndView mAV = vistaRentabilidadNetaPeriodo(festival, unidadVentaId, periodo);
		mAV.addObject("reporte", reporte);
		return mAV;
	}

	private Festival obtenerFestival(long festivalId) {
		return festivalService.findById(festivalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El festival no existe"));
	}

	private ModelAndView vistaRentabilidadNetaPeriodo(Festival festival, long unidadVentaId,
			PeriodoRentabilidadNetaFormDTO periodo) {
		ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_RENTABILIDAD_NETA_PERIODO);
		mAV.addObject("festival", festival);
		mAV.addObject("unidadVentaId", unidadVentaId);
		mAV.addObject("periodo", periodo);
		return mAV;
	}

	private void agregarErroresPeriodo(Festival festival, PeriodoRentabilidadNetaFormDTO periodo,
			BindingResult result) {
		for (ErrorCampoDTO error : rentabilidadNetaService.validarPeriodo(
				festival, periodo.getDesde(), periodo.getHasta())) {
			if (!result.hasFieldErrors(error.campo())) {
				result.rejectValue(error.campo(), error.codigo(), error.mensaje());
			}
		}
	}
}
