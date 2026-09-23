package com.unla.tpgrupaloo2hito2grupo01.services;
import java.util.List;

import com.unla.tpgrupaloo2hito2grupo01.entities.Degree;
import com.unla.tpgrupaloo2hito2grupo01.dtos.DegreeDTO;


public interface IDegreeService {

	List<Degree> getAll();

	void insertOrUpdate(DegreeDTO degreeDTO);

	boolean remove(int id);
}

