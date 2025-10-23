package com.sertax.api.repository

import com.sertax.api.model.Incident
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IncidentRepository : JpaRepository<Incident, Long> {
	fun findByTrip_Driver_License_Association()
}