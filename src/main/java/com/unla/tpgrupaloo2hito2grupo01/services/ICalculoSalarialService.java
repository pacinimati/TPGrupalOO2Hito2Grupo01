package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;

public interface ICalculoSalarialService {

	Optional<Resultado> calcularSueldo(Personal personal, Festival festival);

	record Resultado(double sueldo, String error) {

		public boolean esCalculable() {
			return error == null;
		}
	}
}
