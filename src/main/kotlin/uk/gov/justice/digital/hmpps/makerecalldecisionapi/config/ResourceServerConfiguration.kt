package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration {

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
      headers { frameOptions { sameOrigin = true } }
      csrf { disable() }
      authorizeHttpRequests {
        listOf(
          "/health/**",
          "/info",
          "/prometheus",
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/swagger-resources",
          "/swagger-resources/configuration/ui",
          "/swagger-resources/configuration/security",
          "/webjars/**",
          "/favicon.ico",
          "/csrf",
        ).forEach { authorize(it, permitAll) }
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() } }
    }

    return http.build()
  }
}
