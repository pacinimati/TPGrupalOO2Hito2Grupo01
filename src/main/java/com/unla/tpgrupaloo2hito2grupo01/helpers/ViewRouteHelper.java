package com.unla.tpgrupaloo2hito2grupo01.helpers;

public class ViewRouteHelper {
	/**** Views ****/
	//HOME
	public final static String INDEX = "home/index";
	public final static String HELLO = "home/hello";

	//DEGREE
	public final static String DEGREE_INDEX = "degree/index";
	public final static String DEGREE_FORM = "degree/form";
	public final static String DEGREE_NEW = "degree/new";

	//PERSON
	public final static String PERSON_INDEX = "person/index";
	public final static String PERSON_NEW = "person/new";
	public final static String PERSON_UPDATE = "person/update";
	public final static String PERSON_PARTIAL_VIEW = "person/partialPersonView";

	//USER
	public final static String USER_LOGIN = "user/login";
	public final static String USER_LOGOUT = "user/logout";

	//REGISTRO
	public final static String REGISTRO_UNIDAD = "registro/unidad";

	//ADMIN
	public final static String ADMIN_FESTIVALES = "admin/festivales";
	public final static String ADMIN_UNIDADES = "admin/unidades";
	public final static String ADMIN_RANKING = "admin/ranking";
	public final static String ADMIN_MAYORES_CANON = "admin/mayoresCanon";
	public final static String ADMIN_RECAUDACION = "admin/recaudacion";
	public final static String ADMIN_RENTABILIDAD_NETA = "admin/rentabilidadNeta";
	public final static String ADMIN_RENTABILIDAD_NETA_PERIODO = "admin/rentabilidadNetaPeriodo";

	//PEDIDO
	public final static String PEDIDO_NUEVO = "pedido/nuevo";

	//EMPLEADOS (Responsable)
	public final static String EMPLEADOS_LISTADO = "responsable/empleados";
	public final static String EMPLEADOS_FORM = "responsable/empleadoForm";
	public final static String RESPONSABLE_LIQUIDACIONES = "responsable/liquidaciones";
	public final static String RESPONSABLE_RENTABILIDAD_NETA = "responsable/rentabilidadNeta";
	public final static String RESPONSABLE_RENTABILIDAD_NETA_PERIODO = "responsable/rentabilidadNetaPeriodo";

	/**** Redirects ****/
	public final static String ROUTE = "/index";
	public final static String ADMIN_ROOT = "/admin/festivales";
	public final static String PEDIDO_ROOT = "/pedidos/nuevo";
	public final static String EMPLEADOS_ROOT = "/responsable/empleados";
	public final static String DEGREE_ROOT = "/degrees/";
	public final static String PERSON_ROOT = "/person";
}
