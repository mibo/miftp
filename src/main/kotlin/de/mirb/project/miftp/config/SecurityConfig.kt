package de.mirb.project.miftp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * For hints and tips see: https://www.baeldung.com/spring-security-5-reactive
 */
@EnableWebFluxSecurity
class SecurityConfig {

  @Value("\${miftp.user}")
  private var username: String? = null
  @Value("\${miftp.password}")
  private var password: String? = null

  fun getUsername():String = username!!

  /**
   * See for password encoding the ApplicationHelper in the tests
   */
  @Bean
  fun userDetailsService(): MapReactiveUserDetailsService {
//    val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
//    println(encoder.encode("user"))

    if(username == null || password == null) {
      println("No user and/or password set. Fallback to default ('miftp/miftp')")
      username = "miftp"
      password = "{bcrypt}\$2a\$10\$5SyjnpMano4Z3LGbWQC9W.ySSsheBZI.7uufzpJ4uKokBGfd.uHau"
    }

    val user = User.withUsername(username)
            .password(password)
            .roles("USER")
            .build()
    return MapReactiveUserDetailsService(user)
  }

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http.authorizeExchange()
        .pathMatchers("/go/token/*").permitAll()
        .anyExchange().authenticated()
        .and().httpBasic()
//        .and().formLogin()
    return http.build()
  }
}