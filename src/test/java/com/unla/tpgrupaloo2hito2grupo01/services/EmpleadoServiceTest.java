package com.unla.tpgrupaloo2hito2grupo01.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFiltroDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.EmpleadoFormDTO;
import com.unla.tpgrupaloo2hito2grupo01.dtos.ErrorCampoDTO;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cajero;
import com.unla.tpgrupaloo2hito2grupo01.entities.CategoriaCocina;
import com.unla.tpgrupaloo2hito2grupo01.entities.Cocinero;
import com.unla.tpgrupaloo2hito2grupo01.entities.Festival;
import com.unla.tpgrupaloo2hito2grupo01.entities.FoodTruck;
import com.unla.tpgrupaloo2hito2grupo01.entities.Personal;
import com.unla.tpgrupaloo2hito2grupo01.entities.Responsable;
import com.unla.tpgrupaloo2hito2grupo01.entities.TipoEmpleado;
import com.unla.tpgrupaloo2hito2grupo01.entities.TurnoTrabajo;
import com.unla.tpgrupaloo2hito2grupo01.entities.UnidadVenta;
import com.unla.tpgrupaloo2hito2grupo01.entities.User;
import com.unla.tpgrupaloo2hito2grupo01.entities.UserRole;
import com.unla.tpgrupaloo2hito2grupo01.exceptions.EnvioMailException;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IPersonalRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUnidadVentaRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRepository;
import com.unla.tpgrupaloo2hito2grupo01.repositories.IUserRoleRepository;
import com.unla.tpgrupaloo2hito2grupo01.services.implementation.EmpleadoService;

// ABM de empleados sin base de datos: los repositorios, el encoder y el envío de mail se simulan con Mockito
class EmpleadoServiceTest {

	private static final String RESPONSABLE = "resp.pepe";
	private static final long UNIDAD_ID = 10;
	private static final long EMPLEADO_ID = 50;

