package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.tpgrupaloo2hito2grupo01.dtos.LiquidacionHaberesDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.ICalculoSalarialService;
import com.unla.tpgrupaloo2hito2grupo01.services.ILiquidacionHaberesService;

@Service("liquidacionHaberesService")
public class LiquidacionHaberesService implements ILiquidacionHaberesService {

	private static final String MOTIVO_SIN_FESTIVAL = "La unidad de venta no tiene un festival asociado.";
	private static final String MOTIVO_TIPO_NO_CONTEMPLADO = "No se puede calcular el sueldo para este tipo de empleado.";

	private final IPersonalRepository personalRepository;
	private final ICalculoSalarialService calculoSalarialService;

	public LiquidacionHaberesService(IPersonalRepository personalRepository,
			ICalculoSalarialService calculoSalarialService) {
		this.personalRepository = personalRepository;
		this.calculoSalarialService = calculoSalarialService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<LiquidacionHaberesDTO> getLiquidacionesDeUnidadDelResponsable(String username) {
		Optional<UnidadVenta> unidad = obtenerUnidadDelResponsable(username);
		if (unidad.isEmpty()) {
			return List.of();
		}

		Festival festival = unidad.get().getFestivalAsistio();
		return personalRepository.findByUnidadVentaIdAndEliminadoFalse(unidad.get().getId()).stream()
				.filter(personal -> personal instanceof Cocinero || personal instanceof Cajero)
				.map(personal -> crearLiquidacion(personal, festival))
				.toList();
	}

	private Optional<UnidadVenta> obtenerUnidadDelResponsable(String username) {
		Personal personal = personalRepository.findByUserUsernameAndEliminadoFalse(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
		if (!(personal instanceof Responsable responsable)) {
			throw new AccessDeniedException("El usuario autenticado no es Responsable");
		}
		return Optional.ofNullable(responsable.getUnidadVenta());
	}

	private LiquidacionHaberesDTO crearLiquidacion(Personal personal, Festival festival) {
		TipoEmpleado tipo = personal instanceof Cocinero ? TipoEmpleado.COCINERO : TipoEmpleado.CAJERO;
		CategoriaCocina categoria = personal instanceof Cocinero cocinero
				? CategoriaCocina.desdePlus(cocinero.getPlus()).orElse(null)
				: null;

		if (festival == null) {
			return new LiquidacionHaberesDTO(personal.getId(), personal.getNombre(), personal.getApellido(), tipo,
					categoria, null, MOTIVO_SIN_FESTIVAL);
		}

		return calculoSalarialService.calcularSueldo(personal, festival)
				.map(resultado -> new LiquidacionHaberesDTO(personal.getId(), personal.getNombre(),
						personal.getApellido(), tipo, categoria,
						resultado.esCalculable() ? resultado.sueldo() : null,
						resultado.esCalculable() ? null : resultado.error()))
				.orElseGet(() -> new LiquidacionHaberesDTO(personal.getId(), personal.getNombre(),
						personal.getApellido(), tipo, categoria, null, MOTIVO_TIPO_NO_CONTEMPLADO));
	}
}
