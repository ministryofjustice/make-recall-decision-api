package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence
import java.time.LocalDate

data class OffenceWithLicenceConditions(
  val convictionId: Long?,
  val active: Boolean?,
  val offences: List<Offence>?,
  val sentenceDescription: String?,
  val sentenceOriginalLength: Int?,
  val sentenceOriginalLengthUnits: String?,
  val sentenceStartDate: LocalDate?,
  val licenceExpiryDate: LocalDate?,
  val postSentenceSupervisionEndDate: LocalDate?,
  val statusCode: String?,
  val statusDescription: String?,
  val licenceConditions: List<LicenceCondition>?
)
