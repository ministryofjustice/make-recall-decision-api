package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Overview.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Overview.Release
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class CaseSummaryOverviewResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val activeConvictions: List<Conviction> = emptyList(),
  val lastRelease: Release? = null,
  val risk: Risk? = null,
  val activeRecommendation: ActiveRecommendation? = null,

  // The following fields and mapping code will be removed:
  @Deprecated("Moved to 'activeConvictions'. The 'convictions' field will be removed once the UI has switched over.")
  val convictions: List<OverviewConvictionResponse>? = activeConvictions.map {
    OverviewConvictionResponse(
      active = true,
      offences = listOf(Offence(true, it.mainOffence.description, it.mainOffence.code, it.mainOffence.date)) +
        it.additionalOffences.map { offence -> Offence(false, offence.description, offence.code, offence.date) },
      sentenceDescription = it.sentence?.description,
      sentenceOriginalLength = it.sentence?.length,
      sentenceOriginalLengthUnits = it.sentence?.lengthUnits,
      sentenceExpiryDate = it.sentence?.sentenceExpiryDate,
      licenceExpiryDate = it.sentence?.licenceExpiryDate,
      isCustodial = it.sentence?.isCustodial ?: false,
      statusCode = it.sentence?.custodialStatusCode
    )
  },
  @Deprecated("Moved to 'release'. The 'releaseSummary' field will be removed once the UI has switched over.")
  val releaseSummary: ReleaseSummaryResponse? = lastRelease?.let {
    ReleaseSummaryResponse(
      LastRelease(it.releaseDate),
      LastRecall(it.recallDate)
    )
  }
)
