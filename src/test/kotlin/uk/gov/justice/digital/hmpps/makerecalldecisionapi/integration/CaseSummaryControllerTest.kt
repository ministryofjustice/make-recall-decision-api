package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.ResourceServerConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.WebClientConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.WebConfig
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller.CaseSummaryController


@ActiveProfiles("test")
//@WebFluxTest(CaseSummaryController::class) //Cannot have both this and inherired @SpringBootTest
//@Import(ResourceServerConfiguration::class, WebClientConfiguration::class, WebConfig::class)
@ExperimentalCoroutinesApi
//@EnableGlobalMethodSecurity(prePostEnabled = false, proxyTargetClass = true)
//@ComponentScan(excludeFilters = [ComponentScan.Filter(classes = [ResourceServerConfiguration::class])])
//@EnableAutoConfiguration(exclude = [ResourceServerConfiguration::class])
class CaseSummaryControllerTest(
//  @Autowired private val objectMapper: ObjectMapper
) : IntegrationTestBase() {

  @Test
  fun `retrieves stuff`() {//TODO start mockserver
    runBlockingTest {
      val crn = "1565"
      webTestClient.get()
        .uri { builder ->
          builder.path("/cases/${crn}/search").build()
        }
        .headers { it.authToken(roles = listOf("MAKE_RECALL_DECISION")) }
//        .header("ROLE", "MAKE_RECALL_DECISION" )
        .exchange()//TODO needs token - copy AB or other project!!
        .expectStatus().isOk
    }
  }

}