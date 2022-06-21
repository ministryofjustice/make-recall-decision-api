package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Risk
import java.time.LocalDate

@Service
class CaseSummaryOverviewService(
  private val communityApiClient: CommunityApiClient
) {
  suspend fun getOverview(crn: String): CaseSummaryOverviewResponse {
    val userAccessResponse = communityApiClient.getUserAccess(crn).awaitFirst()
    if (true == userAccessResponse?.userExcluded || true == userAccessResponse?.userRestricted) {
      return CaseSummaryOverviewResponse(userAccessResponse = userAccessResponse)
    } else {
      val offenderDetails = communityApiClient.getAllOffenderDetails(crn).awaitFirst()
      val activeConvictions = communityApiClient.getActiveConvictions(crn).awaitFirst()
      val age = offenderDetails?.dateOfBirth?.until(LocalDate.now())?.years
      val firstName = offenderDetails?.firstName ?: ""
      val surname = offenderDetails?.surname ?: ""
      val name = if (firstName.isEmpty()) {
        surname
      } else "$firstName $surname"
      val registrations = communityApiClient.getRegistrations(crn).awaitFirstOrNull()?.registrations
      val activeRegistrations = registrations?.filter { it.active ?: false }
      val riskFlags = activeRegistrations?.map { it.type?.description ?: "" } ?: emptyList()

      val offences: List<Offence> = activeConvictions
        .map { it.offences }
        .flatMap { it!!.toList() }
        .map {
          Offence(
            mainOffence = it.mainOffence, description = it.detail?.description ?: "", code = it.detail?.code ?: ""
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
        offences = offences.filter { it.mainOffence == true },
        risk = Risk(flags = riskFlags)
      )
    }
  }
}
