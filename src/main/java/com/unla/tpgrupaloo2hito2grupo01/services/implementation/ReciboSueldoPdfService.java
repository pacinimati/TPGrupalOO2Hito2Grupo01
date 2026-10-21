package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ReciboSueldoDTO;
import com.unla.tpgrupaloo2hito2grupo01.services.IReciboSueldoPdfService;

@Service("reciboSueldoPdfService")
public class ReciboSueldoPdfService implements IReciboSueldoPdfService {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");
	private static final Color COLOR_ENCABEZADO = new Color(230, 233, 237);

	private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
	private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
	private static final Font FUENTE_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
	private static final Font FUENTE_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
	private static final Font FUENTE_PIE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

	@Override
	public byte[] generarPdf(ReciboSueldoDTO recibo) {
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
		PdfWriter.getInstance(documento, salida);
		documento.open();

		Paragraph titulo = new Paragraph("Epicentro Gourmet", FUENTE_TITULO);
		titulo.setAlignment(Element.ALIGN_CENTER);
		documento.add(titulo);
		Paragraph subtitulo = new Paragraph("Recibo de sueldo", FUENTE_SUBTITULO);
		subtitulo.setAlignment(Element.ALIGN_CENTER);
		subtitulo.setSpacingAfter(4);
		documento.add(subtitulo);
		Paragraph emision = new Paragraph("Fecha de emisión: " + fecha(recibo.fechaEmision()), FUENTE_NORMAL);
		emision.setAlignment(Element.ALIGN_CENTER);
		emision.setSpacingAfter(16);
		documento.add(emision);

		documento.add(seccion("Datos del empleado"));
		PdfPTable empleado = tablaDeDatos();
		fila(empleado, "Apellido y nombre", recibo.nombreEmpleado());
		fila(empleado, "DNI", String.valueOf(recibo.dni()));
		fila(empleado, "Usuario", recibo.usuario());
		fila(empleado, "Rol", recibo.rol());
		fila(empleado, "Fecha de ingreso", fecha(recibo.fechaIngreso()));
		documento.add(empleado);

		documento.add(seccion("Festival y unidad de venta"));
		PdfPTable festival = tablaDeDatos();
		fila(festival, "Festival", recibo.nombreFestival()
				+ (recibo.temporada() == null ? "" : " (" + recibo.temporada() + ")"));
		fila(festival, "Período", fecha(recibo.fechaInicio()) + " al " + fecha(recibo.fechaFin())
				+ " (" + recibo.diasFestival() + " días)");
		fila(festival, "Unidad de venta", recibo.nombreUnidad() + " - " + recibo.codigoUnico());
		fila(festival, "Tipo de unidad", recibo.tipoUnidad());
		documento.add(festival);

		documento.add(seccion("Liquidación"));
		documento.add(tablaDeConceptos(recibo));

		Paragraph pie = new Paragraph("El porcentaje se calcula según la duración del festival: "
				+ "(días / " + ConstantesSalariales.DIAS_POR_TRAMO + " + 1) x "
				+ porcentaje(ConstantesSalariales.FACTOR_SUELDO) + ", contando la parte entera de la división.",
				FUENTE_PIE);
		pie.setSpacingBefore(12);
		documento.add(pie);

		documento.close();
		return salida.toByteArray();
	}

	@Override
	public String nombreArchivo(ReciboSueldoDTO recibo) {
		String festival = Normalizer.normalize(recibo.nombreFestival(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase()
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("(^-|-$)", "");
		return "recibo-sueldo-" + festival + ".pdf";
	}

	private PdfPTable tablaDeConceptos(ReciboSueldoDTO recibo) {
		PdfPTable tabla = new PdfPTable(new float[] { 3, 1.3f });
		tabla.setWidthPercentage(100);
		celdaEncabezado(tabla, "Concepto", Element.ALIGN_LEFT);
		celdaEncabezado(tabla, "Importe", Element.ALIGN_RIGHT);

		concepto(tabla, "Sueldo base", moneda(recibo.sueldoBase()), FUENTE_NORMAL);
		concepto(tabla, recibo.conceptoAdicional(), moneda(recibo.adicional()), FUENTE_NORMAL);
		concepto(tabla, "Subtotal", moneda(recibo.sueldoBase() + recibo.adicional()), FUENTE_NEGRITA);
		concepto(tabla, "Porcentaje por duración del festival (" + recibo.diasFestival() + " días)",
				porcentaje(recibo.porcentaje()), FUENTE_NORMAL);

		PdfPCell totalEtiqueta = new PdfPCell(new Phrase("Total a cobrar", FUENTE_SUBTITULO));
		PdfPCell totalImporte = new PdfPCell(new Phrase(moneda(recibo.total()), FUENTE_SUBTITULO));
		totalImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
		for (PdfPCell celda : new PdfPCell[] { totalEtiqueta, totalImporte }) {
			celda.setBackgroundColor(COLOR_ENCABEZADO);
			celda.setPadding(6);
			tabla.addCell(celda);
		}
		return tabla;
	}

	private Paragraph seccion(String texto) {
		Paragraph seccion = new Paragraph(texto, FUENTE_SUBTITULO);
		seccion.setSpacingBefore(10);
		seccion.setSpacingAfter(6);
		return seccion;
	}

	private PdfPTable tablaDeDatos() {
		PdfPTable tabla = new PdfPTable(new float[] { 1.2f, 3 });
		tabla.setWidthPercentage(100);
		return tabla;
	}

	private void fila(PdfPTable tabla, String etiqueta, String valor) {
		PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, FUENTE_NEGRITA));
		PdfPCell celdaValor = new PdfPCell(new Phrase(valor == null ? "-" : valor, FUENTE_NORMAL));
		for (PdfPCell celda : new PdfPCell[] { celdaEtiqueta, celdaValor }) {
			celda.setBorder(Rectangle.BOTTOM);
			celda.setBorderColor(Color.LIGHT_GRAY);
			celda.setPadding(4);
			tabla.addCell(celda);
		}
	}

	private void celdaEncabezado(PdfPTable tabla, String texto, int alineacion) {
		PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_NEGRITA));
		celda.setBackgroundColor(COLOR_ENCABEZADO);
		celda.setHorizontalAlignment(alineacion);
		celda.setPadding(5);
		tabla.addCell(celda);
	}

	private void concepto(PdfPTable tabla, String concepto, String importe, Font fuente) {
		PdfPCell celdaConcepto = new PdfPCell(new Phrase(concepto, fuente));
		PdfPCell celdaImporte = new PdfPCell(new Phrase(importe, fuente));
		celdaImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
		for (PdfPCell celda : new PdfPCell[] { celdaConcepto, celdaImporte }) {
			celda.setPadding(5);
			tabla.addCell(celda);
		}
	}

	private String fecha(LocalDate fecha) {
		return fecha == null ? "-" : fecha.format(FORMATO_FECHA);
	}

	private String moneda(double importe) {
		return new DecimalFormat("$ #,##0.00", DecimalFormatSymbols.getInstance(LOCALE_AR)).format(importe);
	}

	private String porcentaje(double factor) {
		return new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(LOCALE_AR)).format(factor * 100) + "%";
	}
}
