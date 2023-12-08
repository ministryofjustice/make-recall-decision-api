package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class Offender(
  val locationDescription: String,
  val bookingNo: String,
  val facialImageId: Long,
  val firstName: String,
  val middleName: String? = "",
  val lastName: String,
  val dateOfBirth: LocalDate,
  val status: String,
  val physicalAttributes: PhysicalAttributes,
  val identifiers: List<Identifier>,
  var image: String? = null,
  val sentenceDetail: SentenceDetail? = null,
)

data class PhysicalAttributes(
  val gender: String,
  val ethnicity: String,
)

data class Identifier(
  val type: String,
  val value: String,
)

data class SentenceDetail(
  val licenceExpiryDate: LocalDate? = null,
)
