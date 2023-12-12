package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class Offender(
  val locationDescription: String? = null,
  val bookingNo: String? = null,
  val facialImageId: Long? = null,
  val firstName: String? = null,
  val middleName: String? = "",
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val status: String? = null,
  val physicalAttributes: PhysicalAttributes? = null,
  val identifiers: List<Identifier>? = null,
  var image: String? = null,
  val sentenceDetail: SentenceDetail? = null,
)

data class PhysicalAttributes(
  val gender: String? = null,
  val ethnicity: String? = null,
)

data class Identifier(
  val type: String? = null,
  val value: String? = null,
)

data class SentenceDetail(
  val licenceExpiryDate: LocalDate? = null,
)
