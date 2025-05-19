package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.mockserver.verify.VerificationTimes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.cache.get
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.CacheConstants.USER_ACCESS_CACHE_KEY
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessAllowedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.UserAccessValidator

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["spring.cache.type=redis"])
@DirtiesContext
class UserAccessValidatorWithCacheEnabledTest : IntegrationTestBase() {

  @Autowired
  lateinit var cacheManager: CacheManager

  @Autowired
  private lateinit var userAccessValidator: UserAccessValidator

  @Test
  fun `checks user access uses cache test`() {
    runTest {
      val crn = "A12345"
      val userName = "SOME_USER"

      val userAccessRequest = request().withPath("/user/$userName/access/$crn")

      deliusIntegration.`when`(userAccessRequest).respond(
        response().withContentType(APPLICATION_JSON).withBody(userAccessAllowedResponse()),
      )

      // Clear cache from any previous test that may have populated it
      cacheManager[USER_ACCESS_CACHE_KEY]?.invalidate()

      // First call: cache miss & write
      userAccessValidator.checkUserAccess(crn, userName)

      deliusIntegration.verify(
        userAccessRequest,
        VerificationTimes.exactly(1),
      )

      // Second call: cache hit
      userAccessValidator.checkUserAccess(crn, userName)

      deliusIntegration.verify(
        userAccessRequest,
        VerificationTimes.exactly(1),
      )
    }
  }
}
