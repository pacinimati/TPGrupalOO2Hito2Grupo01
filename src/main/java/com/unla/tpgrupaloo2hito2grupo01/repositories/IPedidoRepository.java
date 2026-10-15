package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;

@Repository("pedidoRepository")
public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findByUnidadVentaId(long unidadVentaId);

	@Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.lstItems i LEFT JOIN FETCH i.plato "
			+ "WHERE p.unidadVenta.id = :unidadVentaId AND p.fechaTransaccion = :fecha")
	List<Pedido> findByUnidadVentaIdAndFechaConItems(@Param("unidadVentaId") long unidadVentaId,
			@Param("fecha") LocalDate fecha);

	// Pedidos de las unidades de un festival, con sus items y platos (LAZY) en una sola consulta
	@Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.lstItems i LEFT JOIN FETCH i.plato "
			+ "WHERE p.unidadVenta.festivalAsistio.id = :festivalId")
	List<Pedido> findByFestivalConItems(@Param("festivalId") long festivalId);

	// Pedidos de una unidad de un festival, con sus items y platos en una sola consulta
	@Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.lstItems i LEFT JOIN FETCH i.plato "
			+ "WHERE p.unidadVenta.id = :unidadVentaId AND p.unidadVenta.festivalAsistio.id = :festivalId")
	List<Pedido> findByUnidadVentaIdAndFestivalConItems(@Param("unidadVentaId") long unidadVentaId,
			@Param("festivalId") long festivalId);

	// Pedidos de una unidad en un período inclusivo, con sus items y platos en una sola consulta
	@Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.lstItems i LEFT JOIN FETCH i.plato "
			+ "WHERE p.unidadVenta.id = :unidadVentaId AND p.unidadVenta.festivalAsistio.id = :festivalId "
			+ "AND p.fechaTransaccion BETWEEN :desde AND :hasta")
	List<Pedido> findByUnidadVentaIdAndFestivalConItemsEntreFechas(@Param("unidadVentaId") long unidadVentaId,
			@Param("festivalId") long festivalId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
