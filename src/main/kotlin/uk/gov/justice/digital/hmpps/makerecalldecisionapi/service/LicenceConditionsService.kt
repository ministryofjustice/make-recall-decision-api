package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
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

        OffenceWithLicenceConditions(
          convictionId = it.convictionId,
          licenceConditions = result
        )
      }
  }
}
