package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceCondition
import java.time.LocalDate

data class ConvictionResponse(
  val convictionId: Long? = null,
  val active: Boolean? = null,
  val offences: List<Offence>? = null,
  val sentenceDescription: String? = null,
  val sentenceOriginalLength: Int? = null,
  val sentenceOriginalLengthUnits: String? = null,
  val sentenceStartDate: LocalDate? = null,
  val sentenceExpiryDate: LocalDate? = null,
  val licenceExpiryDate: LocalDate? = null,
  val postSentenceSupervisionEndDate: LocalDate? = null,
  val statusCode: String? = null,
  val statusDescription: String? = null,
  val licenceConditions: List<LicenceCondition>? = null,
  val licenceDocuments: List<CaseDocument>? = null,
)
