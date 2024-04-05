package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(SentryContextAppender())
  }

  override fun configurePathMatch(configurer: PathMatchConfigurer) {
    // Use this until the UI is updated to no longer use trailing slashes
    configurer.setUseTrailingSlashMatch(true)
  }
}
