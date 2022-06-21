package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse

@Service
class LicenceConditionsService(
  private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService
) {

  suspend fun getLicenceConditions(crn: String): LicenceConditionsResponse {
    val userAccessResponse = communityApiClient.getUserAccess(crn).awaitFirst()
    return if (true == userAccessResponse?.userExcluded || true == userAccessResponse?.userRestricted) {
      LicenceConditionsResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val convictions = buildConvictionResponse(crn)
      val releaseSummary = getReleaseSummary(crn)

      LicenceConditionsResponse(
        personalDetailsOverview = personalDetailsOverview,
        convictions = convictions,
        releaseSummary = releaseSummary,
      )
    }
  }

  private suspend fun buildConvictionResponse(crn: String): List<ConvictionResponse> {

    val activeConvictions = communityApiClient.getActiveConvictions(crn).awaitFirst()

    return activeConvictions
      .map {
        val result = communityApiClient.getLicenceConditionsByConvictionId(crn, it.convictionId).awaitFirstOrNull()
          ?.licenceConditions

        val offences: List<Offence>? = it.offences
          ?.stream()?.toList()
          ?.map {
            Offence(
              mainOffence = it.mainOffence, description = it.detail?.description ?: "", code = it.detail?.code ?: ""
            )
          }

        ConvictionResponse(
          convictionId = it.convictionId,
          active = it.active,
          offences = offences,
          sentenceDescription = it.sentence?.description,
          sentenceOriginalLength = it.sentence?.originalLength,
          sentenceOriginalLengthUnits = it.sentence?.originalLengthUnits,
          sentenceStartDate = it.sentence?.startDate,
          licenceExpiryDate = it.custody?.keyDates?.licenceExpiryDate,
          postSentenceSupervisionEndDate = it.custody?.keyDates?.postSentenceSupervisionEndDate,
          statusCode = it.custody?.status?.code,
          statusDescription = it.custody?.status?.description,
          licenceConditions = result
        )
      }
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return communityApiClient.getReleaseSummary(crn).awaitFirstOrNull()
  }
}
