package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OverviewConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Risk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import kotlin.streams.toList

@Service
internal class CaseSummaryOverviewService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService,
  private val userAccessValidator: UserAccessValidator,
  private val recommendationService: RecommendationService
) {
  suspend fun getOverview(crn: String): CaseSummaryOverviewResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      CaseSummaryOverviewResponse(userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)

      val registrations = getValueAndHandleWrappedException(communityApiClient.getRegistrations(crn))?.registrations
      val activeRegistrations = registrations?.filter { it.active ?: false }
      val riskFlags = activeRegistrations?.map { it.type?.description ?: "" } ?: emptyList()

      val convictionResponse = buildConvictionResponse(crn)

      val recommendationDetails = recommendationService.getDraftRecommendationForCrn(crn)

      val releaseSummary = getReleaseSummary(crn)

      CaseSummaryOverviewResponse(
        personalDetailsOverview = personalDetailsOverview,
        convictions = convictionResponse,
        releaseSummary = releaseSummary,
        risk = Risk(flags = riskFlags),
        activeRecommendation = recommendationDetails
      )
    }
  }

  private suspend fun buildConvictionResponse(crn: String): List<OverviewConvictionResponse> {

    val activeConvictions = getValueAndHandleWrappedException(communityApiClient.getActiveConvictions(crn))

    return activeConvictions
      ?.map {
        val offences: List<Offence>? = it.offences
          ?.stream()?.toList()
          ?.map {
            Offence(
              mainOffence = it.mainOffence, description = it.detail?.description ?: "", code = it.detail?.code ?: ""
            )
          }

        OverviewConvictionResponse(
          active = it.active,
          offences = offences,
          sentenceDescription = it.sentence?.description,
          sentenceOriginalLength = it.sentence?.originalLength,
          sentenceOriginalLengthUnits = it.sentence?.originalLengthUnits,
          sentenceExpiryDate = it.custody?.keyDates?.sentenceExpiryDate,
          licenceExpiryDate = it.custody?.keyDates?.licenceExpiryDate,
          isCustodial = it.isCustodial
        )
      } ?: emptyList()
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))
  }
}
