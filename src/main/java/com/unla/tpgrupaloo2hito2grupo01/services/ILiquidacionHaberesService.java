package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;

import com.unla.tpgrupaloo2hito2grupo01.dtos.LiquidacionHaberesDTO;

public interface ILiquidacionHaberesService {

	List<LiquidacionHaberesDTO> getLiquidacionesDeUnidadDelResponsable(String username);
}
