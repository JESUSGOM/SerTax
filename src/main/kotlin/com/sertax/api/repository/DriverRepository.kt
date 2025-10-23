package com.sertax.api.repository

import com.sertax.api.model.Driver
import com.sertax.api.model.Association
import com.sertax.api.model.DriverRealtimeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DriverRepository : JpaRepository<Driver, Long> {
    
    // <-- AÑADIDO: Método simple y directo para buscar por asociación
    /**
     * Busca todos los conductores que pertenecen a una asociación específica.
     * Utiliza la nueva relación directa: Driver -> Association.
     */
    fun findByAssociationAssociationid(associationid: Long): List<Driver>
    
    
    // <-- CORREGIDO: Tu método complejo, ahora usando la relación directa
    /**
     * Busca conductores activos y disponibles (Libre o En Parada) que pertenezcan a una asociación específica.
     * La ruta de la propiedad se ha corregido de `License_Association_...` a la directa `association_Associationid`.
     */
    fun findByIsActiveTrueAndAssociation_AssociationidAndDriverStatus_CurrentStatusIn(
        associationid: Long,
        statuses: List<DriverRealtimeStatus>
    ): List<Driver>
    
    
    // <-- SIN CAMBIOS: Este método ya era correcto para los administradores
    /**
     * Busca todos los conductores activos y disponibles (Libre o En Parada) de todo el sistema.
     * Útil para los perfiles de administrador municipal que necesitan ver a todos los conductores.
     */
    fun findByIsActiveTrueAndDriverStatus_CurrentStatusIn(
        statuses: List<DriverRealtimeStatus>
    ): List<Driver>
    
    /**
     * Busca todos los conductores que pertenecen a una asociación específica.
     */
    fun findByAssociation(association: Association): List<Driver>
}