package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.CierreCaja;

@Repository("cierreCajaRepository")
public interface ICierreCajaRepository extends JpaRepository<CierreCaja, Long> {

	Optional<CierreCaja> findByUnidadVentaIdAndFecha(long unidadVentaId, LocalDate fecha);

	// Cierres según los filtros (los nulos no filtran), con cajero, unidad y festival (LAZY) en una sola consulta
	@Query("SELECT c FROM CierreCaja c JOIN FETCH c.cajero JOIN FETCH c.unidadVenta u "
			+ "LEFT JOIN FETCH u.festivalAsistio f "
			+ "WHERE (:festivalId IS NULL OR f.id = :festivalId) "
			+ "AND (:unidadVentaId IS NULL OR u.id = :unidadVentaId) "
			+ "AND (:cajeroId IS NULL OR c.cajero.id = :cajeroId) "
			+ "AND (:fechaDesde IS NULL OR c.fecha >= :fechaDesde) "
			+ "AND (:fechaHasta IS NULL OR c.fecha <= :fechaHasta) "
			+ "ORDER BY c.fecha DESC, u.nombreComercial")
	List<CierreCaja> filtrar(@Param("festivalId") Long festivalId, @Param("unidadVentaId") Long unidadVentaId,
			@Param("cajeroId") Long cajeroId, @Param("fechaDesde") LocalDate fechaDesde,
			@Param("fechaHasta") LocalDate fechaHasta);
}
