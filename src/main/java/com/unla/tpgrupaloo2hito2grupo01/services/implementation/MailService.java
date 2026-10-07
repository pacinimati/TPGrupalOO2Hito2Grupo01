package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.unla.tpgrupaloo2hito2grupo01.exceptions.EnvioMailException;
import com.unla.tpgrupaloo2hito2grupo01.services.IMailService;

@Service("mailService")
public class MailService implements IMailService {

	private static final String ASUNTO = "Epicentro Gourmet - Tus credenciales de acceso";

	private final JavaMailSender mailSender;

	// Casilla remitente: la misma cuenta con la que se autentica el servidor SMTP
	@Value("${spring.mail.username:}")
	private String remitente;

	public MailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void enviarCredenciales(String destinatario, String nombre, String contrasenia) {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		if (remitente != null && !remitente.isBlank()) {
			mensaje.setFrom(remitente);
		}
		mensaje.setTo(destinatario);
		mensaje.setSubject(ASUNTO);
		mensaje.setText("Hola " + nombre + ",\n\n"
				+ "Ya podés ingresar al sistema de gestión de Epicentro Gourmet con estas credenciales:\n\n"
				+ "  Usuario: " + destinatario + "\n"
				+ "  Contraseña: " + contrasenia + "\n\n"
				+ "Epicentro Gourmet");
		try {
			mailSender.send(mensaje);
		} catch (MailException e) {
			throw new EnvioMailException("No se pudo enviar el mail a " + destinatario, e);
		}
	}
}
