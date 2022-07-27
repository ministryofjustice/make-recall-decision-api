package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import kotlin.streams.toList

@Service
internal class LicenceConditionsService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService,
  private val userAccessValidator: UserAccessValidator,
  private val documentService: DocumentService,
  private val recommendationService: RecommendationService
) {
  suspend fun getLicenceConditions(crn: String): LicenceConditionsResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      LicenceConditionsResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val convictions = buildConvictionResponse(crn)
      val releaseSummary = getReleaseSummary(crn)
      val recommendationDetails = recommendationService.getDraftRecommendationForCrn(crn)

      LicenceConditionsResponse(
        personalDetailsOverview = personalDetailsOverview,
        convictions = convictions,
        releaseSummary = releaseSummary,
        activeRecommendation = recommendationDetails,
      )
    }
  }

  private suspend fun buildConvictionResponse(crn: String): List<ConvictionResponse> {

    val activeConvictions = getValueAndHandleWrappedException(communityApiClient.getActiveConvictions(crn))

    val allConvictionDocuments = documentService.getDocumentsForConvictions(crn)

    return activeConvictions
      ?.map {
        val licenceConditions = getValueAndHandleWrappedException(communityApiClient.getLicenceConditionsByConvictionId(crn, it.convictionId))
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
          sentenceExpiryDate = it.custody?.keyDates?.sentenceExpiryDate,
          postSentenceSupervisionEndDate = it.custody?.keyDates?.postSentenceSupervisionEndDate,
          statusCode = it.custody?.status?.code,
          statusDescription = it.custody?.status?.description,
          licenceConditions = licenceConditions,
          licenceDocuments = allConvictionDocuments?.filter { document -> document.parentPrimaryKeyId == it.convictionId },
        )
      } ?: emptyList()
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))
  }
}
