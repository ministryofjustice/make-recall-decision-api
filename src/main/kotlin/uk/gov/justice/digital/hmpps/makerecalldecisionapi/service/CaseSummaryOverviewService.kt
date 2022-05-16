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
    val offenderDetails = communityApiClient.getAllOffenderDetails(crn).awaitFirst()
    val activeConvictions = communityApiClient.getConvictions(crn).awaitFirst()
    val age = offenderDetails?.dateOfBirth?.until(LocalDate.now())?.years
    val firstName = offenderDetails?.firstName ?: ""
    val surname = offenderDetails?.surname ?: ""
    val name = if (firstName.isEmpty()) {
      surname
    } else "$firstName $surname"

    val offences: List<Offence> = activeConvictions
      .map { it.offences }
      .flatMap { it!!.toList() }
      .map {
        Offence(
          mainOffence = it.mainOffence, description = it.detail?.description ?: ""
        )
      }

    return CaseSummaryOverviewResponse(
      personalDetailsOverview = PersonDetails(
        name = name,
        dateOfBirth = offenderDetails?.dateOfBirth,
        age = age,
        gender = offenderDetails?.gender ?: "",
        crn = crn
      ),
      offences = offences.filter { it.mainOffence == true }
    )
  }
}
