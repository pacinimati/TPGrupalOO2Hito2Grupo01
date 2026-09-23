package com.unla.tpgrupaloo2hito2grupo01.services;

import java.util.List;
import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;

public interface IFestivalService {

	List<Festival> getAll();

	Optional<Festival> findById(long id);
}
