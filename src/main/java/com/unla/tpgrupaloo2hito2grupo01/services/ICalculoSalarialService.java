package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;

public interface ICalculoSalarialService {

	Optional<Resultado> calcularSueldo(Personal personal, Festival festival);

	// detalle es nulo cuando el sueldo no es calculable
	record Resultado(double sueldo, String error, Detalle detalle) {

		public Resultado(double sueldo, String error) {
			this(sueldo, error, null);
		}

		public boolean esCalculable() {
			return error == null;
		}
	}

	// Conceptos del sueldo (para el recibo): sueldo = porcentaje * (sueldoBase + adicional), donde el adicional es
	// el plus por categoría (cocina) o la antigüedad (cajero)
	record Detalle(long diasFestival, double porcentaje, double sueldoBase, String conceptoAdicional,
			double adicional) {
	}
}
