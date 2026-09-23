package com.unla.tpgrupaloo2hito2grupo01.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;

@Repository("platoRepository")
public interface IPlatoRepository extends JpaRepository<Plato, Long> {
}
