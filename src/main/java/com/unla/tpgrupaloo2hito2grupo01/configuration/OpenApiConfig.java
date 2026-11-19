package com.unla.tpgrupaloo2hito2grupo01.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuración de Swagger (springdoc-openapi) para el Hito 3.
 * UI: /swagger-ui.html — Especificación JSON: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI epicentroGourmetOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Epicentro Gourmet - API REST")
						.description("Endpoints REST del sistema de gestión de festivales, unidades de venta, "
								+ "personal y pedidos. TP Grupal OO2 - UNLa 2026, Hito 3.")
						.version("v1"));
	}
}
