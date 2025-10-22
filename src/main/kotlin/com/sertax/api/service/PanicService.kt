package com.sertax.api.service

import com.sertax.api.dto.panic.PanicRequestDto
import com.sertax.api.repository.DriverRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class PanicService(
    private val driverRepository: DriverRepository
    // En un futuro, se inyectaría un servicio de SMS o de alertas
) {
    companion object {
        private val LOGGER = Logger.getLogger(PanicService::class.java.name)
    }

    fun triggerPanicAlert(request: PanicRequestDto) {
        val driver = driverRepository.findByIdOrNull(request.driverId)
            ?: throw NoSuchElementException("Conductor con ID ${request.driverId} no encontrado.")

        // --- LÓGICA DE ALERTA ---
        // En una implementación real, aquí se conectaría con un servicio de SMS (como Twilio)
        // para enviar una alerta a un contacto de emergencia o a la central.
        // También podría enviar una notificación push a la asociación o al 112.

        val alertMessage = """
            !!! ALERTA DE PÁNICO !!!
            Conductor: ${driver.name} (ID: ${driver.driverId})
            Licencia: ${driver.license.licenseNumber}
            Ubicación: https://www.google.com/maps?q=${request.latitude},${request.longitude}
            Mensaje: ${request.message ?: "Sin mensaje adicional."}
        """.trimIndent()

        // Por ahora, simulamos la alerta imprimiéndola en la consola del servidor.
        LOGGER.severe(alertMessage)

        // Aquí podrías devolver un estado o simplemente confirmar la recepción.
    }
}