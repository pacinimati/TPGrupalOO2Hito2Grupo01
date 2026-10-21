package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReciboSueldoDTO;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.ReciboSueldoPdfService;

class ReciboSueldoPdfServiceTest {

	private final IReciboSueldoPdfService service = new ReciboSueldoPdfService();

	private final ReciboSueldoDTO recibo = new ReciboSueldoDTO("Ana Pérez", 30101101, "ana@epicentro.test",
			"Cocina · Lavaplatos", LocalDate.of(2023, 2, 1), "Feria Gastronómica de Invierno", "Invierno",
			LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 8), 38, "Choripán Don Pepe", "CHO0000011", "Food Truck",
			100_000, "Plus por categoría (Lavaplatos)", 50_000, 1.5, 225_000, LocalDate.of(2026, 10, 20));

	@Test
	void generaUnArchivoPdf() {
		byte[] pdf = service.generarPdf(recibo);

		assertTrue(pdf.length > 0);
		assertTrue(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1).startsWith("%PDF-"));
	}

	@Test
	void nombraElArchivoSegunElFestivalSinAcentosNiEspacios() {
		assertEquals("recibo-sueldo-feria-gastronomica-de-invierno.pdf", service.nombreArchivo(recibo));
	}
}
