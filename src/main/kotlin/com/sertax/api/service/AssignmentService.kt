package com.sertax.api.service // O tu paquete correspondiente

import com.sertax.api.model.Trip
import com.sertax.api.repository.TripRepository
// import com.sertax.api.service.ConfigurationService // <-- AÑADIDO: Importa tu servicio de configuración
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class AssignmentService(
    private val tripRepository: TripRepository,
    // <-- CORREGIDO: Inyectar el servicio de configuración
    private val configurationService: ConfigurationService
) {
    
    // Un mapa seguro para hilos (thread-safe) para llevar la cuenta de la rotación de asignaciones
    private val associationAssignmentState = ConcurrentHashMap<String, Long>()
    
    companion object {
        // <-- CORREGIDO: Definir la constante que faltaba
        private const val LAST_ASSOC_KEY = "LAST_ASSIGNED_ASSOCIATION_ID"
    }
    
    fun findNextAssociationForManualAssignment(associations: List<Association>): Association? {
        if (associations.isEmpty()) {
            return null
        }
        
        // Obtiene el ID de la última asociación a la que se le asignó un viaje
        val lastAssignedId = associationAssignmentState[LAST_ASSOC_KEY] ?: 0L
        
        // Encuentra el índice de la última asociación asignada
        val lastIndex = associations.indexOfFirst { it.associationId == lastAssignedId }
        
        // Determina el siguiente índice, volviendo al principio si llega al final de la lista
        val nextIndex = if (lastIndex == -1 || lastIndex >= associations.size - 1) 0 else lastIndex + 1
        
        val nextAssociation = associations[nextIndex]
        
        // Actualiza el estado con el nuevo ID de la última asociación asignada
        associationAssignmentState[LAST_ASSOC_KEY] = nextAssociation.associationId
        
        return nextAssociation
    }
    
    fun assignTripToNextAssociation(trip: Trip, associations: List<Association>) {
        val nextAssociation = findNextAssociationForManualAssignment(associations)
        
        if (nextAssociation != null) {
            // <-- CORREGIDO: Los errores de tipo en la línea 82 se resuelven aquí
            // Se modifica el objeto 'trip' y se guarda. El compilador ahora sabe que es de tipo Trip.
            trip.manualAssignmentAssociation = nextAssociation
            trip.status = TripStatus.PendingManualAssignment
            tripRepository.save(trip)
        } else {
            // Gestionar el caso en que no haya asociaciones disponibles
            // Quizás registrar un error o cambiar el estado del viaje
        }
    }
    
    // Ejemplo de cómo usar el servicio de configuración inyectado
    fun someOtherFunction() {
        // <-- CORREGIDO: Usar el servicio inyectado para obtener valores
        val timeout = configurationService.getConfigValue("assignment.timeout.seconds", "30").toLong()
        // ... usar el valor de timeout
    }
}

// NOTA: Necesitarás crear una clase `ConfigurationService` que provea el método `getConfigValue`.