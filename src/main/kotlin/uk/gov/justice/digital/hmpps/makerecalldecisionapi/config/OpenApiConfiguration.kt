package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://make-recall-decision-api-dev.hmpps.service.justice.gov.uk").description("dev"),
        Server().url("http://localhost:8080").description("local"),
      ),
    )
    .info(
      Info().title("Consider a Recall API")
        .version(version)
        .description("API to support the Consider a Recall service")
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
}
