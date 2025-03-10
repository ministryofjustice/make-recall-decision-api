package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.CacheConstants.USER_ACCESS_CACHE_KEY
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException

@Component
internal class UserAccessValidator(private val deliusClient: DeliusClient) {
  @Cacheable(USER_ACCESS_CACHE_KEY)
  fun checkUserAccess(
    crn: String,
    username: String = SecurityContextHolder.getContext().authentication.name,
  ) = try {
    deliusClient.getUserAccess(username, crn)
  } catch (e: PersonNotFoundException) {
    UserAccess(
      userNotFound = true,
      userRestricted = false,
      userExcluded = false,
    )
  }

  fun isUserExcludedRestrictedOrNotFound(userAccess: UserAccess?) =
    userAccess != null && (userAccess.userExcluded || userAccess.userRestricted || userAccess.userNotFound)
}
