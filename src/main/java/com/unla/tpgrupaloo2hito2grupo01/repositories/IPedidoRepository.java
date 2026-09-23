package com.unla.tpgrupaloo2hito2grupo01.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;

@Repository("pedidoRepository")
public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findByUnidadVentaId(long unidadVentaId);
}
