package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.CircumstancesIncreaseRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.FactorsToReduceRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.NatureOfRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OasysHeading
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskOfSeriousHarm
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskPersonalDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.WhenRiskHighest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.WhoIsAtRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Service
class RiskService(
  private val communityApiClient: CommunityApiClient,
  private val arnApiClient: ArnApiClient
) {
  suspend fun getRisk(crn: String): RiskResponse {
    val riskSummaryResponse = arnApiClient.getRiskSummary(crn).awaitFirst()
    val personalDetailsOverview = fetchPersonalDetails(crn)
    val riskOfSeriousHarm = extractRiskOfSeriousHarm(riskSummaryResponse)
    val natureOfRisk = extractNatureOfRisk(riskSummaryResponse)
    val whoIsAtRisk = extractWhoIsAtRisk(riskSummaryResponse)
    val circumstancesIncreaseRisk = extractCircumstancesIncreaseRisk(riskSummaryResponse)
    val factorsToReduceRisk = extractFactorsToReduceRisk(riskSummaryResponse)
    val whenRiskHighest = extractWhenRiskHighest(riskSummaryResponse)

    val mappa = fetchMappa(crn) // TODO find out how isNominal field is derived
    val predictorScores = null // TODO Andrew's API will provide this
    val contingencyPlan = null // TODO Andrew's API will provide this

    return RiskResponse(
      personalDetailsOverview = personalDetailsOverview,
      riskOfSeriousHarm = riskOfSeriousHarm,
      mappa = mappa,
      predictorScores = predictorScores,
      natureOfRisk = natureOfRisk,
      contingencyPlan = contingencyPlan,
      whoIsAtRisk = whoIsAtRisk,
      circumstancesIncreaseRisk = circumstancesIncreaseRisk,
      factorsToReduceRisk = factorsToReduceRisk,
      whenRiskHighest = whenRiskHighest
    )
  }

  private suspend fun extractNatureOfRisk(riskSummaryResponse: RiskSummaryResponse): NatureOfRisk {
    return NatureOfRisk(
      description = riskSummaryResponse.natureOfRisk ?: "",
      oasysHeading = OasysHeading(
        number = "10.2",
        description = "What is the nature of the risk?"
      )
    )
  }

  private suspend fun extractFactorsToReduceRisk(riskSummaryResponse: RiskSummaryResponse): FactorsToReduceRisk {
    return FactorsToReduceRisk(
      description = riskSummaryResponse.riskMitigationFactors ?: "",
      oasysHeading = OasysHeading(
        number = "10.5",
        description = "What factors are likely to reduce the risk?"
      )
    )
  }

  private suspend fun extractWhenRiskHighest(riskSummaryResponse: RiskSummaryResponse): WhenRiskHighest {
    return WhenRiskHighest(
      description = riskSummaryResponse.riskImminence ?: "",
      oasysHeading = OasysHeading(
        number = "10.3",
        description = "When is the risk likely to be greatest?"
      )
    )
  }

  private suspend fun extractCircumstancesIncreaseRisk(riskSummaryResponse: RiskSummaryResponse): CircumstancesIncreaseRisk {
    return CircumstancesIncreaseRisk(
      description = riskSummaryResponse.riskIncreaseFactors ?: "",
      oasysHeading = OasysHeading(
        number = "10.4",
        description = "What circumstances are likely to increase the risk?"
      )
    )
  }

  private suspend fun extractWhoIsAtRisk(riskSummaryResponse: RiskSummaryResponse): WhoIsAtRisk {
    return WhoIsAtRisk(
      description = riskSummaryResponse.whoIsAtRisk ?: "",
      oasysHeading = OasysHeading(
        number = "10.1",
        description = "Who is at risk?"
      )
    )
  }

  private suspend fun extractRiskOfSeriousHarm(riskSummaryResponse: RiskSummaryResponse): RiskOfSeriousHarm {
    val overallRisk = riskSummaryResponse.overallRiskLevel
    return RiskOfSeriousHarm(
      overallRisk = overallRisk ?: "",
      riskToChildren = getRiskLevel(riskSummaryResponse, "children") ?: "",
      riskToPublic = getRiskLevel(riskSummaryResponse, "public") ?: "",
      riskToKnownAdult = getRiskLevel(riskSummaryResponse, "known adult") ?: "",
      riskToStaff = getRiskLevel(riskSummaryResponse, "staff") ?: "",
      lastUpdated = riskSummaryResponse.assessedOn?.toLocalDate()
    )
  }

  private fun getRiskLevel(riskSummaryResponse: RiskSummaryResponse, key: String): String? {

    val veryHigh = riskSummaryResponse.riskInCommunity?.veryHigh
      ?.firstOrNull { it?.lowercase() == key }
    val high = riskSummaryResponse.riskInCommunity?.high
      ?.firstOrNull { it?.lowercase() == key }
    val medium = riskSummaryResponse.riskInCommunity?.medium
      ?.firstOrNull { it?.lowercase() == key }
    val low = riskSummaryResponse.riskInCommunity?.low
      ?.firstOrNull { it?.lowercase() == key }

    val risks = linkedMapOf<String?, String?>(
      "VERY_HIGH" to veryHigh, "HIGH" to high, "MEDIUM" to medium, "LOW" to low
    )

    val highestRecordedRiskLevel = risks.asIterable().firstOrNull { it.value != null }?.key

    return highestRecordedRiskLevel
  }

  private suspend fun fetchPersonalDetails(crn: String): RiskPersonalDetails {
    val offenderDetails = communityApiClient.getAllOffenderDetails(crn).awaitFirst()
    val age = offenderDetails?.dateOfBirth?.until(LocalDate.now())?.years
    val firstName = offenderDetails?.firstName ?: ""
    val surname = offenderDetails?.surname ?: ""
    val name = if (firstName.isEmpty()) {
      surname
    } else "$firstName $surname"

    return RiskPersonalDetails(
      name = name,
      dateOfBirth = offenderDetails?.dateOfBirth,
      age = age,
      gender = offenderDetails?.gender ?: "",
      crn = crn
    )
  }

  private suspend fun fetchMappa(crn: String): Mappa {
    val mappa = communityApiClient.getAllMappaDetails(crn).awaitFirst()
    val reviewDate = mappa.reviewDate?.format(
      DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.UK)
    )
    return Mappa(
      level = mappa.levelDescription ?: "",
      isNominal = true,
      lastUpdated = reviewDate
    )
  }
}
