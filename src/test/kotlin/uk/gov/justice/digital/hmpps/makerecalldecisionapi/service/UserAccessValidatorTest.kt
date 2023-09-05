package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class UserAccessValidatorTest : ServiceTestBase() {

  @Test
  fun `checks user access happy path`() {
    runTest {
      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(noAccessLimitations())
      val crn = "my wonderful crn"

      val userAccessResponse = userAccessValidator.checkUserAccess(crn, "username")

      assertThat(userAccessResponse.userExcluded).isEqualTo(false)
      assertThat(userAccessResponse.userRestricted).isEqualTo(false)
    }
  }

  @Test
  fun `checks user access unhappy path - excluded`() {
    runTest {
      val crn = "my wonderful crn"

      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(excludedAccess())

      val userAccessResponse = userAccessValidator.checkUserAccess(crn, "username")

      assertThat(userAccessResponse.userExcluded).isEqualTo(true)
      assertThat(userAccessResponse.exclusionMessage).isEqualTo("I am an exclusion message")
      assertThat(userAccessResponse.userRestricted).isEqualTo(false)
    }
  }

  @Test
  fun `checks user access unhappy path when user not found`() {
    runTest {
      val crn = "my wonderful crn"
      given(deliusClient.getUserAccess(username, crn)).willThrow(PersonNotFoundException("Not found"))

      val userAccessResponse = userAccessValidator.checkUserAccess(crn)

      assertThat(userAccessResponse.userExcluded).isEqualTo(false)
      assertThat(userAccessResponse.userNotFound).isEqualTo(true)
      assertThat(userAccessResponse.exclusionMessage).isEqualTo(null)
      assertThat(userAccessResponse.restrictionMessage).isEqualTo(null)
      assertThat(userAccessResponse.userRestricted).isEqualTo(false)
    }
  }

  @Test
  fun `checks user access unhappy path when restricted`() {
    runTest {
      val crn = "my wonderful crn"
      given(deliusClient.getUserAccess(username, crn)).willReturn(restrictedAccess())

      val userAccessResponse = userAccessValidator.checkUserAccess(crn)

      assertThat(userAccessResponse.userExcluded).isEqualTo(false)
      assertThat(userAccessResponse.restrictionMessage).isEqualTo("I am a restriction message")
      assertThat(userAccessResponse.userRestricted).isEqualTo(true)
    }
  }

  @Test
  fun `checks isUserExcludedOrRestricted when user is restricted`() {
    runTest {
      val userRestrictedExcludedOrNotFound = userAccessValidator.isUserExcludedRestrictedOrNotFound(
        userAccessResponse(
          excluded = false,
          restricted = true,
          userNotFound = false,
        ),
      )
      assertThat(userRestrictedExcludedOrNotFound).isTrue()
    }
  }

  @Test
  fun `checks isUserExcludedOrRestricted when user is not found`() {
    runTest {
      val userRestrictedExcludedOrNotFound = userAccessValidator.isUserExcludedRestrictedOrNotFound(
        userAccessResponse(
          excluded = false,
          restricted = false,
          userNotFound = true,
        ),
      )
      assertThat(userRestrictedExcludedOrNotFound).isTrue()
    }
  }

  @Test
  fun `checks isUserExcludedOrRestricted when user is excluded`() {
    runTest {
      val userRestrictedExcludedOrNotFound = userAccessValidator.isUserExcludedRestrictedOrNotFound(
        userAccessResponse(
          excluded = true,
          restricted = false,
          userNotFound = false,
        ),
      )
      assertThat(userRestrictedExcludedOrNotFound).isTrue()
    }
  }

  @Test
  fun `checks isUserExcludedOrRestricted when user is neither restricted or excluded`() {
    runTest {
      val userRestrictedExcludedOrNotFound = userAccessValidator.isUserExcludedRestrictedOrNotFound(
        userAccessResponse(
          excluded = false,
          restricted = false,
          userNotFound = false,
        ),
      )
      assertThat(userRestrictedExcludedOrNotFound).isFalse()
    }
  }
}
