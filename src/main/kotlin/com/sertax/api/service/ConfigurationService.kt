package com.sertax.api.service

import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

/**
 * Servicio para gestionar el acceso a las propiedades de configuración de la aplicación.
 * Centraliza la lectura de valores desde 'application.properties' o 'application.yml'.
 */
@Service
class ConfigurationService(
	// Inyectamos el objeto Environment de Spring, que contiene todas las propiedades
	private val environment: Environment
) {
	
	/**
	 * Obtiene el valor de una propiedad de configuración a partir de su clave.
	 *
	 * @param key La clave de la propiedad (ej: "assignment.timeout.seconds").
	 * @param defaultValue Un valor por defecto que se devolverá si la clave no se encuentra.
	 * @return El valor de la propiedad como un String, o el valor por defecto.
	 */
	fun getConfigValue(key: String, defaultValue: String): String {
		return environment.getProperty(key, defaultValue)
	}
}