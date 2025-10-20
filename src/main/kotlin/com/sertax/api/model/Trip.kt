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

    // --- INICIO DE LA CORRECCIÓN ---
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // <-- ESTA ES LA CORRECCIÓN CLAVE
    @Column(name = "requestchannel", nullable = false)
    var requestChannel: RequestChannel,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // <-- CORREGIDO TAMBIÉN PARA EVITAR FUTUROS ERRORES
    @Column(name = "status", nullable = false)
    var status: TripStatus = TripStatus.Requested
    // --- FIN DE LA CORRECCIÓN ---
)

enum class RequestChannel {
    App, Web, WhatsApp
}

enum class TripStatus {
    Requested,
    Assigned,
    EnRoute,
    InProgress,
    Completed,
    Cancelled,
    NoShow
}