	private final IPersonalRepository personalRepository = mock(IPersonalRepository.class);
	private final IUnidadVentaRepository unidadVentaRepository = mock(IUnidadVentaRepository.class);
	private final IUserRepository userRepository = mock(IUserRepository.class);
	private final IUserRoleRepository userRoleRepository = mock(IUserRoleRepository.class);
	private final IMailService mailService = mock(IMailService.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final IEmpleadoService service = new EmpleadoService(personalRepository, unidadVentaRepository,
			userRepository, userRoleRepository, mailService, passwordEncoder);

	private UnidadVenta unidad;

	@BeforeEach
	void unidadConResponsable() {
		Festival festival = new Festival();
		festival.setSueldoBase(100000);
		unidad = new FoodTruck();
		unidad.setId(UNIDAD_ID);
		unidad.setFestivalAsistio(festival);
		Responsable responsable = new Responsable();
		responsable.setUnidadVenta(unidad);
		when(personalRepository.findByUserUsernameAndEliminadoFalse(RESPONSABLE)).thenReturn(Optional.of(responsable));
		when(passwordEncoder.encode(anyString())).thenAnswer(i -> "hash:" + i.getArgument(0));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
	}

	@Test
	void altaDeCocineroGuardaElPlusDeLaCategoriaYElSueldoBaseDelFestival() {
		EmpleadoFormDTO dto = formCocinero();

		service.altaEmpleado(dto, RESPONSABLE);

		ArgumentCaptor<Personal> guardado = ArgumentCaptor.forClass(Personal.class);
		verify(personalRepository).save(guardado.capture());
		Cocinero cocinero = assertInstanceOf(Cocinero.class, guardado.getValue());
		assertEquals(50000, cocinero.getPlus());
		assertEquals(100000, cocinero.getSueldoBase());
		assertEquals("Lavado", cocinero.getEspecialidadCulinaria());
		assertEquals(unidad, cocinero.getUnidadVenta());
		assertFalse(cocinero.isEliminado());
	}

	@Test
	void altaCreaElUsuarioConElEmailYLeEnviaLaContraseniaGenerada() {
		EmpleadoFormDTO dto = formCajero();
		dto.setEmail("  Julieta.Paz@Mail.com ");

		service.altaEmpleado(dto, RESPONSABLE);

		ArgumentCaptor<User> usuario = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(usuario.capture());
		assertEquals("julieta.paz@mail.com", usuario.getValue().getUsername());
		assertTrue(usuario.getValue().isEnabled());

		ArgumentCaptor<UserRole> rol = ArgumentCaptor.forClass(UserRole.class);
		verify(userRoleRepository).save(rol.capture());
		assertEquals("ROLE_EMPLEADO", rol.getValue().getRole());

		ArgumentCaptor<String> contrasenia = ArgumentCaptor.forClass(String.class);
		verify(mailService).enviarCredenciales(eq("julieta.paz@mail.com"), eq("Julieta"), contrasenia.capture());
		assertEquals(10, contrasenia.getValue().length());
		// Se guarda cifrada la misma contraseña que se envía por mail
		assertEquals("hash:" + contrasenia.getValue(), usuario.getValue().getPassword());
	}

	@Test
	void altaGeneraUnaContraseniaDistintaCadaVez() {
		service.altaEmpleado(formCajero(), RESPONSABLE);
		service.altaEmpleado(formCajero(), RESPONSABLE);

		ArgumentCaptor<String> contrasenias = ArgumentCaptor.forClass(String.class);
		verify(mailService, org.mockito.Mockito.times(2)).enviarCredenciales(anyString(), anyString(), contrasenias.capture());
		assertTrue(!contrasenias.getAllValues().get(0).equals(contrasenias.getAllValues().get(1)));
	}

	@Test
	void siFallaElMailLaExcepcionSePropagaParaRevertirElAlta() {
		doThrow(new EnvioMailException("sin servidor", null)).when(mailService).enviarCredenciales(anyString(), anyString(), anyString());

		assertThrows(EnvioMailException.class, () -> service.altaEmpleado(formCajero(), RESPONSABLE));
	}

	@Test
	void validaUnAltaCorrectaSinErrores() {
		assertTrue(service.validarReglasDeNegocio(formCajero(), RESPONSABLE).isEmpty());
	}

	@Test
	void rechazaDniYEmailYaRegistrados() {
		Cajero otro = new Cajero();
		otro.setId(99);
		otro.setUnidadVenta(unidad);
		when(personalRepository.findByDni(30111222)).thenReturn(List.of(otro));
		when(userRepository.existsByUsername("julieta.paz@mail.com")).thenReturn(true);

		List<ErrorCampoDTO> errores = service.validarReglasDeNegocio(formCajero(), RESPONSABLE);

		assertEquals(List.of("dni", "email"), errores.stream().map(ErrorCampoDTO::campo).toList());
		assertEquals("duplicado", errores.get(0).codigo());
	}

	@Test
	void siElDniEsDeUnEmpleadoEliminadoDeLaUnidadIndicaQueHayQueReactivarlo() {
		Cajero eliminado = new Cajero();
		eliminado.setId(99);
		eliminado.setEliminado(true);
		eliminado.setUnidadVenta(unidad);
		when(personalRepository.findByDni(30111222)).thenReturn(List.of(eliminado));

		List<ErrorCampoDTO> errores = service.validarReglasDeNegocio(formCajero(), RESPONSABLE);

		assertEquals(1, errores.size());
		assertEquals("eliminado", errores.get(0).codigo());
	}

	@Test
	void rechazaAlMenorDeEdadYAlIngresoAnteriorALosDieciocho() {
		EmpleadoFormDTO menor = formCajero();
		menor.setFechaNacimiento(LocalDate.now().minusYears(17));
		assertEquals("menor", service.validarReglasDeNegocio(menor, RESPONSABLE).get(0).codigo());

		EmpleadoFormDTO ingresoTemprano = formCajero();
		ingresoTemprano.setFechaNacimiento(LocalDate.now().minusYears(30));
		ingresoTemprano.setFechaIngreso(LocalDate.now().minusYears(30).plusYears(Personal.EDAD_MINIMA).minusDays(1));
		assertEquals("anteriorMayoriaEdad", service.validarReglasDeNegocio(ingresoTemprano, RESPONSABLE).get(0).codigo());
	}

	@Test
	void exigeLosDatosPropiosDelTipoDeEmpleado() {
		EmpleadoFormDTO cocinero = formCocinero();
		cocinero.setCategoriaCocina(null);
		EmpleadoFormDTO cajero = formCajero();
		cajero.setTurnoTrabajo(null);

		assertEquals("categoriaCocina", service.validarReglasDeNegocio(cocinero, RESPONSABLE).get(0).campo());
		assertEquals("turnoTrabajo", service.validarReglasDeNegocio(cajero, RESPONSABLE).get(0).campo());
	}

	@Test
	void alModificarElPropioDniNoCuentaComoDuplicado() {
		Cajero actual = cajeroDeLaUnidad(false);
		when(personalRepository.findEmpleadoDeUnidad(EMPLEADO_ID, UNIDAD_ID)).thenReturn(Optional.of(actual));
		when(personalRepository.findByDni(30111222)).thenReturn(List.of(actual));
		EmpleadoFormDTO dto = formCajero();
		dto.setId(EMPLEADO_ID);

		assertTrue(service.validarReglasDeNegocio(dto, RESPONSABLE).isEmpty());
	}

	@Test
	void modificarActualizaLosDatosSinCambiarElUsuario() {
		Cajero actual = cajeroDeLaUnidad(false);
		when(personalRepository.findEmpleadoDeUnidad(EMPLEADO_ID, UNIDAD_ID)).thenReturn(Optional.of(actual));
		EmpleadoFormDTO dto = formCajero();
		dto.setId(EMPLEADO_ID);
		dto.setNombre("Julia");
		dto.setTurnoTrabajo(TurnoTrabajo.NOCHE);
		dto.setEmail("otro@mail.com");

		assertTrue(service.modificarEmpleado(dto, RESPONSABLE));

		assertEquals("Julia", actual.getNombre());
		assertEquals(TurnoTrabajo.NOCHE, actual.getTurnoTrabajo());
		assertEquals("julieta.paz@mail.com", actual.getUser().getUsername());
		verify(userRepository, never()).save(any());
	}

	@Test
	void unResponsableNoPuedeModificarNiEliminarEmpleadosDeOtraUnidad() {
		when(personalRepository.findEmpleadoDeUnidad(EMPLEADO_ID, UNIDAD_ID)).thenReturn(Optional.empty());
		EmpleadoFormDTO dto = formCajero();
		dto.setId(EMPLEADO_ID);

		assertFalse(service.modificarEmpleado(dto, RESPONSABLE));
		assertFalse(service.eliminarEmpleado(EMPLEADO_ID, RESPONSABLE));
		assertFalse(service.reactivarEmpleado(EMPLEADO_ID, RESPONSABLE));
		verify(personalRepository, never()).save(any());
	}

	@Test
	void eliminarEsBajaLogicaYDeshabilitaElUsuario() {
		Cajero actual = cajeroDeLaUnidad(false);
		when(personalRepository.findEmpleadoDeUnidad(EMPLEADO_ID, UNIDAD_ID)).thenReturn(Optional.of(actual));

		assertTrue(service.eliminarEmpleado(EMPLEADO_ID, RESPONSABLE));

		assertTrue(actual.isEliminado());
		assertFalse(actual.getUser().isEnabled());
		verify(personalRepository, never()).delete(any(Personal.class));
	}

	@Test
	void noSeEliminaDosVecesAlMismoEmpleado() {
		when(personalRepository.findEmpleadoDeUnidad(EMPLEADO_ID, UNIDAD_ID)).thenReturn(Optional.of(cajeroDeLaUnidad(true)));

		assertFalse(service.eliminarEmpleado(EMPLEADO_ID, RESPONSABLE));
	}

	@Test
	void reactivarVuelveAHabilitarAlEmpleadoYLeEnviaUnaContraseniaNueva() {
		Cajero eliminado = cajeroDeLaUnidad(true);
		eliminado.getUser().setEnabled(false);
		when(personalRepository.findEmpleadoDeUnidad(EMPLEADO_ID, UNIDAD_ID)).thenReturn(Optional.of(eliminado));

		assertTrue(service.reactivarEmpleado(EMPLEADO_ID, RESPONSABLE));

		assertFalse(eliminado.isEliminado());
		assertTrue(eliminado.getUser().isEnabled());
		ArgumentCaptor<String> contrasenia = ArgumentCaptor.forClass(String.class);
		verify(mailService).enviarCredenciales(eq("julieta.paz@mail.com"), eq("Julieta"), contrasenia.capture());
		assertEquals("hash:" + contrasenia.getValue(), eliminado.getUser().getPassword());
	}

	@Test
	void noSeReactivaAUnEmpleadoQueEstaActivo() {
		when(personalRepository.findEmpleadoDeUnidad(EMPLEADO_ID, UNIDAD_ID)).thenReturn(Optional.of(cajeroDeLaUnidad(false)));

		assertFalse(service.reactivarEmpleado(EMPLEADO_ID, RESPONSABLE));
		verify(mailService, never()).enviarCredenciales(anyString(), anyString(), anyString());
	}

	@Test
	void filtrarDevuelveLosEmpleadosDeLaUnidadYAplicaElFiltroPorTipo() {
		Cocinero cocinero = new Cocinero();
		cocinero.setId(1);
		cocinero.setNombre("Hernán");
		cocinero.setApellido("Duarte");
		cocinero.setPlus(CategoriaCocina.COCINERO.getPlus());
		cocinero.setEspecialidadCulinaria("Parrillero");
		cocinero.setUser(new User("hernan@mail.com", "x", true));
		Cajero cajero = cajeroDeLaUnidad(false);
		when(personalRepository.filtrarEmpleados(UNIDAD_ID, false, "", "", null)).thenReturn(List.of(cocinero, cajero));

		EmpleadoFiltroDTO todos = new EmpleadoFiltroDTO();
		EmpleadoFiltroDTO soloCajeros = new EmpleadoFiltroDTO();
		soloCajeros.setTipo(TipoEmpleado.CAJERO);

		List<EmpleadoDTO> sinFiltroDeTipo = service.filtrar(RESPONSABLE, todos);
		assertEquals(2, sinFiltroDeTipo.size());
		assertEquals("Cocinero · Parrillero", sinFiltroDeTipo.get(0).detalle());
		assertEquals("hernan@mail.com", sinFiltroDeTipo.get(0).email());
		assertEquals(List.of(TipoEmpleado.CAJERO), service.filtrar(RESPONSABLE, soloCajeros).stream().map(EmpleadoDTO::tipo).toList());
	}

	@Test
	void unUsuarioQueNoEsResponsableNoVeEmpleados() {
		Cajero cajero = new Cajero();
		cajero.setUnidadVenta(unidad);
		when(personalRepository.findByUserUsernameAndEliminadoFalse("julieta.paz@mail.com")).thenReturn(Optional.of(cajero));

		assertTrue(service.filtrar("julieta.paz@mail.com", new EmpleadoFiltroDTO()).isEmpty());
		assertTrue(service.getUnidadDelResponsable("julieta.paz@mail.com").isEmpty());
	}

	private Cajero cajeroDeLaUnidad(boolean eliminado) {
		Cajero cajero = new Cajero();
		cajero.setId(EMPLEADO_ID);
		cajero.setNombre("Julieta");
		cajero.setApellido("Paz");
		cajero.setDni(30111222);
		cajero.setFechaNacimiento(LocalDate.of(1999, 7, 21));
		cajero.setFechaIngreso(LocalDate.of(2025, 3, 3));
		cajero.setTurnoTrabajo(TurnoTrabajo.MANIANA);
		cajero.setUnidadVenta(unidad);
		cajero.setEliminado(eliminado);
		cajero.setUser(new User("julieta.paz@mail.com", "hash:vieja", !eliminado));
		return cajero;
	}

	private EmpleadoFormDTO formCajero() {
		EmpleadoFormDTO dto = formBase();
		dto.setTipo(TipoEmpleado.CAJERO);
		dto.setTurnoTrabajo(TurnoTrabajo.MANIANA);
		return dto;
	}

	private EmpleadoFormDTO formCocinero() {
		EmpleadoFormDTO dto = formBase();
		dto.setTipo(TipoEmpleado.COCINERO);
		dto.setCategoriaCocina(CategoriaCocina.LAVAPLATOS);
		dto.setEspecialidadCulinaria("Lavado");
		return dto;
	}

	private EmpleadoFormDTO formBase() {
		EmpleadoFormDTO dto = new EmpleadoFormDTO();
		dto.setNombre("Julieta");
		dto.setApellido("Paz");
		dto.setDni(30111222);
		dto.setFechaNacimiento(LocalDate.of(1999, 7, 21));
		dto.setFechaIngreso(LocalDate.of(2025, 3, 3));
		dto.setEmail("julieta.paz@mail.com");
		return dto;
	}
}
