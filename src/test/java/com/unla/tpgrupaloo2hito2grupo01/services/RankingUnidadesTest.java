package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.unla.tpgrupaloo2hito2grupo01.dtos.RankingUnidadDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRoleRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.UnidadVentaService;

// Ranking de unidades sin base de datos: los repositorios se simulan con Mockito
class RankingUnidadesTest {

	private static final long FESTIVAL_ID = 1;

	private final IUnidadVentaRepository unidadVentaRepository = mock(IUnidadVentaRepository.class);
	private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
	private final IUnidadVentaService service = new UnidadVentaService(unidadVentaRepository,
			mock(IPersonalRepository.class), mock(IFestivalRepository.class), pedidoRepository,
			mock(IUserRepository.class), mock(IUserRoleRepository.class), null);

	@Test
	void ordenaDescendentePorRecaudacionSumandoLosPedidos() {
		UnidadVenta tacos = unidad(1, "Tacos");
		UnidadVenta pizzas = unidad(2, "Pizzas");
		UnidadVenta sinVentas = unidad(3, "Sin ventas");
		Plato taco = plato(2000);
		Plato pizza = plato(5000);

		when(unidadVentaRepository.findByFestivalConResponsable(FESTIVAL_ID))
				.thenReturn(List.of(tacos, pizzas, sinVentas));
		when(pedidoRepository.findByFestivalConItems(FESTIVAL_ID)).thenReturn(List.of(
				pedido(tacos, item(taco, 3)),                    // 6.000
				pedido(tacos, item(taco, 1), item(taco, 2)),     // 6.000
				pedido(pizzas, item(pizza, 3), item(taco, 1)))); // 17.000

		List<RankingUnidadDTO> ranking = service.getRankingPorRecaudacion(FESTIVAL_ID);

		assertEquals(List.of("Pizzas", "Tacos", "Sin ventas"),
				ranking.stream().map(RankingUnidadDTO::getNombreComercial).toList());
		assertEquals(17000, ranking.get(0).getRecaudacion());
		assertEquals(12000, ranking.get(1).getRecaudacion());
		assertEquals(2, ranking.get(1).getCantidadPedidos());
		assertEquals(0, ranking.get(2).getRecaudacion());
		assertEquals(0, ranking.get(2).getCantidadPedidos());
	}

	private UnidadVenta unidad(long id, String nombre) {
		FoodTruck unidad = new FoodTruck();
		unidad.setId(id);
		unidad.setNombreComercial(nombre);
		return unidad;
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

	private Pedido pedido(UnidadVenta unidad, ItemPedido... items) {
		Pedido pedido = new Pedido();
		pedido.setUnidadVenta(unidad);
		for (ItemPedido item : items) {
			item.setPedido(pedido);
			pedido.getLstItems().add(item);
		}
		return pedido;
	}
}
