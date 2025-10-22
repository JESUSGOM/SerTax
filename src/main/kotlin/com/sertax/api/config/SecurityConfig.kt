package com.sertax.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita la seguridad a nivel de método si la necesitas en el futuro
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    //Cadena de filtros de seguridad para la API REST (/api/**). Es procesada primero (Order 1), y usa JWT para la autenticación
    @Bean
    @Order(1)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**") //Aplica esta configuración SOLO a las rutas que empiezan con /api/
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll() // Endpoints de login/registro de la app móvil son públicos
                    .requestMatchers("/api/whatsapp/webhook").permitAll() // Es el webhook de WatsApp es púbico
                    .anyRequest().authenticated() // Cualquier otra ruta de la API requiere autenticación
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) //La API no usa sesiones
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java) // Añade el filtro JWT

        return http.build()
    }

    //Cadena de filtros de seguridad para el BAckOffice web (/admin/**). Es procesada en egundo lugar (Order 2), usa sesiones y un formulario de login.

    @Bean
    @Order(2)
    fun webSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/admin/**") //Aplica esta configuración SOLO a las rutas que empiezan con /admin/
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/admin/login").permitAll() // La página de login es pública
                    // ---VALIDACIÓN POR ROLL ----
                    // Solo permite el acceso a las rutas de /admin/** a los usuarios que tengan alguno de estos roles.
                    .requestMatchers("/admin/**").hasAnyRole("GESTOR_MUNICIPAL", "ADMIN_MUNICIPAL", "ASOCIACION")
                    .anyRequest().authenticated()
            }
            .formLogin { from ->
                from
                    .loginPage("/admin/login") // URL de nuestra página de login
                    .loginProcessingUrl("/admin/login") //URL a la que el formulario envía los datos (POST)
                    .defaultSuccessUrl("/admin/dashboard", true) // A dónde redirigir si el login es eitoso
                    .failureUrl("/admin/login?error=true") //A dónde dirigir si el login falla
            }
            .logout { logout ->
                logout
                    .logoutUrl("/admin/logout") //URL para activar el cierre de sesión
                    .logoutSuccessUrl("/admin/login?logout=true") //A dónde redirigir después de cerrar sesión
            }
        // No se añade el filtro JWT aquí, ya que el BackOffice usa sesiones estándar.

        return http.build()
    }
}





