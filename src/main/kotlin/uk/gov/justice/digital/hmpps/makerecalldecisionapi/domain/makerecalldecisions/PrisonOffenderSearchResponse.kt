package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class PrisonOffenderSearchResponse(
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
)

data class PhysicalAttributes(
  val gender: String,
  val ethnicity: String,
)

data class Identifier(
  val type: String,
  val value: String,
)
