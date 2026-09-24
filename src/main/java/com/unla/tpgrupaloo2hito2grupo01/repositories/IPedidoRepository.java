package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;

@Repository("pedidoRepository")
public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findByUnidadVentaId(long unidadVentaId);

	// Pedidos de las unidades de un festival, con sus items y platos (LAZY) en una sola consulta
	@Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.lstItems i LEFT JOIN FETCH i.plato "
			+ "WHERE p.unidadVenta.festivalAsistio.id = :festivalId")
	List<Pedido> findByFestivalConItems(@Param("festivalId") long festivalId);
}
