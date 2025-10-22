package com.sertax.api.repository

import com.sertax.api.model.Driver
import com.sertax.api.model.DriverRealtimeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DriverRepository : JpaRepository<Driver, Long> {

    /**
     * Busca conductores activos y disponibles (Libre o En Parada) que pertenezcan a una asociación específica.
     */
    fun findByIsActiveTrueAndLicense_Association_AssociationIdAndDriverStatus_CurrentStatusIn(
        associationId: Long,
        statuses: List<DriverRealtimeStatus>
    ): List<Driver>

    /**
     * Busca todos los conductores activos y disponibles (Libre o En Parada) del sistema.
     * Útil para los perfiles de administrador municipal.
     */
    fun findByIsActiveTrueAndDriverStatus_CurrentStatusIn(
        statuses: List<DriverRealtimeStatus>
    ): List<Driver>
}