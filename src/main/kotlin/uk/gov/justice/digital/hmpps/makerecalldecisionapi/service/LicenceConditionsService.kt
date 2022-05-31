package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceWithLicenceConditions

@Service
class LicenceConditionsService(
  private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService
) {

  suspend fun getLicenceConditions(crn: String): LicenceConditionsResponse {
    val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
    val licenceConditions = buildLicenceConditions(crn)

    return LicenceConditionsResponse(
      personalDetailsOverview = personalDetailsOverview,
      offences = licenceConditions,
    )
  }

  private suspend fun buildLicenceConditions(crn: String): List<OffenceWithLicenceConditions> {

    val activeConvictions = communityApiClient.getActiveConvictions(crn).awaitFirst()

    return activeConvictions
      .map {
        val result = communityApiClient.getLicenceConditionsByConvictionId(crn, it.convictionId).awaitFirstOrNull()
          ?.licenceConditions?.filter { it.active == true }

        val offences: List<Offence> = activeConvictions
          .map { it.offences }
          .flatMap { it!!.toList() }
          .map {
            Offence(
              mainOffence = it.mainOffence, description = it.detail?.description ?: "", code = it.detail?.code ?: ""
            )
          }

        OffenceWithLicenceConditions(
          convictionId = it.convictionId,
          active = it.active,
          offences = offences.filter { it.mainOffence == true },
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
}
