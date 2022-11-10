package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OverviewConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import kotlin.streams.toList

@Service
internal class ConvictionService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val documentService: DocumentService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun buildConvictionResponse(crn: String, shouldGetDocuments: Boolean): List<ConvictionResponse> {
    log.info(StringUtils.normalizeSpace("About to build conviction response for $crn in conviction service"))

    val activeConvictions = getValueAndHandleWrappedException(communityApiClient.getActiveConvictions(crn))

    log.info(StringUtils.normalizeSpace("Got conviction response for $crn in conviction service."))

    val allConvictionDocuments = if (shouldGetDocuments) documentService.getDocumentsByDocumentType(crn, "CONVICTION_DOCUMENT") else null

    return activeConvictions
      ?.map {
        log.info(StringUtils.normalizeSpace("About to get licence conditions by conviction id " + it.convictionId + " for $crn in conviction service"))
        val licenceConditions = getValueAndHandleWrappedException(communityApiClient.getLicenceConditionsByConvictionId(crn, it.convictionId))
          ?.licenceConditions
        log.info(StringUtils.normalizeSpace("Got licence conditions by conviction id " + it.convictionId + " for $crn in conviction service"))

        ConvictionResponse(
          convictionId = it.convictionId,
          active = it.active,
          offences = getActiveConvictions(it),
          sentenceDescription = it.sentence?.description,
          sentenceOriginalLength = it.sentence?.originalLength,
          sentenceOriginalLengthUnits = it.sentence?.originalLengthUnits,
          sentenceSecondLength = it.sentence?.secondLength,
          sentenceSecondLengthUnits = it.sentence?.secondLengthUnits,
          sentenceStartDate = it.sentence?.startDate,
          licenceExpiryDate = it.custody?.keyDates?.licenceExpiryDate,
          sentenceExpiryDate = it.custody?.keyDates?.sentenceExpiryDate,
          postSentenceSupervisionEndDate = it.custody?.keyDates?.postSentenceSupervisionEndDate,
          statusCode = it.custody?.status?.code,
          statusDescription = it.custody?.status?.description,
          licenceConditions = licenceConditions,
          licenceDocuments = allConvictionDocuments?.filter { document -> document.parentPrimaryKeyId == it.convictionId },
          isCustodial = it.isCustodial
        )
      } ?: emptyList()
  }

  suspend fun buildConvictionResponseForOverview(crn: String): List<OverviewConvictionResponse> {

    val activeConvictions = getValueAndHandleWrappedException(communityApiClient.getActiveConvictions(crn))

    return activeConvictions
      ?.map {
        OverviewConvictionResponse(
          active = it.active,
          offences = getActiveConvictions(it),
          sentenceDescription = it.sentence?.description,
          sentenceOriginalLength = it.sentence?.originalLength,
          sentenceOriginalLengthUnits = it.sentence?.originalLengthUnits,
          sentenceExpiryDate = it.custody?.keyDates?.sentenceExpiryDate,
          licenceExpiryDate = it.custody?.keyDates?.licenceExpiryDate,
          isCustodial = it.isCustodial
        )
      } ?: emptyList()
  }

  private fun getActiveConvictions(conviction: Conviction): List<Offence>? {
    return conviction.offences
      ?.stream()?.toList()
      ?.map {
        Offence(
          mainOffence = it.mainOffence, description = it.detail?.description ?: "", code = it.detail?.code ?: "", offenceDate = it.offenceDate
        )
      }
  }
}
