package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class ResourceServerConfiguration {

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
    http.authorizeExchange()
      .pathMatchers("/health/**").permitAll()
      .pathMatchers("/info").permitAll()
      .pathMatchers("/prometheus").permitAll()
      .pathMatchers("/v3/api-docs/**").permitAll()
      .pathMatchers("/swagger-ui/**").permitAll()
      .pathMatchers("/swagger-ui.html").permitAll()
      .pathMatchers("/swagger-resources").permitAll()
      .pathMatchers("/swagger-resources/configuration/ui").permitAll()
      .pathMatchers("/swagger-resources/configuration/security").permitAll()
      .pathMatchers("/webjars/**").permitAll()
      .pathMatchers("/favicon.ico").permitAll()
      .pathMatchers("/csrf").permitAll()
//      .pathMatchers(AntPathMatcher.any).authenticated()
      .and()
      .csrf()
      .disable()
      .oauth2ResourceServer().jwt { AuthAwareTokenConverter() }
    return http.build()
  }
}
