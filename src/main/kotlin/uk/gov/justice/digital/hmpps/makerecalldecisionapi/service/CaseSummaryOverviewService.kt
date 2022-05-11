package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.PersonDetails
import java.time.LocalDate

@Service
class CaseSummaryOverviewService(
  private val communityApiClient: CommunityApiClient
) {
  suspend fun getOverview(crn: String): CaseSummaryOverviewResponse {
    val convictions = communityApiClient.getConvictions(crn).awaitFirst()
    val activeConviction = convictions.first { it.active ?: false }
    val offenderDetails = communityApiClient.getAllOffenderDetails(crn).awaitFirst()
    val age = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years

    return CaseSummaryOverviewResponse(
      personDetails = PersonDetails(
        name = "${offenderDetails.firstName} ${offenderDetails.surname}",
        dateOfBirth = offenderDetails.dateOfBirth,
        age = age,
        gender = offenderDetails.gender,
        crn = crn
      ),
      offences = activeConviction.offences?.map {
        Offence(mainOffence = it.mainOffence, description = it.detail?.description)
      }
    )
  }
}
