package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.joinToString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderSearchOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Paging

@Service
internal class OffenderSearchService(
  private val deliusClient: DeliusClient,
  private val userAccessValidator: UserAccessValidator,
) {
  suspend fun search(
    crn: String? = null,
    firstName: String? = null,
    lastName: String? = null,
    page: Int,
    pageSize: Int,
  ): OffenderSearchResponse = if (crn != null) {
    deliusClient.findByCrn(crn)?.let {
      OffenderSearchResponse(
        results = listOf(it.toOffenderSearchOffender()),
        paging = Paging(page = 1, pageSize = pageSize, totalNumberOfPages = 1),
      )
    } ?: OffenderSearchResponse()
  } else if (firstName != null && lastName != null) {
    deliusClient.findByName(firstName, lastName, page, pageSize).let {
      OffenderSearchResponse(
        results = it.content.map { it.toOffenderSearchOffender() },
        paging = Paging(
          page = it.page.number.toInt(),
          pageSize = it.page.size.toInt(),
          totalNumberOfPages = it.page.totalPages.toInt(),
        ),
      )
    }
  } else {
    OffenderSearchResponse()
  }

  private fun DeliusClient.PersonalDetailsOverview.toOffenderSearchOffender(): OffenderSearchOffender {
    val access = userAccessValidator.checkUserAccess(identifiers.crn)
    val hasAccess = !access.userExcluded && !access.userRestricted

    return OffenderSearchOffender(
      name = joinToString(this.name.forename, this.name.surname).takeIf { hasAccess } ?: "",
      crn = this.identifiers.crn,
      dateOfBirth = this.dateOfBirth.takeIf { hasAccess },
      userExcluded = access.userExcluded,
      userRestricted = access.userRestricted,
    )
  }
}
