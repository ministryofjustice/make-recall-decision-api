package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionResponse
import java.util.Collections

@Service
class LicenceConditionsCoordinator {
  internal fun selectLicenceConditions(nDeliusLicenceConditions: LicenceConditions, cvlLicenceConditions: List<LicenceConditionResponse?>): SelectedLicenceConditions {
    val hasAllConvictionsReleasedOnLicence = hasAllConvictionsReleasedOnLicence(nDeliusLicenceConditions)
    val onLicenceCvlWithLaterOrSameStartDate = isCvlLicenceMoreOrAsRecent(nDeliusLicenceConditions, cvlLicenceConditions, hasAllConvictionsReleasedOnLicence == true)
    val source = if (onLicenceCvlWithLaterOrSameStartDate) "cvl" else "nDelius"

    return SelectedLicenceConditions(
      source = source,
      hasAllConvictionsReleasedOnLicence = hasAllConvictionsReleasedOnLicence,
      ndeliusLicenceConditions = nDeliusLicenceConditions,
      cvlLicenceConditions = cvlLicenceConditions
    )
  }

  private fun hasAllConvictionsReleasedOnLicence(nDeliusLicenceConditions: LicenceConditions): Boolean? {
    return if (nDeliusLicenceConditions.activeConvictions.isNotEmpty()) {
      nDeliusLicenceConditions.activeConvictions.all {
        it.sentence?.isCustodial == true && it.sentence.custodialStatusCode == "B"
      }
    } else null
  }

  fun isCvlLicenceMoreOrAsRecent(nDeliusLicenceConditions: LicenceConditions, cvlLicenceConditions: List<LicenceConditionResponse?>, onLicence: Boolean): Boolean {
    val cvlLicenceStartDates = cvlLicenceConditions
      .filter { it?.licenceStatus == "ACTIVE" }
      .map { it?.licenceStartDate }
    Collections.sort(cvlLicenceStartDates)

    val cvlLicenceMoreRecent = nDeliusLicenceConditions.activeConvictions
      .filter { it.sentence?.isCustodial == true && it.sentence.custodialStatusCode == "B" }
      .map { it.sentence?.licenceStartDate }
      .all { cvlLicenceStartDates.isNotEmpty() && (it?.isBefore(cvlLicenceStartDates[0]) == true || it?.isEqual(cvlLicenceStartDates[0]) == true) }

    return onLicence && cvlLicenceMoreRecent
  }
}

data class SelectedLicenceConditions(
  val source: String? = null,
  val hasAllConvictionsReleasedOnLicence: Boolean? = null,
  val ndeliusLicenceConditions: LicenceConditions? = null,
  val cvlLicenceConditions: List<LicenceConditionResponse?>? = null
)
