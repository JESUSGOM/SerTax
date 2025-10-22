package com.sertax.api.service

import com.sertax.api.repository.SystemAdminRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AdminUserDetailsService(
    private val systemAdminRepository: SystemAdminRepository
) : UserDetailsService {

    /**
     * Carga los detalles de un administrador por su nombre de usuario.
     * Es utilizado automáticamente por Spring Security durante el proceso de login del formulario del BackOffice.
     */
    override fun loadUserByUsername(username: String): UserDetails {
        val admin = systemAdminRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Administrador no encontrado con el nombre de usuario: $username")

        // Creamos una lista de "autoridades" o roles para Spring Security.
        // El prefijo "ROLE_" es una convención estándar de Spring.
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${admin.role.name.uppercase()}"))

        return User(
            admin.username,
            admin.passwordHash,
            authorities // Aquí le pasamos los permisos del administrador
        )
    }
}