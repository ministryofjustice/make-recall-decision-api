package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class OverviewConvictionResponse(
  val active: Boolean? = null,
  val offences: List<Offence>? = null,
  val sentenceDescription: String? = null,
  val sentenceOriginalLength: Int? = null,
  val sentenceOriginalLengthUnits: String? = null,
  val sentenceExpiryDate: LocalDate? = null,
  val licenceExpiryDate: LocalDate? = null,
  val isCustodial: Boolean? = null,
  val statusCode: String? = null
)
