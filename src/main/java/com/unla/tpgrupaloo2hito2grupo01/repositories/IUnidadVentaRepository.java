package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;

@Repository("unidadVentaRepository")
public interface IUnidadVentaRepository extends JpaRepository<UnidadVenta, Long> {

	List<UnidadVenta> findByFestivalAsistioId(long festivalId);

	// Trae también el responsable (LAZY) para evitar una consulta por unidad
	@Query("SELECT u FROM UnidadVenta u LEFT JOIN FETCH u.responsable WHERE u.festivalAsistio.id = :festivalId ORDER BY u.nombreComercial")
	List<UnidadVenta> findByFestivalConResponsable(@Param("festivalId") long festivalId);

	// Todas las unidades con su festival (LAZY), ordenadas por festival y nombre comercial
	@Query("SELECT u FROM UnidadVenta u LEFT JOIN FETCH u.festivalAsistio f ORDER BY f.nombre, u.nombreComercial")
	List<UnidadVenta> findAllConFestival();

	@Query("SELECT u FROM UnidadVenta u LEFT JOIN FETCH u.festivalAsistio WHERE u.id = :id")
	Optional<UnidadVenta> findByIdConFestival(@Param("id") long id);

	boolean existsByCodigoUnico(String codigoUnico);
}
