package com.unla.tpgrupaloo2hito2grupo01.services.implementation;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.unla.tpgrupaloo2hito2grupo01.entities.Person;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PersonDTO;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IPersonService;


@Service("personService")
public class PersonService implements IPersonService {
	private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

	private final IPersonRepository personRepository;

	private final ModelMapper modelMapper;

	public PersonService(IPersonRepository personRepository, ModelMapper modelMapper) {
		this.personRepository = personRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public List<Person> getAll() {
		return personRepository.findAll();
	}

	@Override
	public Person insertOrUpdate(Person person) {
		return personRepository.save(person);
	}

	@Override
	public boolean remove(int id) {
		try {
			personRepository.deleteById(id);
			return true;
		} catch (EmptyResultDataAccessException | DataIntegrityViolationException e) {
			logger.warn("Could not remove Person with id {}", id, e);
			return false;
		} catch (DataAccessException e) {
			logger.error("Database error while removing Person with id {}", id, e);
			return false;
		}
	}

	@Override
	public Optional<Person> findById(int id) {
		return personRepository.findById(id);
	}

	@Override
	public Person findByName(String name) {
		return personRepository.findByName(name).orElseThrow(
				() -> new IllegalArgumentException("ERROR: Person not found with name: " + name)
		);
	}

	@Override
	public List<PersonDTO> findByDegreeName(String degreeName) {
		return personRepository.findByDegreeName(degreeName)
				.stream()
				.map(person -> modelMapper.map(person, PersonDTO.class))
				.toList();
	}
}
