package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.unla.tpgrupaloo2hito2grupo01.dtos.PlatoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.ItemPedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.exceptions.UnidadNoPerteneceFestivalException;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IFestivalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.PlatoEstrellaService;

class PlatoEstrellaServiceTest {

	private static final long FESTIVAL_ID = 1;
	private static final long UNIDAD_ID = 2;

	private final IFestivalRepository festivalRepository = mock(IFestivalRepository.class);
	private final IUnidadVentaRepository unidadVentaRepository = mock(IUnidadVentaRepository.class);
	private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
	private final IPersonalRepository personalRepository = mock(IPersonalRepository.class);
	private final IPlatoEstrellaService service = new PlatoEstrellaService(festivalRepository,
			unidadVentaRepository, pedidoRepository, personalRepository);

	@Test
	void devuelveElPlatoQueApareceEnMasPedidosAunqueOtroVendaMasUnidades() {
		Plato choripan = plato(10, "Choripán");
		Plato cerveza = plato(11, "Cerveza artesanal");
		prepararPedidos(
				pedido(item(choripan, 1), item(cerveza, 20)),
				pedido(item(choripan, 2)),
				pedido(item(choripan, 1), item(cerveza, 30)));

		PlatoDTO estrella = service.getPlatoEstrella(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertEquals(10, estrella.id());
		assertEquals("Choripán", estrella.nombre());
		assertEquals(3, estrella.cantidadPedidos());
		assertEquals(4, estrella.unidadesVendidas());
	}

	@Test
	void unPlatoRepetidoEnElMismoPedidoCuentaUnaSolaVez() {
		Plato tacos = plato(10, "Tacos x3");
		Plato limonada = plato(11, "Limonada");
		prepararPedidos(
				pedido(item(tacos, 1), item(tacos, 1), item(tacos, 1)),
				pedido(item(limonada, 1)),
				pedido(item(limonada, 1)));

		PlatoDTO estrella = service.getPlatoEstrella(FESTIVAL_ID, UNIDAD_ID).orElseThrow();

		assertEquals("Limonada", estrella.nombre());
		assertEquals(2, estrella.cantidadPedidos());
	}

	@Test
	void anteIgualCantidadDePedidosGanaElQueVendioMasUnidades() {
		Plato pizza = plato(10, "Pizza muzzarella");
		Plato fugazza = plato(11, "Fugazzeta");
		prepararPedidos(
				pedido(item(pizza, 5), item(fugazza, 2)),
				pedido(item(pizza, 1), item(fugazza, 2)));

		assertEquals("Pizza muzzarella", service.getPlatoEstrella(FESTIVAL_ID, UNIDAD_ID).orElseThrow().nombre());
	}

	@Test
	void siEmpatanEnPedidosYUnidadesGanaElPrimeroPorNombre() {
		Plato gaseosa = plato(10, "Gaseosa");
		Plato agua = plato(11, "Agua");
		prepararPedidos(pedido(item(gaseosa, 2), item(agua, 2)));

		assertEquals("Agua", service.getPlatoEstrella(FESTIVAL_ID, UNIDAD_ID).orElseThrow().nombre());
	}

	@Test
	void devuelveVacioCuandoLaUnidadNoTienePedidos() {
		prepararPedidos();

		assertTrue(service.getPlatoEstrella(FESTIVAL_ID, UNIDAD_ID).isEmpty());
	}

	@Test
	void informaCuandoElFestivalNoExiste() {
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.empty());

		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> service.getPlatoEstrella(FESTIVAL_ID, UNIDAD_ID));

		assertEquals("El festival seleccionado no existe", error.getMessage());
		verify(pedidoRepository, never()).findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID);
	}

	@Test
	void rechazaUnaUnidadDeOtroFestivalAntesDeConsultarPedidos() {
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival(FESTIVAL_ID)));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID))
				.thenReturn(Optional.of(unidad(festival(FESTIVAL_ID + 1))));

		assertThrows(UnidadNoPerteneceFestivalException.class,
				() -> service.getPlatoEstrella(FESTIVAL_ID, UNIDAD_ID));
		verify(pedidoRepository, never()).findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID);
	}

	@Test
	void usaLaUnidadYElFestivalDelResponsable() {
		Responsable responsable = new Responsable();
		responsable.setUnidadVenta(unidad(festival(FESTIVAL_ID)));
		when(personalRepository.findByUserUsernameAndEliminadoFalse("resp")).thenReturn(Optional.of(responsable));
		Plato choripan = plato(10, "Choripán");
		prepararPedidos(pedido(item(choripan, 3)));

		PlatoDTO estrella = service.getPlatoEstrellaDeUnidadDelResponsable("resp").orElseThrow();

		assertEquals("Choripán", estrella.nombre());
		verify(pedidoRepository).findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID);
	}

	@Test
	void devuelveVacioSiElResponsableNoTieneUnidadOFestival() {
		Responsable sinUnidad = new Responsable();
		Responsable sinFestival = new Responsable();
		sinFestival.setUnidadVenta(unidad(null));
		when(personalRepository.findByUserUsernameAndEliminadoFalse("sinUnidad")).thenReturn(Optional.of(sinUnidad));
		when(personalRepository.findByUserUsernameAndEliminadoFalse("sinFestival"))
				.thenReturn(Optional.of(sinFestival));

		assertTrue(service.getPlatoEstrellaDeUnidadDelResponsable("sinUnidad").isEmpty());
		assertTrue(service.getPlatoEstrellaDeUnidadDelResponsable("sinFestival").isEmpty());
	}

	@Test
	void rechazaAUnUsuarioQueNoEsResponsable() {
		when(personalRepository.findByUserUsernameAndEliminadoFalse("cajero")).thenReturn(Optional.of(new Cajero()));

		assertThrows(AccessDeniedException.class, () -> service.getPlatoEstrellaDeUnidadDelResponsable("cajero"));
	}

	private void prepararPedidos(Pedido... pedidos) {
		Festival festival = festival(FESTIVAL_ID);
		when(festivalRepository.findById(FESTIVAL_ID)).thenReturn(Optional.of(festival));
		when(unidadVentaRepository.findByIdConFestival(UNIDAD_ID)).thenReturn(Optional.of(unidad(festival)));
		when(pedidoRepository.findByUnidadVentaIdAndFestivalConItems(UNIDAD_ID, FESTIVAL_ID))
				.thenReturn(List.of(pedidos));
	}

	private Festival festival(long id) {
		Festival festival = new Festival();
		festival.setId(id);
		festival.setNombre("Festival " + id);
		return festival;
	}

	private FoodTruck unidad(Festival festival) {
		FoodTruck unidad = new FoodTruck();
		unidad.setId(UNIDAD_ID);
		unidad.setNombreComercial("Unidad " + UNIDAD_ID);
		unidad.setFestivalAsistio(festival);
		return unidad;
	}

	private Plato plato(long id, String nombre) {
		Plato plato = new Plato();
		plato.setId(id);
		plato.setNombre(nombre);
		plato.setPrecioVenta(100);
		plato.setCostoProduccion(40);
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
		for (ItemPedido item : items) {
			item.setPedido(pedido);
			pedido.getLstItems().add(item);
		}
		return pedido;
	}
}
