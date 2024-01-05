package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthController {
  @PublicEndpoint
  @GetMapping("/health")
  @ResponseStatus(HttpStatus.OK)
  fun getHealth(): Map<String, String> =
    hashMapOf(
      "status" to "UP",
      "version" to version(),
    )

  private

  fun version(): String = System.getenv("BUILD_NUMBER") ?: "app_version"
}
