package com.sertax.api.dto.admin

// DTO para mostrar una lista resumida de conductores en el BackOffice
data class DriverListDto(
    val driverId: Long,
    val name: String,
    val role: String,
    val licenseNumber: String,
    val isActive: Boolean
)

// DTO para mostrar la vista de detalle completa de un conductor
data class DriverDetailDto(
    val driverId: Long,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val license: LicenseInfoDto,
    val vehicle: VehicleInfoDto?
) {
    // Sub-DTO con información de la licencia
    data class LicenseInfoDto(
        val licenseId: Long,
        val licenseNumber: String,
        val associationName: String?
    )
    // Sub-DTO con información del vehículo asociado
    data class VehicleInfoDto(
        val vehicleId: Long,
        val make: String?,
        val model: String?,
        val licensePlate: String,
        val isPMRAdapted: Boolean,
        val allowsPets: Boolean
    )
}