package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CvlApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoCvlLicenceByIdException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertDateStringToIso8601Date

@Service
internal class CreateAndVaryALicenceService(
  @Qualifier("cvlApiClientUserEnhanced") private val cvlApiClient: CvlApiClient,
) {

  suspend fun buildLicenceConditions(crn: String, nomsId: String): List<LicenceConditionResponse> {
    val matchedLicences =
      getValueAndHandleWrappedException(cvlApiClient.getLicenceMatch(crn, LicenceConditionSearch(listOf(nomsId))))
    return matchedLicences
      ?.filter { it.crn.equals(crn) }
      ?.map { matched ->
        try {
          val licence = getValueAndHandleWrappedException(cvlApiClient.getLicenceById(crn, matched.licenceId))

          LicenceConditionResponse(
            licenceStatus = matched.licenceStatus,
            conditionalReleaseDate = convertDateStringToIso8601Date(licence?.conditionalReleaseDate),
            actualReleaseDate = convertDateStringToIso8601Date(licence?.actualReleaseDate),
            sentenceStartDate = convertDateStringToIso8601Date(licence?.sentenceStartDate),
            sentenceEndDate = convertDateStringToIso8601Date(licence?.sentenceEndDate),
            licenceStartDate = convertDateStringToIso8601Date(licence?.licenceStartDate),
            licenceExpiryDate = convertDateStringToIso8601Date(licence?.licenceExpiryDate),
            topupSupervisionStartDate = convertDateStringToIso8601Date(licence?.topupSupervisionStartDate),
            topupSupervisionExpiryDate = convertDateStringToIso8601Date(licence?.topupSupervisionExpiryDate),
            standardLicenceConditions = licence?.standardLicenceConditions?.map {
              LicenceConditionDetail(
                it.code,
                it.text,
              )
            },
            standardPssConditions = licence?.standardPssConditions?.map { LicenceConditionDetail(it.code, it.text) },
            additionalLicenceConditions = licence?.additionalLicenceConditions?.map {
              LicenceConditionDetail(
                it.code,
                it.text,
                it.expandedText,
                it.category,
              )
            },
            additionalPssConditions = licence?.additionalPssConditions?.map {
              LicenceConditionDetail(
                it.code,
                it.text,
                it.expandedText,
              )
            },
            bespokeConditions = licence?.bespokeConditions?.map { LicenceConditionDetail(it.code, it.text) },
          )
        } catch (ex: NoCvlLicenceByIdException) {
          log.error(ex.message)
          LicenceConditionResponse()
        } catch (ex: ClientTimeoutException) {
          throw ex
        } catch (ex: Exception) {
          log.error(ex.message)
          LicenceConditionResponse()
        }
      } ?: emptyList()
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
