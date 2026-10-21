package com.unla.tpgrupaloo2hito2grupo01.controllers;

import java.security.Principal;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReciboSueldoDTO;
import com.unla.tpgrupaloo2hito2grupo01.helpers.ViewRouteHelper;
import com.unla.tpgrupaloo2hito2grupo01.services.IHistorialLaboralService;
import com.unla.tpgrupaloo2hito2grupo01.services.IReciboSueldoPdfService;

@Controller
@PreAuthorize("hasRole('ROLE_EMPLEADO')")
@RequestMapping("/empleado/historial")
public class HistorialLaboralController {

	private final IHistorialLaboralService historialLaboralService;
	private final IReciboSueldoPdfService reciboSueldoPdfService;

	public HistorialLaboralController(IHistorialLaboralService historialLaboralService,
			IReciboSueldoPdfService reciboSueldoPdfService) {
		this.historialLaboralService = historialLaboralService;
		this.reciboSueldoPdfService = reciboSueldoPdfService;
	}

	@GetMapping
	public ModelAndView historial(Principal principal) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_HISTORIAL);
		modelAndView.addObject("historial", historialLaboralService.getHistorialLaboral(principal.getName()));
		return modelAndView;
	}

	@GetMapping("/{festivalId}/recibo")
	public ResponseEntity<byte[]> descargarRecibo(@PathVariable long festivalId, Principal principal) {
		ReciboSueldoDTO recibo = historialLaboralService.getReciboSueldo(principal.getName(), festivalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(reciboSueldoPdfService.nombreArchivo(recibo)).build().toString())
				.body(reciboSueldoPdfService.generarPdf(recibo));
	}
}
