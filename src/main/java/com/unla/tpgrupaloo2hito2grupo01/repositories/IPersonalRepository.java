package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;

@Repository("personalRepository")
public interface IPersonalRepository extends JpaRepository<Personal, Long> {

	// Personal activo (no eliminado lógicamente) de una unidad de venta
	List<Personal> findByUnidadVentaIdAndEliminadoFalse(long unidadVentaId);

	// Personal activo asociado al usuario con el que se inició sesión
	Optional<Personal> findByUserUsernameAndEliminadoFalse(String username);

	// Empleados (cocina y cajeros, no el responsable) de una unidad, activos o dados de baja según "eliminado".
	// Los filtros de texto vacíos no filtran y un dni nulo tampoco; trae el usuario (LAZY) para mostrar el email
	@Query("SELECT p FROM Personal p LEFT JOIN FETCH p.user "
			+ "WHERE p.unidadVenta.id = :unidadVentaId AND p.eliminado = :eliminado "
			+ "AND TYPE(p) IN (Cocinero, Cajero) "
			+ "AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) "
			+ "AND LOWER(p.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')) "
			+ "AND (:dni IS NULL OR p.dni = :dni) "
			+ "ORDER BY p.apellido, p.nombre")
	List<Personal> filtrarEmpleados(@Param("unidadVentaId") long unidadVentaId, @Param("eliminado") boolean eliminado,
			@Param("nombre") String nombre, @Param("apellido") String apellido, @Param("dni") Integer dni);

	// Empleado de una unidad (el responsable solo gestiona los de la suya), esté o no eliminado
	@Query("SELECT p FROM Personal p LEFT JOIN FETCH p.user WHERE p.id = :id AND p.unidadVenta.id = :unidadVentaId "
			+ "AND TYPE(p) IN (Cocinero, Cajero)")
	Optional<Personal> findEmpleadoDeUnidad(@Param("id") long id, @Param("unidadVentaId") long unidadVentaId);

	// El dni no es único en la base: puede haber más de una persona con el mismo
	List<Personal> findByDni(int dni);
}
