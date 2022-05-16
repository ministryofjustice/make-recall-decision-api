package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import java.time.LocalDate

@Service
class CaseSummaryOverviewService(
  private val communityApiClient: CommunityApiClient
) {
  suspend fun getOverview(crn: String): CaseSummaryOverviewResponse {
    val offenderDetails = communityApiClient.getAllOffenderDetails(crn).awaitFirstOrNull()
      ?: throw PersonNotFoundException("No details available for crn: $crn")
    val age = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years
    val convictions = communityApiClient.getConvictions(crn).awaitFirstOrNull()
    val activeConviction = convictions?.first { it.active ?: false }
    val firstName = offenderDetails.firstName ?: ""
    val surname = offenderDetails.surname ?: ""
    val name = if (firstName.isEmpty()) {
      surname
    } else "$firstName $surname"

    return CaseSummaryOverviewResponse(
      personalDetailsOverview = PersonDetails(
        name = name,
        dateOfBirth = offenderDetails.dateOfBirth,
        age = age,
        gender = offenderDetails.gender ?: "",
        crn = crn
      ),
      offences = activeConviction?.offences?.map {
        Offence(mainOffence = it.mainOffence, description = it.detail?.description ?: "")
      }
    )
  }
}
