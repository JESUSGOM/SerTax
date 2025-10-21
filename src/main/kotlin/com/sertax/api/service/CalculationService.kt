package com.sertax.api.service

import com.sertax.api.dto.trip.TripEstimateResponse
import com.sertax.api.repository.SystemConfigRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CalculationService(
    private val systemConfigRepository: SystemConfigRepository
) {
    private val restTemplate = RestTemplate()

    // Deberás añadir esto a tu application.properties
    @Value("\${routing.api.key:}") // Añadimos un valor por defecto vacío
    private lateinit var apiKey: String

    fun estimateTrip(startLat: Double, startLon: Double, endLat: Double, endLon: Double): TripEstimateResponse {
        // 1. OBTENER DATOS DE RUTA (Usando OSRM como ejemplo gratuito)
        val url = "http://router.project-osrm.org/route/v1/driving/$startLon,$startLat;$endLon,$endLat?overview=false"
        val osrmResponse = restTemplate.getForObject(url, Map::class.java)
            ?: throw IllegalStateException("No se pudo obtener la ruta desde el servicio externo.")
        val route = (osrmResponse["routes"] as List<Map<String, Any>>).first()
        val distanceMeters = (route["distance"] as Number).toInt()
        val durationSeconds = (route["duration"] as Number).toInt()

        // 2. OBTENER TARIFAS DESDE LA BASE DE DATOS
        val baseFare = getConfigValueAsBigDecimal("FARE_BASE", "2.50")
        val pricePerKm = getConfigValueAsBigDecimal("FARE_PER_KM", "1.15")
        val pricePerMinute = getConfigValueAsBigDecimal("FARE_PER_MINUTE", "0.20")

        // 3. CALCULAR COSTE
        val distanceKm = distanceMeters.toBigDecimal().divide(BigDecimal(1000))
        val durationMinutes = durationSeconds.toBigDecimal().divide(BigDecimal(60))

        val cost = baseFare + (distanceKm * pricePerKm) + (durationMinutes * pricePerMinute)

        return TripEstimateResponse(
            estimatedCost = cost.setScale(2, RoundingMode.HALF_UP),
            estimatedDurationMinutes = durationMinutes.toInt(),
            distanceMeters = distanceMeters
        )
    }

    private fun getConfigValueAsBigDecimal(key: String, defaultValue: String): BigDecimal {
        val config = systemConfigRepository.findById(key).orElse(null)
        return BigDecimal(config?.configValue ?: defaultValue)
    }
}