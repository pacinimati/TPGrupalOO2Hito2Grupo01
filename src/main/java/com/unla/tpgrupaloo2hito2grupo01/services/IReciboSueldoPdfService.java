package com.unla.tpgrupaloo2hito2grupo01.services;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReciboSueldoDTO;

public interface IReciboSueldoPdfService {

	// Contenido del archivo PDF del recibo de sueldo
	byte[] generarPdf(ReciboSueldoDTO recibo);

	// Nombre con el que se descarga el archivo (ej. recibo-sueldo-feria-gastronomica-de-invierno.pdf)
	String nombreArchivo(ReciboSueldoDTO recibo);
}
