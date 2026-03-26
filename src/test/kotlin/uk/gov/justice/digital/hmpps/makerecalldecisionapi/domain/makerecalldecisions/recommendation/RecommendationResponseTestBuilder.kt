package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

fun underIntegratedOffenderManagement(
  selected: String? = randomString(),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = UnderIntegratedOffenderManagement(
  selected = selected,
  allOptions = allOptions,
)

fun personOnProbation(
  name: String? = randomString(),
  firstName: String? = randomString(),
  surname: String? = randomString(),
  middleNames: String? = randomString(),
  gender: String? = randomString(),
  ethnicity: String? = randomString(),
  dateOfBirth: LocalDate? = randomLocalDate(),
  croNumber: String? = randomString(),
  mostRecentPrisonerNumber: String? = randomString(),
  nomsNumber: String? = randomString(),
  pncNumber: String? = randomString(),
  mappa: Mappa? = mappa(),
  addresses: List<Address>? = listOf(address()),
  primaryLanguage: String? = randomString(),
  hasBeenReviewed: Boolean? = randomBoolean(),
  ftr56MappaReviewed: Boolean? = randomBoolean(),
) = PersonOnProbation(
  name = name,
  firstName = firstName,
  surname = surname,
  middleNames = middleNames,
  gender = gender,
  ethnicity = ethnicity,
  dateOfBirth = dateOfBirth,
  croNumber = croNumber,
  mostRecentPrisonerNumber = mostRecentPrisonerNumber,
  nomsNumber = nomsNumber,
  pncNumber = pncNumber,
  mappa = mappa,
  addresses = addresses,
  primaryLanguage = primaryLanguage,
  hasBeenReviewed = hasBeenReviewed,
  ftr56MappaReviewed = ftr56MappaReviewed,
)

fun whoCompletedPartA(
  name: String? = randomString(),
  email: String? = randomString(),
  telephone: String? = randomString(),
  region: String? = randomString(),
  localDeliveryUnit: String? = randomString(),
  isPersonProbationPractitionerForOffender: Boolean? = randomBoolean(),
) = WhoCompletedPartA(
  name = name,
  email = email,
  telephone = telephone,
  region = region,
  localDeliveryUnit = localDeliveryUnit,
  isPersonProbationPractitionerForOffender = isPersonProbationPractitionerForOffender,
)

fun practitionerForPartA(
  name: String? = randomString(),
  email: String? = randomString(),
  telephone: String? = randomString(),
  region: String? = randomString(),
  localDeliveryUnit: String? = randomString(),
) = PractitionerForPartA(
  name = name,
  email = email,
  telephone = telephone,
  region = region,
  localDeliveryUnit = localDeliveryUnit,
)

fun prisonOffender(
  locationDescription: String? = randomString(),
  bookingNo: String? = randomString(),
  facialImageId: Long? = randomInt().toLong(),
  firstName: String? = randomString(),
  middleName: String? = randomString(),
  lastName: String? = randomString(),
  dateOfBirth: LocalDate? = randomLocalDate(),
  agencyId: String? = randomString(),
  agencyDescription: String? = randomString(),
  status: String? = randomString(),
  gender: String? = randomString(),
  ethnicity: String? = null,
  cro: String? = null,
  pnc: String? = null,
  image: String? = null,
  releaseDate: LocalDate? = null,
) = PrisonOffender(
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
  gender = gender,
  ethnicity = ethnicity,
  cro = cro,
  pnc = pnc,
  image = image,
  releaseDate = releaseDate,
)

fun convictionDetail(
  indexOffenceDescription: String? = randomString(),
  dateOfOriginalOffence: LocalDate? = randomLocalDate(),
  dateOfSentence: LocalDate? = randomLocalDate(),
  lengthOfSentence: Int? = randomInt(),
  lengthOfSentenceUnits: String? = randomString(),
  sentenceDescription: String? = randomString(),
  licenceExpiryDate: LocalDate? = randomLocalDate(),
  sentenceExpiryDate: LocalDate? = randomLocalDate(),
  sentenceSecondLength: Int? = randomInt(),
  sentenceSecondLengthUnits: String? = randomString(),
  custodialTerm: String? = randomString(),
  extendedTerm: String? = randomString(),
  hasBeenReviewed: Boolean? = randomBoolean(),
) = ConvictionDetail(
  indexOffenceDescription = indexOffenceDescription,
  dateOfOriginalOffence = dateOfOriginalOffence,
  dateOfSentence = dateOfSentence,
  lengthOfSentence = lengthOfSentence,
  lengthOfSentenceUnits = lengthOfSentenceUnits,
  sentenceDescription = sentenceDescription,
  licenceExpiryDate = licenceExpiryDate,
  sentenceExpiryDate = sentenceExpiryDate,
  sentenceSecondLength = sentenceSecondLength,
  sentenceSecondLengthUnits = sentenceSecondLengthUnits,
  custodialTerm = custodialTerm,
  extendedTerm = extendedTerm,
  hasBeenReviewed = hasBeenReviewed,
)
