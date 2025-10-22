package com.sertax.api.repository

import com.sertax.api.model.DriverRealtimeStatus
import com.sertax.api.model.DriverStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DriverStatusRepository : JpaRepository<DriverStatus, Long> {
    /**
     * Encuentra todos los registros de estado de conductor que coinciden con un estado específico.
     * Este método es utilizado por el FleetService para contar cuántos taxis hay en las paradas.
     */
    fun findByCurrentStatus(currentStatus: DriverRealtimeStatus): List<DriverStatus>
}