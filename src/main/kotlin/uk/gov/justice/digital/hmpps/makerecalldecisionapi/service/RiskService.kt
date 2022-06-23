package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.CircumstancesIncreaseRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.FactorsToReduceRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.HistoricalScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.NatureOfRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OGRS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OSPC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OSPI
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OasysHeading
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.PredictorScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RSR
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskOfSeriousHarm
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskPersonalDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Scores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.WhenRiskHighest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.WhoIsAtRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.CurrentScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.HistoricalScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Service
class RiskService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  @Qualifier("assessRisksNeedsApiClientUserEnhanced") private val arnApiClient: ArnApiClient
) {
  suspend fun getRisk(crn: String): RiskResponse {
    val userAccessResponse = getValue(communityApiClient.getUserAccess(crn))
    return if (true == userAccessResponse?.userExcluded || true == userAccessResponse?.userRestricted) {
      RiskResponse(userAccessResponse = userAccessResponse)
    } else {
      val riskSummaryResponse = getValue(arnApiClient.getRiskSummary(crn))
      val personalDetailsOverview = fetchPersonalDetails(crn)
      val riskOfSeriousHarm = extractRiskOfSeriousHarm(riskSummaryResponse!!)
      val natureOfRisk = extractNatureOfRisk(riskSummaryResponse)
      val whoIsAtRisk = extractWhoIsAtRisk(riskSummaryResponse)
      val circumstancesIncreaseRisk = extractCircumstancesIncreaseRisk(riskSummaryResponse)
      val factorsToReduceRisk = extractFactorsToReduceRisk(riskSummaryResponse)
      val whenRiskHighest = extractWhenRiskHighest(riskSummaryResponse)
      val mappa = handleFetchMappaApiCall(crn)
      val predictorScores = PredictorScores(
        current = fetchCurrentScores(crn),
        historical = fetchHistoricalScores(crn)
      )
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
  }

  private suspend fun fetchCurrentScores(crn: String): Scores {
    val currentScoresResponse = try {
      getValue(arnApiClient.getCurrentScores(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No cuurent scores available for CRN: $crn - ${e.message}")
      listOf(
        CurrentScoreResponse(
          completedDate = "",
          generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "", ogpDynamicWeightedScore = "", ogpTotalWeightedScore = "", ogpRisk = ""),
          riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "", scoreLevel = ""),
          sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "", ospContactPercentageScore = "", ospIndecentScoreLevel = "", ospContactScoreLevel = "")
        )
      )
    }
    val latestScores = currentScoresResponse.maxByOrNull { LocalDateTime.parse(it.completedDate) }
    val rsr = latestScores?.riskOfSeriousRecidivismScore
    val osp = latestScores?.sexualPredictorScore
    val osg = latestScores?.generalPredictorScore
    return Scores(
      rsr = RSR(level = rsr?.scoreLevel ?: "", score = rsr?.percentageScore ?: "", type = "RSR"),
      ospc = OSPC(level = osp?.ospContactScoreLevel ?: "", score = osp?.ospContactPercentageScore ?: "", type = "OSP/C"),
      ospi = OSPI(level = osp?.ospIndecentScoreLevel ?: "", score = osp?.ospIndecentPercentageScore ?: "", type = "OSP/I"),
      ogrs = OGRS(level = osg?.ogpRisk ?: "", score = osg?.ogpTotalWeightedScore, type = "OGRS") // TODO check if 'total' is correct field
    )
  }

  private suspend fun fetchHistoricalScores(crn: String): List<HistoricalScore> {
    val historicalScoresResponse = try {
      getValue(arnApiClient.getHistoricalScores(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No historical scores available for CRN: $crn - ${e.message}")
      listOf(
        HistoricalScoreResponse(
          rsrPercentageScore = "",
          rsrScoreLevel = "",
          ospcPercentageScore = "",
          ospcScoreLevel = "",
          ospiPercentageScore = "",
          ospiScoreLevel = "",
          calculatedDate = null
        )
      )
    }
    return historicalScoresResponse
      .map {
        HistoricalScore(
          date = it.calculatedDate?.let { it1 -> formatDateTimeStamp(it1) } ?: "",
          scores = Scores(
            rsr = RSR(level = it.rsrScoreLevel ?: "", score = it.rsrPercentageScore ?: "", type = "RSR"),
            ospc = OSPC(level = it.ospcScoreLevel ?: "", score = it.ospcPercentageScore ?: "", type = "OSP/C"),
            ospi = OSPI(level = it.ospiScoreLevel ?: "", score = it.ospiPercentageScore ?: "", type = "OSP/I"),
            ogrs = OGRS(level = "", score = "", type = "OGRS") // TODO - discuss with ARN team
          )
        )
      }
  }

  private fun formatDateTimeStamp(localDateTimeString: String): String {
    return LocalDateTime.parse(localDateTimeString).format(
      DateTimeFormatter.ofPattern("dd MMMM YYYY HH:mm")
        .withLocale(Locale.UK)
    )
  }

  private suspend fun handleFetchMappaApiCall(crn: String): Mappa? {
    return try { fetchMappa(crn) } catch (e: WebClientResponseException.NotFound) {
      log.info("No MAPPA details available for CRN: $crn - ${e.message}")
      Mappa(level = "", isNominal = true, lastUpdated = "")
    }
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
    val offenderDetails = getValue(communityApiClient.getAllOffenderDetails(crn))
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
    val mappaResponse = try {
      getValue(communityApiClient.getAllMappaDetails(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No MAPPA details available for CRN: $crn - ${e.message}")
      null
    }
    val reviewDate = mappaResponse?.reviewDate?.format(
      DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.UK)
    )
    return Mappa(
      level = mappaResponse?.levelDescription ?: "",
      isNominal = true,
      lastUpdated = reviewDate ?: ""
    )
  }

  private fun <T : Any> getValue(mono: Mono<T>?): T? {
    return try {
      val value = mono?.block()
      value ?: value
    } catch (wrappedException: RuntimeException) {
      when (wrappedException.cause) {
        is ClientTimeoutException -> throw wrappedException.cause as ClientTimeoutException
        is PersonNotFoundException -> throw wrappedException.cause as PersonNotFoundException
        else -> throw wrappedException
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
