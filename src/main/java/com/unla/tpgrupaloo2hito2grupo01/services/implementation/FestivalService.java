package com.unla.tpgrupaloo2hito2grupo01.services.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.IFestivalService;

@Service("festivalService")
public class FestivalService implements IFestivalService {

	private final IFestivalRepository festivalRepository;

	public FestivalService(IFestivalRepository festivalRepository) {
		this.festivalRepository = festivalRepository;
	}

	@Override
	public List<Festival> getAll() {
		return festivalRepository.findAll();
	}

	@Override
	public Optional<Festival> findById(long id) {
		return festivalRepository.findById(id);
	}
}
