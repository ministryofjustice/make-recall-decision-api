package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.servlet.invoke

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    http {
      csrf { disable() }
      sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
      authorizeRequests {
        authorize("/health/**", permitAll)
        //The following is validated because the submitting form must submit a token when uploading.
        authorize("/recommendations/*/file-upload/**", permitAll)
        //The following is validated because the request url must contain a valid token.
        authorize("/recommendations/*/file/**", permitAll)
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
