package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class UserAccessValidatorTest {

  @Mock
  protected lateinit var communityApiClient: CommunityApiClient

  protected lateinit var userAccessValidator: UserAccessValidator

  @BeforeEach
  fun setup() {
    userAccessValidator = UserAccessValidator(communityApiClient)
  }

  @Test
  fun `checks user access happy path`() {
    runTest {
      given(communityApiClient.getUserAccess(anyString()))
        .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
      val crn = "my wonderful crn"

      val userAccessResponse = userAccessValidator.checkUserAccess(crn)

      assertThat(userAccessResponse?.userExcluded).isEqualTo(false)
      assertThat(userAccessResponse?.userRestricted).isEqualTo(false)
    }
  }

  @Test
  fun `checks user access unhappy path - excluded`() {
    runTest {
      val crn = "my wonderful crn"

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403, "Forbidden", null, excludedResponse().toByteArray(), null
        )
      )

      val userAccessResponse = userAccessValidator.checkUserAccess(crn)

      assertThat(userAccessResponse?.userExcluded).isEqualTo(true)
      assertThat(userAccessResponse?.exclusionMessage).isEqualTo("I am an exclusion message")
      assertThat(userAccessResponse?.userRestricted).isEqualTo(false)
    }
  }

  @Test
  fun `checks user access unhappy path when restricted`() {
    runTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403, "Forbidden", null, restrictedResponse().toByteArray(), null
        )
      )

      val userAccessResponse = userAccessValidator.checkUserAccess(crn)

      assertThat(userAccessResponse?.userExcluded).isEqualTo(false)
      assertThat(userAccessResponse?.exclusionMessage).isEqualTo("I am a restriction message")
      assertThat(userAccessResponse?.userRestricted).isEqualTo(true)
    }
  }

  @Test
  fun `checks isUserExcludedOrRestricted when user is restricted`() {
    runTest {
      val userRestrictedOrExcluded = userAccessValidator.isUserExcludedOrRestricted(
        userAccessResponse(
          excluded = false,
          restricted = true
        )
      )
      assertThat(userRestrictedOrExcluded).isTrue()
    }
  }

  @Test
  fun `checks isUserExcludedOrRestricted when user is excluded`() {
    runTest {
      val userRestrictedOrExcluded = userAccessValidator.isUserExcludedOrRestricted(
        userAccessResponse(
          excluded = true,
          restricted = false
        )
      )
      assertThat(userRestrictedOrExcluded).isTrue()
    }
  }

  @Test
  fun `checks isUserExcludedOrRestricted when user is neither restricted or excluded`() {
    runTest {
      val userRestrictedOrExcluded = userAccessValidator.isUserExcludedOrRestricted(
        userAccessResponse(
          excluded = false,
          restricted = false
        )
      )
      assertThat(userRestrictedOrExcluded).isFalse()
    }
  }

  private fun userAccessResponse(excluded: Boolean, restricted: Boolean) = UserAccessResponse(
    userRestricted = restricted,
    userExcluded = excluded,
    exclusionMessage = "I am an exclusion message",
    restrictionMessage = "I am a restriction message"
  )
}
