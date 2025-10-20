package com.sertax.api.repository

import com.sertax.api.model.Association
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AssociationRepository : JpaRepository<Association, Long> {
}