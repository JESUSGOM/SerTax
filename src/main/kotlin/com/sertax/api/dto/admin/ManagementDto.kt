package com.sertax.api.dto.admin

import com.sertax.api.model.DriverRole

// --- DTOs para Gestión de Usuarios (Pasajeros) ---

data class CreateUserRequestDto(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val password: String // La contraseña se envía en texto plano para que el backend la encripte
)

data class UpdateUserRequestDto(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val isActive: Boolean
)

// --- DTOs para Gestión de Licencias ---

data class CreateLicenseRequestDto(
    val licenseNumber: String,
    val associationId: Long?
)

data class UpdateLicenseRequestDto(
    val licenseNumber: String,
    val associationId: Long?
)


// --- DTOs para Gestión de Conductores ---

data class CreateDriverRequestDto(
    val name: String,
    val password: String,
    val role: DriverRole,
    val licenseId: Long,
    val isActive: Boolean
)

data class UpdateDriverRequestDto(
    val name: String,
    val role: DriverRole,
    val isActive: Boolean
)


// --- DTOs para Gestión de Vehículos ---

data class CreateVehicleRequestDto(
    val licensePlate: String,
    val licenseId: Long,
    val make: String?,
    val model: String?,
    val isPMRAdapted: Boolean,
    val allowsPets: Boolean
)

data class UpdateVehicleRequestDto(
    val licensePlate: String,
    val make: String?,
    val model: String?,
    val isPMRAdapted: Boolean,
    val allowsPets: Boolean
)

// --- DTO para la Configuración del Sistema ---

data class UpdateSystemConfigRequestDto(
    val configValue: String
)