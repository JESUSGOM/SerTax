package com.sertax.api.controller

import com.sertax.api.dto.admin.*
import com.sertax.api.model.DriverRole
import com.sertax.api.model.IncidentStatus
import com.sertax.api.service.AdminDataService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/admin")
class AdminWebController(
    private val adminDataService: AdminDataService
) {

    // --- LOGIN, DASHBOARD, y GESTIÓN DE ENTIDADES (sin cambios) ---
    @GetMapping("/login")
    fun showLoginPage(@RequestParam(required = false) error: String?, @RequestParam(required = false) logout: String?, model: Model): String { if (error != null) model.addAttribute("errorMessage", "Usuario o contraseña incorrectos."); if (logout != null) model.addAttribute("logoutMessage", "Has cerrado sesión correctamente."); return "admin/login" }
    @GetMapping("/dashboard")
    fun showDashboardPage(model: Model): String { model.addAttribute("dashboardStats", adminDataService.getDashboardStats()); model.addAttribute("pageTitle", "Dashboard"); return "admin/dashboard" }
    @GetMapping("/users")
    fun showUsersPage(model: Model): String { model.addAttribute("users", adminDataService.getAllUsers()); model.addAttribute("pageTitle", "Gestión de Usuarios"); return "admin/users" }
    @GetMapping("/users/new")
    fun showCreateUserForm(model: Model): String { model.addAttribute("userDto", CreateUserRequestDto("", "", "", "")); model.addAttribute("pageTitle", "Crear Nuevo Usuario"); return "admin/user-form" }
    @PostMapping("/users/save")
    fun saveUser(@ModelAttribute("userDto") userDto: CreateUserRequestDto, redirectAttributes: RedirectAttributes): String { try { adminDataService.createUser(userDto); redirectAttributes.addFlashAttribute("successMessage", "Usuario creado con éxito.") } catch (e: IllegalStateException) { redirectAttributes.addFlashAttribute("errorMessage", e.message) }; return "redirect:/admin/users" }
    @GetMapping("/licenses")
    fun showLicensesPage(model: Model): String { model.addAttribute("licenses", adminDataService.getAllLicenses()); model.addAttribute("pageTitle", "Gestión de Licencias"); return "admin/licenses" }
    @GetMapping("/licenses/new")
    fun showCreateLicenseForm(model: Model): String { model.addAttribute("licenseDto", CreateLicenseRequestDto("", null)); model.addAttribute("associations", adminDataService.getAllAssociations()); model.addAttribute("pageTitle", "Crear Nueva Licencia"); model.addAttribute("formAction", "/admin/licenses/save"); return "admin/license-form" }
    @GetMapping("/licenses/edit/{id}")
    fun showEditLicenseForm(@PathVariable id: Long, model: Model): String { val license = adminDataService.getLicenseById(id); val licenseDto = UpdateLicenseRequestDto(license.licenseNumber, license.association?.associationId); model.addAttribute("licenseId", id); model.addAttribute("licenseDto", licenseDto); model.addAttribute("associations", adminDataService.getAllAssociations()); model.addAttribute("pageTitle", "Editar Licencia"); model.addAttribute("formAction", "/admin/licenses/update/$id"); return "admin/license-form" }
    @PostMapping("/licenses/save")
    fun saveLicense(@ModelAttribute("licenseDto") licenseDto: CreateLicenseRequestDto, redirectAttributes: RedirectAttributes): String { try { adminDataService.createLicense(licenseDto); redirectAttributes.addFlashAttribute("successMessage", "Licencia creada con éxito.") } catch (e: IllegalStateException) { redirectAttributes.addFlashAttribute("errorMessage", e.message) }; return "redirect:/admin/licenses" }
    @PostMapping("/licenses/update/{id}")
    fun updateLicense(@PathVariable id: Long, @ModelAttribute("licenseDto") licenseDto: UpdateLicenseRequestDto, redirectAttributes: RedirectAttributes): String { try { adminDataService.updateLicense(id, licenseDto); redirectAttributes.addFlashAttribute("successMessage", "Licencia actualizada con éxito.") } catch (e: Exception) { redirectAttributes.addFlashAttribute("errorMessage", e.message) }; return "redirect:/admin/licenses" }
    @GetMapping("/drivers")
    fun showDriversPage(model: Model): String { model.addAttribute("drivers", adminDataService.getAllDrivers()); model.addAttribute("pageTitle", "Gestión de Conductores"); return "admin/drivers" }
    @GetMapping("/drivers/new")
    fun showCreateDriverForm(model: Model): String { model.addAttribute("driverDto", CreateDriverRequestDto("", "", DriverRole.Owner, 0L, true)); model.addAttribute("licenses", adminDataService.getAllLicenses()); model.addAttribute("roles", DriverRole.values()); model.addAttribute("pageTitle", "Crear Nuevo Conductor"); model.addAttribute("formAction", "/admin/drivers/save"); return "admin/driver-form" }
    @GetMapping("/drivers/edit/{id}")
    fun showEditDriverForm(@PathVariable id: Long, model: Model): String { val driver = adminDataService.getDriverById(id); val driverDto = UpdateDriverRequestDto(driver.name, driver.role, driver.isActive); model.addAttribute("driverId", id); model.addAttribute("driverDto", driverDto); model.addAttribute("currentLicense", driver.license); model.addAttribute("roles", DriverRole.values()); model.addAttribute("pageTitle", "Editar Conductor"); model.addAttribute("formAction", "/admin/drivers/update/$id"); return "admin/driver-form" }
    @PostMapping("/drivers/save")
    fun saveDriver(@ModelAttribute("driverDto") driverDto: CreateDriverRequestDto, redirectAttributes: RedirectAttributes): String { try { adminDataService.createDriver(driverDto); redirectAttributes.addFlashAttribute("successMessage", "Conductor creado con éxito.") } catch (e: Exception) { redirectAttributes.addFlashAttribute("errorMessage", e.message) }; return "redirect:/admin/drivers" }
    @PostMapping("/drivers/update/{id}")
    fun updateDriver(@PathVariable id: Long, @ModelAttribute("driverDto") driverDto: UpdateDriverRequestDto, redirectAttributes: RedirectAttributes): String { try { adminDataService.updateDriver(id, driverDto); redirectAttributes.addFlashAttribute("successMessage", "Conductor actualizado con éxito.") } catch (e: Exception) { redirectAttributes.addFlashAttribute("errorMessage", e.message) }; return "redirect:/admin/drivers" }
    @GetMapping("/vehicles")
    fun showVehiclesPage(model: Model): String { model.addAttribute("vehicles", adminDataService.getAllVehicles()); model.addAttribute("pageTitle", "Gestión de Vehículos"); return "admin/vehicles" }
    @GetMapping("/vehicles/new")
    fun showCreateVehicleForm(model: Model): String { model.addAttribute("vehicleDto", CreateVehicleRequestDto("", 0L, null, null, false, false)); model.addAttribute("licenses", adminDataService.getAllLicenses()); model.addAttribute("pageTitle", "Registrar Nuevo Vehículo"); model.addAttribute("formAction", "/admin/vehicles/save"); return "admin/vehicle-form" }
    @GetMapping("/vehicles/edit/{id}")
    fun showEditVehicleForm(@PathVariable id: Long, model: Model): String { val vehicle = adminDataService.getVehicleById(id); val vehicleDto = UpdateVehicleRequestDto(vehicle.licensePlate, vehicle.make, vehicle.model, vehicle.isPMRAdapted, vehicle.allowsPets); model.addAttribute("vehicleId", id); model.addAttribute("vehicleDto", vehicleDto); model.addAttribute("currentLicense", vehicle.license); model.addAttribute("pageTitle", "Editar Vehículo"); model.addAttribute("formAction", "/admin/vehicles/update/$id"); return "admin/vehicle-form" }
    @PostMapping("/vehicles/save")
    fun saveVehicle(@ModelAttribute("vehicleDto") vehicleDto: CreateVehicleRequestDto, redirectAttributes: RedirectAttributes): String { try { adminDataService.createVehicle(vehicleDto); redirectAttributes.addFlashAttribute("successMessage", "Vehículo registrado con éxito.") } catch (e: Exception) { redirectAttributes.addFlashAttribute("errorMessage", e.message) }; return "redirect:/admin/vehicles" }
    @PostMapping("/vehicles/update/{id}")
    fun updateVehicle(@PathVariable id: Long, @ModelAttribute("vehicleDto") vehicleDto: UpdateVehicleRequestDto, redirectAttributes: RedirectAttributes): String { try { adminDataService.updateVehicle(id, vehicleDto); redirectAttributes.addFlashAttribute("successMessage", "Vehículo actualizado con éxito.") } catch (e: Exception) { redirectAttributes.addFlashAttribute("errorMessage", e.message) }; return "redirect:/admin/vehicles" }
    @GetMapping("/trips")
    fun showTripsPage(model: Model): String { model.addAttribute("trips", adminDataService.getAllTrips()); model.addAttribute("pageTitle", "Historial de Viajes"); return "admin/trips" }
    @GetMapping("/incidents")
    fun showIncidentsPage(model: Model): String { model.addAttribute("incidents", adminDataService.getAllIncidents()); model.addAttribute("pageTitle", "Gestión de Incidencias"); return "admin/incidents" }
    @GetMapping("/incidents/{id}")
    fun showIncidentDetailPage(@PathVariable id: Long, model: Model): String { try { model.addAttribute("incident", adminDataService.getIncidentDetails(id)); model.addAttribute("statuses", IncidentStatus.values()); model.addAttribute("pageTitle", "Detalle de Incidencia"); return "admin/incident-detail" } catch (e: NoSuchElementException) { return "redirect:/admin/incidents" } }
    @PostMapping("/incidents/update-status/{id}")
    fun updateIncidentStatus(@PathVariable id: Long, @RequestParam("status") newStatus: IncidentStatus, redirectAttributes: RedirectAttributes): String { try { adminDataService.updateIncidentStatus(id, newStatus); redirectAttributes.addFlashAttribute("successMessage", "Estado de la incidencia actualizado con éxito.") } catch (e: Exception) { redirectAttributes.addFlashAttribute("errorMessage", e.message) }; return "redirect:/admin/incidents/$id" }
    @GetMapping("/config")
    fun showConfigPage(model: Model): String { model.addAttribute("configs", adminDataService.getAllSystemConfigs()); model.addAttribute("pageTitle", "Configuración del Sistema"); return "admin/config" }
    @PostMapping("/config/update")
    fun updateConfig(@RequestParam("configKey") key: String, @RequestParam("configValue") value: String, redirectAttributes: RedirectAttributes): String { try { val request = UpdateSystemConfigRequestDto(value); adminDataService.updateSystemConfig(key, request); redirectAttributes.addFlashAttribute("successMessage", "Parámetro '$key' actualizado con éxito.") } catch (e: Exception) { redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el parámetro: ${e.message}") }; return "redirect:/admin/config" }

    // --- NUEVA RUTA PARA INFORMES ---

    @GetMapping("/reports")
    fun showReportsPage(model: Model): String {
        model.addAttribute("ratingStats", adminDataService.getRatingStats())
        model.addAttribute("pageTitle", "Informes y Estadísticas")
        return "admin/reports"
    }
}