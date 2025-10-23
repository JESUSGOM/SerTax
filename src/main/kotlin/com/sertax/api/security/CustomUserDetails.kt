package com.sertax.api.security // O tu paquete correspondiente

import com.sertax.api.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(private val user: User) : UserDetails {
	
	val role: String
		get() = user.role
	
	// <-- CORREGIDO: Accede a 'associationid' que está dentro de la entidad Association
	val associationId: Long?
		get() = user.association?.associationId
	
	override fun getAuthorities(): Collection<GrantedAuthority> {
		return listOf(SimpleGrantedAuthority("ROLE_${user.role}"))
	}
	
	// <-- CORREGIDO: 'passwordHash' con 'H' mayúscula
	override fun getPassword(): String = user.passwordHash
	
	override fun getUsername(): String = user.email
	override fun isAccountNonExpired(): Boolean = true
	override fun isAccountNonLocked(): Boolean = true
	override fun isCredentialsNonExpired(): Boolean = true
	
	// Enlazado a tu campo 'isActive'
	override fun isEnabled(): Boolean = user.isActive
}