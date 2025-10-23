package com.sertax.api.repository

import com.sertax.api.model.Association // <-- AÑADIDO: Importar Association
import com.sertax.api.model.Trip
import com.sertax.api.model.TripStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query // <-- AÑADIDO: Importar Query
import org.springframework.stereotype.Repository

@Repository
interface TripRepository : JpaRepository<Trip, Long> {
    fun findByUserUserId(userId: Long): List<Trip>
    fun findByDriverDriverId(driverId: Long): List<Trip>
    
    /**
     * Devuelve una lista paginada de viajes con un estado específico,
     * ordenados por su fecha de finalización descendente.
     */
    fun findAllByStatusOrderByCompletionTimestampDesc(status: TripStatus, pageable: Pageable): List<Trip>
    
    /**
     * Busca todos los viajes que se encuentran en un estado específico.
     */
    fun findByStatus(status: TripStatus): List<Trip>
    
    /**
     * Busca viajes pendientes de asignación manual para una asociación específica.
     */
    fun findByStatusAndManualAssignmentAssociation(status: TripStatus, association: Association): List<Trip>
    
    // <-- AÑADIDO: Método principal para filtrar por asociación
    /**
     * Busca todos los viajes (sin importar su estado) cuyos conductores
     * pertenecen a una asociación específica.
     * Esta es la consulta que el controlador usará para que una asociación vea su historial completo.
     */
    @Query("SELECT t FROM Trip t WHERE t.driver.association.associationId = :associationid")
    fun findByDriverAssociationId(associationid: Long): List<Trip>
}