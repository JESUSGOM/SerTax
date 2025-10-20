package com.sertax.api.config

import com.sertax.api.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider, // <-- Esta referencia ahora funcionará
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // La variable se llama authHeader, no auth-Header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) { // <-- CORREGIDO
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)
        val userEmail = jwtProvider.getEmailFromToken(token) // <-- Esta referencia ahora funcionará

        if (userEmail != null && SecurityContextHolder.getContext().authentication == null) {

            val user = userRepository.findByEmail(userEmail)
                ?: throw UsernameNotFoundException("Usuario no encontrado con el email: $userEmail")

            val userDetails: UserDetails = org.springframework.security.core.userdetails.User(
                user.email,
                user.passwordHash,
                emptyList()
            )

            val authToken = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities
            )

            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)

            SecurityContextHolder.getContext().authentication = authToken
        }

        filterChain.doFilter(request, response)
    }
}