package com.sertax.api.service

import com.sertax.api.dto.feedback.CreateIncidentRequest
import com.sertax.api.model.Incident
import com.sertax.api.model.ReporterType
import com.sertax.api.repository.IncidentRepository
import com.sertax.api.repository.TripRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IncidentService(
    private val incidentRepository: IncidentRepository,
    private val tripRepository: TripRepository
) {
    /**
     * Registra una nueva incidencia reportada por un usuario o conductor.
     */
    @Transactional
    fun createIncident(request: CreateIncidentRequest): Incident {
        val trip = request.tripId?.let {
            tripRepository.findById(it).orElse(null)
        }

        val newIncident = Incident(
            trip = trip,
            reporterId = request.reporterId,
            reporterType = ReporterType.valueOf(request.reporterType),
            type = request.type,
            description = request.description
            // El estado por defecto es 'Open'
        )

        return incidentRepository.save(newIncident)
    }
}