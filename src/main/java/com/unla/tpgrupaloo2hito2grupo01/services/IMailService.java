package com.unla.tpgrupaloo2hito2grupo01.services;

public interface IMailService {

	// Envía al empleado el usuario (su email) y la contraseña con la que inicia sesión.
	// Lanza EnvioMailException si el servidor de correo no pudo enviarlo
	void enviarCredenciales(String destinatario, String nombre, String contrasenia);
}
