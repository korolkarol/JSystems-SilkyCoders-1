package com.lppsa.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class ActuatorSecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/actuator/health").permitAll()
                    .pathMatchers("/actuator/**").hasRole("ACTUATOR")
                    .anyExchange().permitAll()
            }
            .httpBasic(Customizer.withDefaults())
            .csrf { it.disable() }
            .build()

    @Bean
    fun userDetailsService(
        @Value("\${ACTUATOR_USER}") user: String,
        @Value("\${ACTUATOR_PASSWORD}") password: String,
        encoder: PasswordEncoder,
    ): ReactiveUserDetailsService =
        MapReactiveUserDetailsService(
            User.withUsername(user)
                .password(encoder.encode(password))
                .roles("ACTUATOR")
                .build()
        )

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
