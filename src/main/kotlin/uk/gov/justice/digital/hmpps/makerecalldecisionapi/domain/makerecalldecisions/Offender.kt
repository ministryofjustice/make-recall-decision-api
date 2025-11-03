package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PrisonOffender
import java.time.LocalDate

data class Offender(
  val locationDescription: String? = null,
  val bookingNo: String? = null,
  val facialImageId: Long? = null,
  val firstName: String? = null,
  val middleName: String? = "",
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val agencyId: String? = null,
  var agencyDescription: String? = null,
  val status: String? = null,
  val physicalAttributes: PhysicalAttributes? = null,
  val identifiers: List<Identifier>? = null,
  var image: String? = null,
  val sentenceDetail: SentenceDetail? = null,
)

fun Offender.toPrisonOffender(): PrisonOffender = PrisonOffender(
  image = this.image,
  locationDescription = this.locationDescription,
  bookingNo = this.bookingNo,
  facialImageId = this.facialImageId,
  firstName = this.firstName,
  middleName = this.middleName,
  lastName = this.lastName,
  dateOfBirth = this.dateOfBirth,
  agencyId = this.agencyId,
  agencyDescription = this.agencyDescription,
  status = this.status,
  gender = this.physicalAttributes?.gender,
  ethnicity = this.physicalAttributes?.ethnicity,
  cro = this.identifiers?.find { id -> id.type == "CRO" }?.value,
  pnc = this.identifiers?.find { id -> id.type == "PNC" }?.value,
  releaseDate = this.sentenceDetail?.releaseDate,
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
  val releaseDate: LocalDate? = null,
)
