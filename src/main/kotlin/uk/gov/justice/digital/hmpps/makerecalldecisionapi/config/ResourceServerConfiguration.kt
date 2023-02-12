package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class ResourceServerConfiguration {
  @Bean
  fun configure(http: HttpSecurity): DefaultSecurityFilterChain? {
    http.authorizeHttpRequests()
      .requestMatchers("/health/**").permitAll()
      .requestMatchers("/info").permitAll()
      .requestMatchers("/prometheus").permitAll()
      .requestMatchers("/v3/api-docs/**").permitAll()
      .requestMatchers("/swagger-ui/**").permitAll()
      .requestMatchers("/swagger-ui.html").permitAll()
      .requestMatchers("/swagger-resources").permitAll()
      .requestMatchers("/swagger-resources/configuration/ui").permitAll()
      .requestMatchers("/swagger-resources/configuration/security").permitAll()
      .requestMatchers("/webjars/**").permitAll()
      .requestMatchers("/favicon.ico").permitAll()
      .requestMatchers("/csrf").permitAll()
      .requestMatchers("/recommendations/**").hasAnyRole("MAKE_RECALL_DECISION")
      .anyRequest().authenticated()
      //.requestMatchers("/**").permitAll()//authenticated()
      .and()
      .csrf()
      .disable()
      .oauth2ResourceServer().jwt().jwtAuthenticationConverter { AuthAwareTokenConverter().convert(it) }
    return http.build()
  }

  @Bean
  fun corsConfigurationSource(): CorsConfigurationSource? {
    val configuration = CorsConfiguration()
    configuration.allowedOrigins = listOf("http://localhost:3000")
    configuration.addAllowedHeader("*")
    configuration.addAllowedMethod("*")
    configuration.allowCredentials = true
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)
    return source
  }

}
