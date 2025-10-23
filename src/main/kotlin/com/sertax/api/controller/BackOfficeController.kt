package com.sertax.api.controller

import com.sertax.api.security.CustomUserDetails // Importa tu clase de UserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import com.sertax.api.repository.DriverRepository
import com.sertax.api.repository.TripRepository

@Controller
class BackOfficeController(
	private val driverRepository: DriverRepository,
	private val tripRepository: TripRepository
) {
	
	@GetMapping("/dashboard/drivers")
	fun showDrivers(model: Model, @AuthenticationPrincipal userDetails: CustomUserDetails): String {
		val drivers = if (userDetails.role == "ASSOCIATION") {
			// Si el rol es ASSOCIATION, filtra por su ID
			val associationId = userDetails.associationId ?: throw IllegalStateException("Association user is missing an association ID.")
			driverRepository.findByAssociationAssociationid(associationId)
		} else {
			// Para otros roles (ADMIN, etc.), muestra todos los conductores
			driverRepository.findAll()
		}
		
		model.addAttribute("drivers", drivers)
		return "drivers_view" // El nombre de tu plantilla Thymeleaf
	}
	
	@GetMapping("/dashboard/trip-history")
	fun showTripHistory(model: Model, @AuthenticationPrincipal userDetails: CustomUserDetails): String {
		val trips = if (userDetails.role == "ASSOCIATION") {
			// Misma lógica de filtrado para los viajes
			val associationId = userDetails.associationId ?: throw IllegalStateException("Association user is missing an association ID.")
			tripRepository.findByDriverAssociationId(associationId)
		} else {
			tripRepository.findAll()
		}
		
		model.addAttribute("trips", trips)
		return "trip_history_view" // El nombre de tu plantilla Thymeleaf
	}
}