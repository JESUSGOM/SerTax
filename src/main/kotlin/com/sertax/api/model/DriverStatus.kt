package com.sertax.api.model

import jakarta.persistence.*
import org.locationtech.jts.geom.Point
import java.time.OffsetDateTime

@Entity
@Table(name = "driverstatus")
data class DriverStatus(
    @Id
    val driverId: Long,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "driverId")
    val driver: Driver,

    @Enumerated(EnumType.STRING)
    var currentStatus: DriverRealtimeStatus, // Esta línea ahora funcionará

    @Column(columnDefinition = "geometry(Point, 4326)")
    var currentLocation: Point? = null,

    var lastUpdate: OffsetDateTime
)

/**
 * Enum que define los posibles estados de un conductor en tiempo real.
 * Esta es la definición que faltaba.
 */
enum class DriverRealtimeStatus {
    Free,         // Libre y disponible
    Busy,         // Ocupado en un viaje
    OnPickup,     // En camino a recoger a un pasajero
    AtStop,       // Libre en una parada
    OutOfService  // Fuera de servicio
}