package com.unla.tpgrupaloo2hito2grupo01.services.implementation;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.unla.tpgrupaloo2hito2grupo01.entities.Degree;
import com.unla.tpgrupaloo2hito2grupo01.dtos.DegreeDTO;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IDegreeRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IDegreeService;

@Service("degreeService")
public class DegreeService implements IDegreeService {
	private static final Logger logger = LoggerFactory.getLogger(DegreeService.class);

	private final IDegreeRepository degreeRepository;

	private final ModelMapper modelMapper;

	public DegreeService(IDegreeRepository degreeRepository, ModelMapper modelMapper) {
		this.degreeRepository = degreeRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public List<Degree> getAll() {
		return degreeRepository.findAll();
	}

	@Override
	public void insertOrUpdate(DegreeDTO degreeDTO) {
		degreeRepository.save(modelMapper.map(degreeDTO, Degree.class));
	}

	@Override
	public boolean remove(int id) {
		try {
			degreeRepository.deleteById(id);
			return true;
		} catch (EmptyResultDataAccessException | DataIntegrityViolationException e) {
			logger.warn("Could not remove Degree with id {}", id, e);
			return false;
		} catch (DataAccessException e) {
			logger.error("Database error while removing Degree with id {}", id, e);
			return false;
		}
	}
}
