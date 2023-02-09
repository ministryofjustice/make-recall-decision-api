package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


//import org.springframework.security.config.web.servlet.invoke

@Configuration
@EnableWebSecurity
class ResourceServerConfiguration {

  @Bean
  @Throws(Exception::class)
  fun filterChain(http: HttpSecurity): SecurityFilterChain? {
    http
      .authorizeHttpRequests(
        Customizer<AuthorizationManagerRequestMatcherRegistry> { authz: AuthorizationManagerRequestMatcherRegistry ->
          authz
            .anyRequest().authenticated()
        }
      )
      .httpBasic(withDefaults())
    return http.build()
  }
  override fun configure(http: HttpSecurity) {
    http {
      csrf { disable() }
      sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
      authorizeRequests {
        authorize("/health/**", permitAll)
        authorize("/info", permitAll)
        authorize("/prometheus", permitAll)
        authorize("/v3/api-docs/**", permitAll)
        authorize("/swagger-ui/**", permitAll)
        authorize("/swagger-ui.html", permitAll)
        authorize("/swagger-resources", permitAll)
        authorize("/swagger-resources/configuration/ui", permitAll)
        authorize("/swagger-resources/configuration/security", permitAll)
        authorize("/webjars/**", permitAll)
        authorize("/favicon.ico", permitAll)
        authorize("/csrf", permitAll)
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer {
        jwt {
          jwtAuthenticationConverter = AuthAwareTokenConverter()
        }
      }
    }
  }
}
