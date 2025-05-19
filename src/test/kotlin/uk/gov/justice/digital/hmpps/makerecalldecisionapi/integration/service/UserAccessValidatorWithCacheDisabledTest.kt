package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.mockserver.verify.VerificationTimes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessAllowedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.UserAccessValidator

@ActiveProfiles("test")
class UserAccessValidatorWithCacheDisabledTest : IntegrationTestBase() {

  @Autowired
  private lateinit var userAccessValidator: UserAccessValidator

  @Test
  fun `checks user access - caching test`() {
    runTest {
      val userName = "SOME_USER"

      val userAccessRequest = request().withPath("/user/$userName/access/$crn")
      deliusIntegration.`when`(userAccessRequest).respond(
        response().withContentType(APPLICATION_JSON).withBody(userAccessAllowedResponse()),
      )

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
    }
  }
}
