package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionResponse

@Service
class LicenceConditionsCoordinator {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  internal fun selectLicenceConditions(
    nDeliusLicenceConditions: LicenceConditions,
    cvlLicenceConditions: List<LicenceConditionResponse>,
  ): SelectedLicenceConditions {
    val hasAllConvictionsReleasedOnLicence = hasAllConvictionsReleasedOnLicence(nDeliusLicenceConditions)
    val onLicenceCvlWithLaterOrSameStartDate =
      isCvlLicenceMoreOrAsRecent(nDeliusLicenceConditions, cvlLicenceConditions, hasAllConvictionsReleasedOnLicence)
    return SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = hasAllConvictionsReleasedOnLicence,
      ndeliusActiveConvictions = nDeliusLicenceConditions.activeConvictions.filter { it.sentence?.isCustodial == true },
      cvlLicenceCondition = if (onLicenceCvlWithLaterOrSameStartDate) cvlLicenceConditions.firstOrNull() else null,
    )
  }

  private fun hasAllConvictionsReleasedOnLicence(nDeliusLicenceConditions: LicenceConditions): Boolean {
    log.info("LC Issue Logging::" + " number of active convictions::" + nDeliusLicenceConditions.activeConvictions.size)
    nDeliusLicenceConditions.activeConvictions.forEach {
      log.info("LC Issue Logging::" + " active conviction number::" + nDeliusLicenceConditions.activeConvictions.indexOf(it))
      log.info("LC Issue Logging::" + " custodialStatusCode::" + it.sentence?.custodialStatusCode)
      log.info("LC Issue Logging::" + " isCustodial::" + it.sentence?.isCustodial)
    }
    val hasAllConvictionsReleasedOnLicence = nDeliusLicenceConditions.activeConvictions.isNotEmpty() &&
      nDeliusLicenceConditions.activeConvictions.any { it.sentence?.isCustodial == true } &&
      nDeliusLicenceConditions.activeConvictions
        .filter { it.sentence?.isCustodial == true }
        .all { it.sentence?.custodialStatusCode == "B" }
    log.info("LC Issue Logging::" + " hasAllConvictionsReleasedOnLicence::" + hasAllConvictionsReleasedOnLicence)
    return hasAllConvictionsReleasedOnLicence
  }

  private fun isCvlLicenceMoreOrAsRecent(
    nDeliusLicenceConditions: LicenceConditions,
    cvlLicenceConditions: List<LicenceConditionResponse>,
    onLicence: Boolean,
  ): Boolean {
    val cvlLicenceStartDates =
      cvlLicenceConditions.filter { it.licenceStatus == "ACTIVE" }.mapNotNull { it.licenceStartDate }.sortedDescending()

    val cvlLicenceMoreRecent = nDeliusLicenceConditions.activeConvictions.filter { it.sentence?.isCustodial == true }
      .flatMap { it.licenceConditions }.map { it.startDate }.all {
        cvlLicenceStartDates.isNotEmpty() &&
          (it.isBefore(cvlLicenceStartDates[0]) || it.isEqual(cvlLicenceStartDates[0]))
      }
    return onLicence && cvlLicenceMoreRecent
  }
}

data class SelectedLicenceConditions(
  val hasAllConvictionsReleasedOnLicence: Boolean,
  val ndeliusActiveConvictions: List<LicenceConditions.ConvictionWithLicenceConditions> = emptyList(),
  val cvlLicenceCondition: LicenceConditionResponse? = null,
)
