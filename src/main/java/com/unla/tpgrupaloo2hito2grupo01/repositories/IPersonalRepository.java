package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;

@Repository("personalRepository")
public interface IPersonalRepository extends JpaRepository<Personal, Long> {

	// Personal activo (no eliminado lógicamente) de una unidad de venta
	List<Personal> findByUnidadVentaIdAndEliminadoFalse(long unidadVentaId);
}
