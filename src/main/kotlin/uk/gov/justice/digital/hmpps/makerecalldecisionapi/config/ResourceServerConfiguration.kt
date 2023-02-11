package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.DefaultSecurityFilterChain

@Configuration
@EnableWebSecurity
class ResourceServerConfiguration {

  // rebase this on new branch + rebase ui and try e2e again
  // google / sof security with springboot v3 upgrade
  // ordeactivate controller test and push to ci
  // find out why failing if stillfailing .. hard code pwd??
  // pwd missingine2e

  // stand up
  // security scan tool has told use upgrade is critical
  // half done... invested a good chunk of time in it (not wasted as we have to do eventually)
  // but
  // no one else in org has upgraded
  // pen test/assessment has passed
  // so is arg to keep branch/ de-prioeitise/pause work until needed and nudged  JIT devwork and then will have head start
  // focus prep onboarding stuff

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
      .requestMatchers("/**").authenticated()
      .and()
      .csrf()
      .disable()
      .oauth2ResourceServer().jwt { AuthAwareTokenConverter() }
    return http.build()
  }
}
