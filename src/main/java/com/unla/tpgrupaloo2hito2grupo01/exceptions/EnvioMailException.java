package com.unla.tpgrupaloo2hito2grupo01.exceptions;

// No se pudo enviar un mail; al ser RuntimeException hace rollback de la transacción que lo envió
public class EnvioMailException extends RuntimeException {

	public EnvioMailException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}
