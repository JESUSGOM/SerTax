package com.sertax.api.repository

import com.sertax.api.model.TaxiStop
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaxiStopRepository : JpaRepository<TaxiStop, Long> {
}