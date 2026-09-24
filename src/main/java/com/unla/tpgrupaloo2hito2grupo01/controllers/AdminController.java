package com.unla.tpgrupaloo2hito2grupo01.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IFestivalService;
import com.unla.tpgrupaloo2hito2grupo01.services.IUnidadVentaService;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin")
public class AdminController {

	private final IFestivalService festivalService;
	private final IUnidadVentaService unidadVentaService;

	public AdminController(IFestivalService festivalService, IUnidadVentaService unidadVentaService) {
		this.festivalService = festivalService;
		this.unidadVentaService = unidadVentaService;
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
}
