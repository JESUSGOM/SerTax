package com.sertax.api.model

import jakarta.persistence.*
import org.locationtech.jts.geom.Point

@Entity
@Table(name = "taxistops")
data class TaxiStop(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val stopId: Long = 0,

    @Column(nullable = false, length = 150)
    val name: String,

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    val location: Point
)