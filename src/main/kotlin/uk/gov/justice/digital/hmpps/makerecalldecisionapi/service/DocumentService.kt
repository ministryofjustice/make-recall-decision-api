package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException

@Service
internal class DocumentService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val userAccessValidator: UserAccessValidator
) {
  fun getDocumentByCrnAndId(crn: String, documentId: String): ResponseEntity<Resource>? {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      val message = if (userAccessResponse.userRestricted) {
        "Access restricted for case:: $crn, message:: ${userAccessResponse.restrictionMessage}"
      } else if (userAccessResponse.userExcluded) {
        "Access excluded for case:: $crn, message:: ${userAccessResponse.exclusionMessage}"
      } else {
        "User trying to access case:: $crn not found"
      }
      throw UserAccessException(message)
    }
    return getValueAndHandleWrappedException(communityApiClient.getDocumentByCrnAndId(crn, documentId))
  }
}
