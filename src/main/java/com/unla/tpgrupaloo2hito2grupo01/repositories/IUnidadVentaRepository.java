package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;

@Repository("unidadVentaRepository")
public interface IUnidadVentaRepository extends JpaRepository<UnidadVenta, Long> {

	List<UnidadVenta> findByFestivalAsistioId(long festivalId);
}
