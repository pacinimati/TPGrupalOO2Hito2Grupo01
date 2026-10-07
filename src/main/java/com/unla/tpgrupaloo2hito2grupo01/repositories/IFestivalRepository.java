package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;

@Repository("festivalRepository")
public interface IFestivalRepository extends JpaRepository<Festival, Long> {

	boolean existsByNombre(String nombre);

	Optional<Festival> findByNombre(String nombre);
}
