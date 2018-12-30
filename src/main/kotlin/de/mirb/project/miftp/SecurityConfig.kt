package de.mirb.project.miftp

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain


@EnableWebFluxSecurity
class SecurityConfig {
  /**
   * See for password encoding the ApplicationHelper in the tests
   */
  @Bean
  fun userDetailsService(): MapReactiveUserDetailsService {
//    val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
//    println(encoder.encode("user"))

    val user = User.withUsername("user")
            .password("{bcrypt}\$2a\$10\$OVfUtzvd8zqBHgJQyrY.vO4M2MTDigzhLfS.rHdGoaER86WR.D9q2")
            .roles("USER")
            .build()
//    val user = User.withDefaultPasswordEncoder()
//            .username("user")
//            .password("user")
//            .roles("USER")
//            .build()
    return MapReactiveUserDetailsService(user)
  }

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http.authorizeExchange()
        .anyExchange().authenticated()
        .and().httpBasic()
//        .and().formLogin()
    return http.build()
  }
}