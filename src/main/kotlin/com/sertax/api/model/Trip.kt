package com.sertax.api.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "trips")
data class Trip(
    // ... (campos existentes sin cambios) ...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tripid")
    val tripId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driverid")
    var driver: Driver?,

    @Column(name = "requesttimestamp", updatable = false)
    val requestTimestamp: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "assignmenttimestamp")
    var assignmentTimestamp: OffsetDateTime?,

    @Column(name = "pickuptimestamp")
    var pickupTimestamp: OffsetDateTime?,

    @Column(name = "completiontimestamp")
    var completionTimestamp: OffsetDateTime?,

    @Column(name = "pickupaddress", nullable = false, length = 255)
    var pickupAddress: String,

    @Column(name = "pickuplocation", columnDefinition = "geometry(Point, 4326)", nullable = false)
    var pickupLocation: Point,

    @Column(name = "destinationaddress", length = 255)
    var destinationAddress: String?,

    @Column(name = "destinationlocation", columnDefinition = "geometry(Point, 4326)")
    var destinationLocation: Point?,

    @Column(name = "estimatedcost", precision = 10, scale = 2)
    var estimatedCost: BigDecimal?,

    @Column(name = "finalcost", precision = 10, scale = 2)
    var finalCost: BigDecimal?,

    @Column(name = "numpassengers")
    var numPassengers: Int = 1,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "json")
    var options: Map<String, Any>?,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "requestchannel", nullable = false)
    var requestChannel: RequestChannel,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    var status: TripStatus = TripStatus.Requested
)

enum class RequestChannel {
    App, Web, WhatsApp
}

// --- ENUM MODIFICADO ---
enum class TripStatus {
    Requested,      // Usuario solicita
    Assigned,       // Conductor asignado, pendiente de aceptar
    EnRoute,        // Conductor aceptó y va a recoger
    InProgress,     // Conductor recogió al pasajero y el viaje ha comenzado
    Completed,      // Viaje finalizado
    Cancelled,      // Cancelado (por usuario o por no encontrar conductor)
    NoShow          // El usuario no apareció
}