package com.sertax.api.model // O tu paquete correspondiente

import jakarta.persistence.*

@Entity
@Table(name = "associations")
data class Association(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "associationid")
    val associationId: Long = 0, // <-- CORREGIDO: 'associationid' a 'associationId'
    
    @Column(name = "name", nullable = false, length = 100)
    val name: String,
    
    @Column(name = "contactinfo", columnDefinition = "text")
    val contactinfo: String? = null
)