package com.unla.tpgrupaloo2hito2grupo01.dtos;

// Error de una regla de negocio asociado al campo del formulario que lo provocó (no persiste)
public record ErrorCampoDTO(String campo, String codigo, String mensaje) {
}
