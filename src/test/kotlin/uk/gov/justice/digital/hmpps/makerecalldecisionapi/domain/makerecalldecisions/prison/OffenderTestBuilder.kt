package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Identifier
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PhysicalAttributes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

fun offender(
  locationDescription: String? = randomString(),
  bookingNo: String? = randomString(),
  facialImageId: Long? = null,
  firstName: String? = randomString(),
  middleName: String? = randomString(),
  lastName: String? = randomString(),
  dateOfBirth: LocalDate? = null,
  agencyId: String? = randomString(),
  agencyDescription: String? = randomString(),
  status: String? = randomString(),
  physicalAttributes: PhysicalAttributes? = physicalAttributes(),
  identifiers: List<Identifier>? = listOf(identifier()),
  image: String? = randomString(),
  sentenceDetail: SentenceDetail? = sentenceDetail(),
) = Offender(
  locationDescription = locationDescription,
  bookingNo = bookingNo,
  facialImageId = facialImageId,
  firstName = firstName,
  middleName = middleName,
  lastName = lastName,
  dateOfBirth = dateOfBirth,
  agencyId = agencyId,
  agencyDescription = agencyDescription,
  status = status,
  physicalAttributes = physicalAttributes,
  identifiers = identifiers,
  image = image,
  sentenceDetail = sentenceDetail,
)

fun physicalAttributes(
  gender: String? = randomString(),
  ethnicity: String? = randomString(),
) = PhysicalAttributes(
  gender,
  ethnicity,
)

fun identifier(
  type: String? = randomString(),
  value: String? = randomString(),
) = Identifier(
  type,
  value,
)

fun sentenceDetail(
  licenceExpiryDate: LocalDate? = randomLocalDate(),
  releaseDate: LocalDate? = randomLocalDate(),
) = SentenceDetail(
  licenceExpiryDate,
  releaseDate,
)