package com.sertax.api.controller

import com.sertax.api.dto.admin.*
import com.sertax.api.model.AdminRole
import com.sertax.api.model.DriverRole
import com.sertax.api.model.IncidentStatus
import com.sertax.api.repository.TripRepository
import com.sertax.api.service.AdminDataService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException // Necesario para capturar el error
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal // Necesario para obtener el usuario logueado
import java.util.NoSuchElementException // Necesario para capturar errores 404

@Controller
@RequestMapping("/admin")
class AdminWebController(
    private val adminDataService: AdminDataService,
    private val tripRepository: TripRepository // Asegúrate de que está inyectado
) {
    
    // --- LOGIN ---
    @GetMapping("/login")
    fun showLoginPage(
        @RequestParam(required = false) error: String?,
        @RequestParam(required = false) logout: String?,
        model: Model
    ): String {
        if (error != null) model.addAttribute("errorMessage", "Usuario o contraseña incorrectos.")
        if (logout != null) model.addAttribute("logoutMessage", "Has cerrado sesión correctamente.")
        return "admin/login" // Corresponde a templates/admin/login.html
    }
    
    // --- DASHBOARD ---
    @GetMapping("/dashboard")
    fun showDashboardPage(model: Model, principal: Principal): String {
        // El dashboard es visible para todos los roles logueados
        return handleAdminGetAction(principal, null, model, "admin/dashboard") {
            model.addAttribute("dashboardStats", adminDataService.getDashboardStats())
            // Pasamos el nombre del admin para que el servicio pueda filtrar por asociación
            model.addAttribute("pendingTrips", adminDataService.getPendingManualTrips(principal.name))
            model.addAttribute("pageTitle", "Dashboard")
        }
    }
    
    // --- GESTIÓN DE USUARIOS ---
    @GetMapping("/users") // Corregido: Paréntesis añadido
    fun showUsersPage(model: Model, principal: Principal): String {
        // Solo AdminMunicipal puede ver usuarios
        return handleAdminGetAction(principal, AdminRole.AdminMunicipal, model, "admin/users") {
            model.addAttribute("users", adminDataService.getAllUsers()) // getAllUsers no necesita principal.name
            model.addAttribute("pageTitle", "Gestión de Usuarios")
        }
    }
    @GetMapping("/users/new")
    fun showCreateUserForm(model: Model, principal: Principal): String {
        // Solo AdminMunicipal puede crear usuarios
        return handleAdminGetAction(principal, AdminRole.AdminMunicipal, model, "admin/user-form") {
            model.addAttribute("userDto", CreateUserRequestDto("", "", "", ""))
            model.addAttribute("pageTitle", "Crear Nuevo Usuario")
            model.addAttribute("formAction", "/admin/users/save")
            model.addAttribute("isEdit", false) // Indica que es formulario de creación
        }
    }
    @PostMapping("/users/save")
    fun saveUser(@ModelAttribute("userDto") userDto: CreateUserRequestDto, redirectAttributes: RedirectAttributes, principal: Principal): String {
        return handleAdminAction(principal, AdminRole.AdminMunicipal, redirectAttributes, "redirect:/admin/users") {
            adminDataService.createUser(userDto, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Usuario creado con éxito.")
        }
    }
    @GetMapping("/users/edit/{id}")
    fun showEditUserForm(@PathVariable id: Long, model: Model, principal: Principal): String {
        // Solo AdminMunicipal puede editar
        return handleAdminGetAction(principal, AdminRole.AdminMunicipal, model, "admin/user-form") {
            // Necesitamos obtener el usuario completo para el DTO
            val user = adminDataService.getAllUsers().find { u -> u.userId == id } ?: throw NoSuchElementException("Usuario no encontrado") // Corregido 'it' y lambda
            val userDto = UpdateUserRequestDto(user.name, user.email, user.phoneNumber, user.isActive)
            model.addAttribute("userId", id)
            model.addAttribute("userDto", userDto) // Usa el mismo DTO pero con datos cargados
            model.addAttribute("pageTitle", "Editar Usuario")
            model.addAttribute("formAction", "/admin/users/update/$id")
            model.addAttribute("isEdit", true) // Indica que es formulario de edición
        }
    }
    @PostMapping("/users/update/{id}")
    fun updateUser(@PathVariable id: Long, @ModelAttribute("userDto") userDto: UpdateUserRequestDto, redirectAttributes: RedirectAttributes, principal: Principal): String {
        return handleAdminAction(principal, AdminRole.AdminMunicipal, redirectAttributes, "redirect:/admin/users") {
            adminDataService.updateUser(id, userDto, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado con éxito.")
        }
    }
    
    // --- GESTIÓN DE LICENCIAS ---
    @GetMapping("/licenses")
    fun showLicensesPage(model: Model, principal: Principal): String {
        // Todos los roles logueados pueden ver licencias (filtradas si es asociación)
        return handleAdminGetAction(principal, null, model, "admin/licenses") {
            model.addAttribute("licenses", adminDataService.getAllLicenses(principal.name)) // Pasar principal.name
            model.addAttribute("pageTitle", "Gestión de Licencias")
        }
    }
    @GetMapping("/licenses/new")
    fun showCreateLicenseForm(model: Model, principal: Principal): String {
        // Solo AdminMunicipal puede crear
        return handleAdminGetAction(principal, AdminRole.AdminMunicipal, model, "admin/license-form") {
            model.addAttribute("licenseDto", CreateLicenseRequestDto("", null))
            model.addAttribute("associations", adminDataService.getAllAssociations())
            model.addAttribute("pageTitle", "Crear Nueva Licencia")
            model.addAttribute("formAction", "/admin/licenses/save")
            model.addAttribute("isEdit", false)
        }
    }
    @GetMapping("/licenses/edit/{id}")
    fun showEditLicenseForm(@PathVariable id: Long, model: Model, principal: Principal): String {
        // Solo AdminMunicipal puede editar
        return handleAdminGetAction(principal, AdminRole.AdminMunicipal, model, "admin/license-form") {
            val license = adminDataService.getLicenseById(id, principal.name) // Pasar principal.name
            val licenseDto = UpdateLicenseRequestDto(license.licenseNumber, license.association?.associationId)
            model.addAttribute("licenseId", id); model.addAttribute("licenseDto", licenseDto)
            model.addAttribute("associations", adminDataService.getAllAssociations())
            model.addAttribute("pageTitle", "Editar Licencia"); model.addAttribute("formAction", "/admin/licenses/update/$id")
            model.addAttribute("isEdit", true)
        }
    }
    @PostMapping("/licenses/save")
    fun saveLicense(@ModelAttribute("licenseDto") licenseDto: CreateLicenseRequestDto, redirectAttributes: RedirectAttributes, principal: Principal): String {
        return handleAdminAction(principal, AdminRole.AdminMunicipal, redirectAttributes, "redirect:/admin/licenses") {
            adminDataService.createLicense(licenseDto, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Licencia creada con éxito.")
        }
    }
    @PostMapping("/licenses/update/{id}")
    fun updateLicense(@PathVariable id: Long, @ModelAttribute("licenseDto") licenseDto: UpdateLicenseRequestDto, redirectAttributes: RedirectAttributes, principal: Principal): String {
        return handleAdminAction(principal, AdminRole.AdminMunicipal, redirectAttributes, "redirect:/admin/licenses") {
            adminDataService.updateLicense(id, licenseDto, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Licencia actualizada con éxito.")
        }
    }
    
    // --- GESTIÓN DE CONDUCTORES ---
    @GetMapping("/drivers")
    fun showDriversPage(model: Model, principal: Principal): String {
        // Todos los roles logueados pueden ver conductores (filtrados si es asociación)
        return handleAdminGetAction(principal, null, model, "admin/drivers") {
            model.addAttribute("drivers", adminDataService.getAllDrivers(principal.name)) // Pasar principal.name
            model.addAttribute("pageTitle", "Gestión de Conductores")
        }
    }
    @GetMapping("/drivers/new")
    fun showCreateDriverForm(model: Model, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden crear
        return handleAdminGetAction(principal, null, model, "admin/driver-form") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede crear conductores.")
            model.addAttribute("driverDto", CreateDriverRequestDto("", "", DriverRole.Owner, 0L, true))
            model.addAttribute("licenses", adminDataService.getAllLicenses(principal.name)) // Licencias filtradas
            model.addAttribute("roles", DriverRole.entries) // Corregido: Usar .entries
            model.addAttribute("pageTitle", "Crear Nuevo Conductor"); model.addAttribute("formAction", "/admin/drivers/save")
            model.addAttribute("isEdit", false)
        }
    }
    @GetMapping("/drivers/edit/{id}")
    fun showEditDriverForm(@PathVariable id: Long, model: Model, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden editar
        return handleAdminGetAction(principal, null, model, "admin/driver-form") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede editar conductores.")
            val driver = adminDataService.getDriverById(id, principal.name) // Pasar principal.name
            val driverDto = UpdateDriverRequestDto(driver.name, driver.role, driver.isActive)
            model.addAttribute("driverId", id); model.addAttribute("driverDto", driverDto)
            model.addAttribute("currentLicense", driver.license); model.addAttribute("roles", DriverRole.entries) // Corregido: Usar .entries
            model.addAttribute("pageTitle", "Editar Conductor"); model.addAttribute("formAction", "/admin/drivers/update/$id")
            model.addAttribute("isEdit", true)
        }
    }
    @PostMapping("/drivers/save")
    fun saveDriver(@ModelAttribute("driverDto") driverDto: CreateDriverRequestDto, redirectAttributes: RedirectAttributes, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden guardar
        return handleAdminAction(principal, null, redirectAttributes, "redirect:/admin/drivers") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede crear conductores.")
            adminDataService.createDriver(driverDto, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Conductor creado con éxito.")
        }
    }
    @PostMapping("/drivers/update/{id}")
    fun updateDriver(@PathVariable id: Long, @ModelAttribute("driverDto") driverDto: UpdateDriverRequestDto, redirectAttributes: RedirectAttributes, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden actualizar
        return handleAdminAction(principal, null, redirectAttributes, "redirect:/admin/drivers") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede editar conductores.")
            adminDataService.updateDriver(id, driverDto, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Conductor actualizado con éxito.")
        }
    }
    
    // --- GESTIÓN DE VEHÍCULOS ---
    @GetMapping("/vehicles")
    fun showVehiclesPage(model: Model, principal: Principal): String {
        // Todos los roles pueden ver vehículos (filtrados si es asociación)
        return handleAdminGetAction(principal, null, model, "admin/vehicles") {
            model.addAttribute("vehicles", adminDataService.getAllVehicles(principal.name)) // Pasar principal.name
            model.addAttribute("pageTitle", "Gestión de Vehículos")
        }
    }
    @GetMapping("/vehicles/new")
    fun showCreateVehicleForm(model: Model, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden crear
        return handleAdminGetAction(principal, null, model, "admin/vehicle-form") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede crear vehículos.")
            model.addAttribute("vehicleDto", CreateVehicleRequestDto("", 0L, null, null, false, false))
            model.addAttribute("licenses", adminDataService.getAllLicenses(principal.name)) // Licencias filtradas
            model.addAttribute("pageTitle", "Registrar Nuevo Vehículo"); model.addAttribute("formAction", "/admin/vehicles/save")
            model.addAttribute("isEdit", false)
        }
    }
    @GetMapping("/vehicles/edit/{id}")
    fun showEditVehicleForm(@PathVariable id: Long, model: Model, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden editar
        return handleAdminGetAction(principal, null, model, "admin/vehicle-form") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede editar vehículos.")
            val vehicle = adminDataService.getVehicleById(id, principal.name) // Pasar principal.name
            val vehicleDto = UpdateVehicleRequestDto(vehicle.licensePlate, vehicle.make, vehicle.model, vehicle.isPMRAdapted, vehicle.allowsPets)
            model.addAttribute("vehicleId", id); model.addAttribute("vehicleDto", vehicleDto)
            model.addAttribute("currentLicense", vehicle.license)
            model.addAttribute("pageTitle", "Editar Vehículo"); model.addAttribute("formAction", "/admin/vehicles/update/$id")
            model.addAttribute("isEdit", true)
        }
    }
    @PostMapping("/vehicles/save")
    fun saveVehicle(@ModelAttribute("vehicleDto") vehicleDto: CreateVehicleRequestDto, redirectAttributes: RedirectAttributes, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden guardar
        return handleAdminAction(principal, null, redirectAttributes, "redirect:/admin/vehicles") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede crear vehículos.")
            adminDataService.createVehicle(vehicleDto, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Vehículo registrado con éxito.")
        }
    }
    @PostMapping("/vehicles/update/{id}")
    fun updateVehicle(@PathVariable id: Long, @ModelAttribute("vehicleDto") vehicleDto: UpdateVehicleRequestDto, redirectAttributes: RedirectAttributes, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden actualizar
        return handleAdminAction(principal, null, redirectAttributes, "redirect:/admin/vehicles") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede editar vehículos.")
            adminDataService.updateVehicle(id, vehicleDto, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Vehículo actualizado con éxito.")
        }
    }
    
    // --- HISTORIAL DE VIAJES ---
    @GetMapping("/trips")
    fun showTripsPage(model: Model, principal: Principal): String {
        // Todos los roles pueden ver el historial (filtrado si es asociación)
        return handleAdminGetAction(principal, null, model, "admin/trips") {
            model.addAttribute("trips", adminDataService.getAllTrips(principal.name)) // Pasar principal.name
            model.addAttribute("pageTitle", "Historial de Viajes")
        }
    }
    
    // --- GESTIÓN DE INCIDENCIAS ---
    @GetMapping("/incidents")
    fun showIncidentsPage(model: Model, principal: Principal): String {
        // Todos los roles pueden ver incidencias (filtradas si es asociación)
        return handleAdminGetAction(principal, null, model, "admin/incidents") {
            model.addAttribute("incidents", adminDataService.getAllIncidents(principal.name)) // Pasar principal.name
            model.addAttribute("pageTitle", "Gestión de Incidencias")
        }
    }
    @GetMapping("/incidents/{id}")
    fun showIncidentDetailPage(@PathVariable id: Long, model: Model, principal: Principal): String {
        // Todos los roles pueden ver detalles (filtrados si es asociación)
        return handleAdminGetAction(principal, null, model, "admin/incident-detail") {
            model.addAttribute("incident", adminDataService.getIncidentDetails(id, principal.name)) // Pasar principal.name
            model.addAttribute("statuses", IncidentStatus.entries) // Corregido: Usar .entries
            model.addAttribute("pageTitle", "Detalle de Incidencia")
        }
    }
    @PostMapping("/incidents/update-status/{id}")
    fun updateIncidentStatus(@PathVariable id: Long, @RequestParam("status") newStatus: IncidentStatus, redirectAttributes: RedirectAttributes, principal: Principal): String {
        // AdminMunicipal o Asociacion pueden gestionar
        return handleAdminAction(principal, null, redirectAttributes, "redirect:/admin/incidents/$id") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede gestionar incidencias.")
            adminDataService.updateIncidentStatus(id, newStatus, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Estado de la incidencia actualizado con éxito.")
        }
    }
    
    // --- CONFIGURACIÓN ---
    @GetMapping("/config")
    fun showConfigPage(model: Model, principal: Principal): String {
        // Solo AdminMunicipal
        return handleAdminGetAction(principal, AdminRole.AdminMunicipal, model, "admin/config") {
            model.addAttribute("configs", adminDataService.getAllSystemConfigs(principal.name)) // Pasar principal.name
            model.addAttribute("pageTitle", "Configuración del Sistema")
        }
    }
    @PostMapping("/config/update")
    fun updateConfig(@RequestParam("configKey") key: String, @RequestParam("configValue") value: String, redirectAttributes: RedirectAttributes, principal: Principal): String {
        // Solo AdminMunicipal
        return handleAdminAction(principal, AdminRole.AdminMunicipal, redirectAttributes, "redirect:/admin/config") {
            val request = UpdateSystemConfigRequestDto(value)
            adminDataService.updateSystemConfig(key, request, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Parámetro '$key' actualizado con éxito.")
        }
    }
    
    // --- INFORMES ---
    @GetMapping("/reports")
    fun showReportsPage(model: Model, principal: Principal): String {
        // Todos los roles pueden ver informes
        return handleAdminGetAction(principal, null, model, "admin/reports") {
            model.addAttribute("ratingStats", adminDataService.getRatingStats()) // getRatingStats no necesita principal.name
            model.addAttribute("pageTitle", "Informes y Estadísticas")
        }
    }
    
    // --- ASIGNACIÓN MANUAL ---
    @GetMapping("/trips/assign/{tripId}")
    fun showManualAssignForm(@PathVariable tripId: Long, model: Model, principal: Principal): String {
        // AdminMunicipal o Asociacion
        return handleAdminGetAction(principal, null, model, "admin/trip-assign-manual") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede asignar viajes.")
            val trip = tripRepository.findByIdOrNull(tripId) ?: throw NoSuchElementException("Viaje no encontrado")
            // Valida si el admin puede ver este viaje pendiente (reforzando la lógica del service)
            val admin = adminDataService.getAdmin(principal.name) // Corregido acceso a getAdmin
            if(admin.role == AdminRole.Asociacion && trip.manualAssignmentAssociation?.associationId != admin.association?.associationId) {
                throw AccessDeniedException("Este viaje no está asignado a tu asociación.")
            }
            val availableDrivers = adminDataService.getAvailableDriversForAssociation(principal.name) // Pasar principal.name
            model.addAttribute("trip", trip)
            model.addAttribute("drivers", availableDrivers)
            model.addAttribute("pageTitle", "Asignación Manual de Viaje")
        }
    }
    
    @PostMapping("/trips/assign")
    fun processManualAssign(@RequestParam("tripId") tripId: Long, @RequestParam("driverId") driverId: Long, principal: Principal, redirectAttributes: RedirectAttributes): String {
        // AdminMunicipal o Asociacion
        return handleAdminAction(principal, null, redirectAttributes, "redirect:/admin/dashboard") {
            if(getAdminRole(principal) == AdminRole.GestorMunicipal) throw AccessDeniedException("Gestor no puede asignar viajes.")
            adminDataService.assignDriverManually(tripId, driverId, principal.name) // Pasar principal.name
            redirectAttributes.addFlashAttribute("successMessage", "Taxi asignado correctamente al viaje #$tripId.")
        }
    }
    
    // --- Helper Methods ---
    
    /** Obtiene el rol del administrador logueado. Devuelve null si no se encuentra. */
    private fun getAdminRole(principal: Principal): AdminRole? {
        // Usamos Non-Transactional getAdmin para evitar problemas de proxy
        return try { adminDataService.getAdmin(principal.name).role } catch (e: Exception) { null }
    }
    
    /**
     * Helper genérico para manejar peticiones GET del BackOffice con validación de rol.
     * Captura excepciones comunes y redirige a páginas de error apropiadas.
     * @param principal El usuario autenticado.
     * @param requiredRole El rol mínimo requerido para VER (null si cualquier rol logueado es suficiente). AdminMunicipal y GestorMunicipal siempre tienen acceso de lectura.
     * @param model El modelo para pasar datos a la vista.
     * @param successView La vista a renderizar si todo va bien.
     * @param action La lógica específica del endpoint a ejecutar.
     * @return El nombre de la vista o la redirección.
     */
    private fun handleAdminGetAction(principal: Principal, requiredRole: AdminRole?, model: Model, successView: String, action: () -> Unit): String {
        try {
            val userRole = getAdminRole(principal) ?: throw AccessDeniedException("Usuario no encontrado o sin rol.")
            val isAdmin = userRole == AdminRole.AdminMunicipal
            val isGestor = userRole == AdminRole.GestorMunicipal
            
            // Verifica si tiene permiso para VER (Admin y Gestor siempre pueden)
            val hasPermission = when {
                isAdmin || isGestor -> true // Admin y Gestor pueden ver todo
                requiredRole == null && userRole == AdminRole.Asociacion -> true // Acceso general permitido para Asociación si no se especifica rol mínimo
                requiredRole == AdminRole.Asociacion && userRole == AdminRole.Asociacion -> true // Rol específico Asociación
                requiredRole == AdminRole.AdminMunicipal && isAdmin -> true // Si requiere Admin y lo es
                else -> false // Si no es Municipal y no cumple el rol de Asociación (si se requiere) o si el rol requerido es Admin y no lo es
            }
            
            if (!hasPermission) {
                throw AccessDeniedException("Permiso denegado para ver esta sección con el rol $userRole.")
            }
            
            action() // Ejecuta la lógica específica del endpoint (que puede lanzar sus propias AccessDeniedException si el filtrado falla)
            return successView
        } catch (e: AccessDeniedException) {
            model.addAttribute("pageTitle", "Acceso Denegado") // Título para la página de error
            model.addAttribute("errorMessage", e.message ?: "No tienes los permisos necesarios para acceder a esta página.")
            return "admin/access-denied" // Página de Acceso Denegado
        } catch (e: NoSuchElementException){
            model.addAttribute("pageTitle", "Error - No encontrado") // Título para la página de error
            model.addAttribute("errorMessage", e.message ?: "El recurso solicitado no fue encontrado.")
            return "admin/error" // Página genérica de error
        } catch (e: Exception) {
            model.addAttribute("pageTitle", "Error Inesperado") // Título para la página de error
            model.addAttribute("errorMessage", "Error inesperado del servidor: ${e.message}")
            // e.printStackTrace() // Considera quitar esto en producción
            return "admin/error" // Página genérica de error
        }
    }
    
    /**
     * Helper genérico para manejar acciones POST/PUT/DELETE del BackOffice con validación de rol.
     * @param principal El usuario autenticado.
     * @param requiredRole El rol mínimo requerido para la acción (null si AdminMunicipal y Asociacion pueden). AdminMunicipal siempre tiene permiso.
     * @param redirectAttributes Para pasar mensajes flash.
     * @param redirectUrl La URL a la que redirigir después de la acción.
     * @param action La lógica específica del endpoint a ejecutar.
     * @return La redirección.
     */
    private fun handleAdminAction(principal: Principal, requiredRole: AdminRole?, redirectAttributes: RedirectAttributes, redirectUrl: String, action: () -> Unit): String {
        try {
            val userRole = getAdminRole(principal) ?: throw AccessDeniedException("Usuario no encontrado o sin rol.")
            val isAdmin = userRole == AdminRole.AdminMunicipal
            
            // Verifica si tiene permiso para la ACCIÓN (Gestor nunca puede)
            val hasPermission = when {
                isAdmin -> true // AdminMunicipal puede todo
                // Acciones sin rol específico pueden ser hechas por Asociación (ej. crear/editar sus drivers/vehicles) si requiredRole es null
                requiredRole == null && userRole == AdminRole.Asociacion -> true
                // Si se requiere un rol específico (solo puede ser AdminMunicipal o Asociacion), comprueba si lo tiene
                requiredRole != null && userRole == requiredRole -> true
                else -> false // Gestor y otros casos no permitidos
            }
            
            if (!hasPermission) {
                throw AccessDeniedException("Permiso denegado para realizar esta acción con el rol $userRole.")
            }
            
            action() // Ejecuta la lógica específica
        } catch (e: AccessDeniedException) {
            redirectAttributes.addFlashAttribute("errorMessage", e.message ?: "No tienes permiso para realizar esta acción.")
        } catch (e: IllegalStateException) { // Errores de negocio (ej. "Email ya en uso")
            redirectAttributes.addFlashAttribute("errorMessage", e.message)
        } catch (e: Exception) { // Otros errores inesperados
            redirectAttributes.addFlashAttribute("errorMessage", "Ha ocurrido un error inesperado: ${e.message}")
            // e.printStackTrace() // Considera quitar esto en producción
        }
        return redirectUrl
    }
}