package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.joinToString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderSearchOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Paging
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPeopleRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Pageable
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SearchOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO_NAME_AVAILABLE

@Service
internal class OffenderSearchService(
  @Qualifier("offenderSearchApiClientUserEnhanced") private val offenderSearchApiClient: OffenderSearchApiClient,
  private val userAccessValidator: UserAccessValidator
) {

  val stableSortOrder = listOf("surname", "firstName", "crn", "offenderId")

  suspend fun search(
    crn: String? = null,
    firstName: String? = null,
    lastName: String? = null,
    page: Int,
    pageSize: Int
  ): OffenderSearchResponse {
    val request = OffenderSearchPeopleRequest(
      Pageable(page = page, size = pageSize, sort = stableSortOrder),
      SearchOptions(crn = crn, firstName = firstName, surname = lastName)
    )
    val apiResponse = getValueAndHandleWrappedException(offenderSearchApiClient.searchPeople(request))

    return if (apiResponse == null) {
      OffenderSearchResponse()
    } else {
      return OffenderSearchResponse(
        results = apiResponse.content.map {
          val (userExcluded, userRestricted) = determineAccessRestrictions(it)
          toOffenderSearchOffender(it, userExcluded, userRestricted)
        },
        paging = Paging(
          page = apiResponse.pageable.pageNumber,
          pageSize = apiResponse.pageable.pageSize,
          totalNumberOfPages = apiResponse.totalPages
        )
      )
    }
  }

  private data class AccessRestrictions(val userExcluded: Boolean, val userRestricted: Boolean)

  private fun determineAccessRestrictions(offenderDetails: OffenderDetails): AccessRestrictions {
    // Check whether an empty name is genuinely due to a restriction or exclusion
    return if (offenderDetails.isNameNullOrBlank) {
      val userAccessResponse = userAccessValidator.checkUserAccess(offenderDetails.otherIds.crn)
      AccessRestrictions(
        userExcluded = userAccessResponse.userExcluded,
        userRestricted = userAccessResponse.userRestricted
      )
    } else {
      AccessRestrictions(userExcluded = false, userRestricted = false)
    }
  }

  private fun toOffenderSearchOffender(
    offenderDetails: OffenderDetails,
    userExcluded: Boolean,
    userRestricted: Boolean
  ): OffenderSearchOffender {
    val name =
      if (offenderDetails.isNameNullOrBlank && !userExcluded && !userRestricted) {
        NO_NAME_AVAILABLE
      } else {
        joinToString(offenderDetails.firstName, offenderDetails.surname)
      }
    return OffenderSearchOffender(
      name = name,
      crn = offenderDetails.otherIds.crn,
      dateOfBirth = offenderDetails.dateOfBirth,
      userExcluded = userExcluded,
      userRestricted = userRestricted
    )
  }
}
