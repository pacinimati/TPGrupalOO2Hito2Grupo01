package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PedidoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.PedidoResumenDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Administrador;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.Pedido;
import com.unla.tpgrupaloo2hito2grupo01.entities.Plato;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPedidoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPlatoRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.PedidoService;

// Alta de pedidos sin base de datos: los repositorios se simulan con Mockito
class AltaPedidoTest {

	private static final String RESPONSABLE = "responsable";
	private static final String ADMIN = "admin";
	private static final LocalDate DENTRO_DEL_FESTIVAL = LocalDate.of(2027, 3, 13);

	private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
	private final IPlatoRepository platoRepository = mock(IPlatoRepository.class);
	private final IUnidadVentaRepository unidadVentaRepository = mock(IUnidadVentaRepository.class);
	private final IPersonalRepository personalRepository = mock(IPersonalRepository.class);
	private final IPedidoService service = new PedidoService(pedidoRepository, platoRepository,
			unidadVentaRepository, personalRepository);

	private final UnidadVenta propia = unidad(1, "Tacos");
	private final UnidadVenta ajena = unidad(2, "Pizzas");
	private final Plato taco = plato(10, 2000);
	private final Plato limonada = plato(20, 500);

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		Responsable responsable = new Responsable();
		responsable.setUnidadVenta(propia);
		when(personalRepository.findByUserUsernameAndEliminadoFalse(RESPONSABLE)).thenReturn(Optional.of(responsable));
		when(personalRepository.findByUserUsernameAndEliminadoFalse(ADMIN)).thenReturn(Optional.of(new Administrador()));
		when(unidadVentaRepository.findByIdConFestival(1)).thenReturn(Optional.of(propia));
		when(unidadVentaRepository.findByIdConFestival(2)).thenReturn(Optional.of(ajena));
		when(platoRepository.findAllById(any())).thenAnswer(inv -> List.of(taco, limonada).stream()
				.filter(p -> ((Collection<Long>) inv.getArgument(0)).contains(p.getId()))
				.toList());
	}

	@Test
	void registraUnItemPorPlatoConCantidadYCalculaElTotal() {
		PedidoFormDTO dto = pedido(1, DENTRO_DEL_FESTIVAL);
		dto.getCantidades().put(10L, 3);
		dto.getCantidades().put(20L, 2);
		dto.getCantidades().put(99L, 0); // en 0 no forma parte del pedido

		PedidoResumenDTO resumen = service.registrar(dto, RESPONSABLE);

		ArgumentCaptor<Pedido> guardado = ArgumentCaptor.forClass(Pedido.class);
		verify(pedidoRepository).save(guardado.capture());
		assertEquals(propia, guardado.getValue().getUnidadVenta());
		assertEquals(DENTRO_DEL_FESTIVAL, guardado.getValue().getFechaTransaccion());
		assertEquals(2, guardado.getValue().getLstItems().size());
		assertEquals(7000, resumen.total()); // 3 * 2000 + 2 * 500
	}

	@Test
	void elResponsableNoPuedeRegistrarEnUnaUnidadAjena() {
		PedidoFormDTO dto = pedido(2, DENTRO_DEL_FESTIVAL);
		dto.getCantidades().put(10L, 1);

		assertEquals(List.of("unidadVentaId"), campos(service.validarReglasDeNegocio(dto, RESPONSABLE)));
		assertThrows(IllegalArgumentException.class, () -> service.registrar(dto, RESPONSABLE));
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void elAdministradorPuedeRegistrarEnCualquierUnidad() {
		PedidoFormDTO dto = pedido(2, DENTRO_DEL_FESTIVAL);
		dto.getCantidades().put(10L, 1);

		assertTrue(service.validarReglasDeNegocio(dto, ADMIN).isEmpty());
	}

	@Test
	void exigeAlMenosUnPlatoYFechaDentroDelFestival() {
		PedidoFormDTO dto = pedido(1, LocalDate.of(2027, 3, 20));
		dto.getCantidades().put(10L, 0);

		assertEquals(List.of("fechaTransaccion", "cantidades"), campos(service.validarReglasDeNegocio(dto, RESPONSABLE)));
	}

	private List<String> campos(List<ErrorCampoDTO> errores) {
		return errores.stream().map(ErrorCampoDTO::campo).toList();
	}

	private PedidoFormDTO pedido(long unidadVentaId, LocalDate fecha) {
		PedidoFormDTO dto = new PedidoFormDTO();
		dto.setUnidadVentaId(unidadVentaId);
		dto.setFechaTransaccion(fecha);
		return dto;
	}

	private UnidadVenta unidad(long id, String nombre) {
		Festival festival = new Festival();
		festival.setNombre("Lollapalooza");
		festival.setFechaInicio(LocalDate.of(2027, 3, 12));
		festival.setFechaFin(LocalDate.of(2027, 3, 14));
		FoodTruck unidad = new FoodTruck();
		unidad.setId(id);
		unidad.setNombreComercial(nombre);
		unidad.setFestivalAsistio(festival);
		return unidad;
	}

	private Plato plato(long id, float precioVenta) {
		Plato plato = new Plato();
		plato.setId(id);
		plato.setPrecioVenta(precioVenta);
		return plato;
	}
}
