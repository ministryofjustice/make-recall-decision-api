package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CvlApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoCvlLicenceByIdException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoCvlLicenceMatchException

@Service
internal class CreateAndVaryALicenceService(
  @Qualifier("cvlApiClientUserEnhanced") private val cvlApiClient: CvlApiClient
) {

  suspend fun buildLicenceConditions(crn: String, nomsId: String): List<LicenceConditionResponse?> {

    val matchedLicences = try {
      getValueAndHandleWrappedException(cvlApiClient.getLicenceMatch(crn, LicenceConditionSearch(listOf(nomsId))))
    } catch (ex: NoCvlLicenceMatchException) {
      log.info(ex.message)
      return emptyList()
    }

    return matchedLicences
      ?.filter { it.crn.equals(crn) }
      ?.map { matched ->
        try {
          val licence = getValueAndHandleWrappedException(cvlApiClient.getLicenceById(crn, matched.licenceId))

          LicenceConditionResponse(
            conditionalReleaseDate = licence?.conditionalReleaseDate,
            actualReleaseDate = licence?.actualReleaseDate,
            sentenceStartDate = licence?.sentenceStartDate,
            sentenceEndDate = licence?.sentenceEndDate,
            licenceStartDate = licence?.licenceStartDate,
            licenceExpiryDate = licence?.licenceExpiryDate,
            topupSupervisionStartDate = licence?.topupSupervisionStartDate,
            topupSupervisionExpiryDate = licence?.topupSupervisionExpiryDate,
            standardLicenceConditions = licence?.standardLicenceConditions?.map { LicenceConditionDetail(it.text) },
            standardPssConditions = licence?.standardPssConditions?.map { LicenceConditionDetail(it.text) },
            additionalLicenceConditions = licence?.additionalLicenceConditions?.map { LicenceConditionDetail(it.text, it.expandedText) },
            additionalPssConditions = licence?.additionalPssConditions?.map { LicenceConditionDetail(it.text, it.expandedText) },
            bespokeConditions = licence?.bespokeConditions?.map { LicenceConditionDetail(it.text) }
          )
        } catch (ex: NoCvlLicenceByIdException) {
          log.info(ex.message)
          LicenceConditionResponse()
        }
      } ?: emptyList()
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
