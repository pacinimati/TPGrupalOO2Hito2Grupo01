package com.unla.tpgrupaloo2hito2grupo01.services;
import java.util.List;
import java.util.Optional;

import com.unla.tpgrupaloo2hito2grupo01.entities.Person;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PersonDTO;

public interface IPersonService {

	List<Person> getAll();

	Optional<Person> findById(int id);

	Person findByName(String name);

	Person insertOrUpdate(Person person);

	boolean remove(int id);

	List<PersonDTO> findByDegreeName(String degreeName);
}

