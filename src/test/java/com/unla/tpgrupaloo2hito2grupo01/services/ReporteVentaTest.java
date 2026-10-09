package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ReporteVentaDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPlatoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.PedidoService;

class ReporteVentaTest {

	private static final long FESTIVAL_ID = 1;

	private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
	private final IPedidoService service = new PedidoService(pedidoRepository, mock(IPlatoRepository.class),
			mock(IUnidadVentaRepository.class), mock(IPersonalRepository.class));

	@Test
	void sumaLaRecaudacionDeTodosLosPedidosDelFestival() {
		Plato taco = plato(2000);
		Plato pizza = plato(5000);
		when(pedidoRepository.findByFestivalConItems(FESTIVAL_ID)).thenReturn(List.of(
				pedido(item(taco, 3), item(pizza, 1)), // 11.000
				pedido(item(pizza, 2), item(taco, 1)))); // 12.000

		ReporteVentaDTO reporte = service.getReporteRecaudacion(FESTIVAL_ID);

		assertEquals(23000, reporte.recaudacionTotal());
	}

	@Test
	void devuelveRecaudacionCeroCuandoElFestivalNoTieneVentas() {
		when(pedidoRepository.findByFestivalConItems(FESTIVAL_ID)).thenReturn(List.of());

		ReporteVentaDTO reporte = service.getReporteRecaudacion(FESTIVAL_ID);

		assertEquals(0, reporte.recaudacionTotal());
	}

	private Plato plato(float precioVenta) {
		Plato plato = new Plato();
		plato.setPrecioVenta(precioVenta);
		return plato;
	}

	private ItemPedido item(Plato plato, int cantidad) {
		ItemPedido item = new ItemPedido();
		item.setPlato(plato);
		item.setCantidad(cantidad);
		return item;
	}

	private Pedido pedido(ItemPedido... items) {
		Pedido pedido = new Pedido();
		pedido.setUnidadVenta(new FoodTruck());
		for (ItemPedido item : items) {
			item.setPedido(pedido);
			pedido.getLstItems().add(item);
		}
		return pedido;
	}
}
