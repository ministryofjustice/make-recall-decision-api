package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.mockserver.verify.VerificationTimes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.get
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.CacheConstants.USER_ACCESS_CACHE_KEY
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessAllowedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.UserAccessValidator

@ActiveProfiles("test")
class UserAccessValidatorTest() : IntegrationTestBase() {

  @Autowired
  private lateinit var userAccessValidator: UserAccessValidator

  @Autowired
  lateinit var cacheManager: CacheManager

  @Test
  fun `checks user access - caching test`(@Value("\${spring.cache.type}") isCacheEnabled: String = "none") {
    runTest {
      val userName = "SOME_USER"

      val userAccessRequest = request().withPath("/user/$userName/access/$crn")
      deliusIntegration.`when`(userAccessRequest).respond(
        response().withContentType(APPLICATION_JSON).withBody(userAccessAllowedResponse()),
      )

      if (isCacheEnabled.equals("none", ignoreCase = true)) {
        // First call: cache disabled (miss & no write)
        userAccessValidator.checkUserAccess(crn, userName)

        deliusIntegration.verify(
          request()
            .withPath("/user/$userName/access/$crn"),
          VerificationTimes.exactly(1),
        )

        // Second call: cache disabled (miss & no write)
        userAccessValidator.checkUserAccess(crn, userName)

        deliusIntegration.verify(
          request()
            .withPath("/user/$userName/access/$crn"),
          VerificationTimes.exactly(2),
        )
      } else {
        // Clear any previous cache write
        cacheManager[USER_ACCESS_CACHE_KEY]?.invalidate()

        // First call: cache miss & write
        userAccessValidator.checkUserAccess(crn, userName)

        deliusIntegration.verify(
          request()
            .withPath("/user/$userName/access/$crn"),
          VerificationTimes.exactly(1),
        )

        // Second call: cache hit
        userAccessValidator.checkUserAccess(crn, userName)

        deliusIntegration.verify(
          request()
            .withPath("/user/$userName/access/$crn"),
          VerificationTimes.exactly(1),
        )
      }
    }
  }
}
