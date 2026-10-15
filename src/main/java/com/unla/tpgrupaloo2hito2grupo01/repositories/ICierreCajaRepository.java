package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.CierreCaja;

@Repository("cierreCajaRepository")
public interface ICierreCajaRepository extends JpaRepository<CierreCaja, Long> {

	Optional<CierreCaja> findByUnidadVentaIdAndFecha(long unidadVentaId, LocalDate fecha);
}
