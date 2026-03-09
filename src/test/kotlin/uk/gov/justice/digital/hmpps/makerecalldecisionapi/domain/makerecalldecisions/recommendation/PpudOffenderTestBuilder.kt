package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun ppudOffender(
  id: String = randomString(),
  croOtherNumber: String = randomString(),
  dateOfBirth: String = randomString(),
  ethnicity: String = randomString(),
  familyName: String = randomString(),
  firstNames: String = randomString(),
  gender: String = randomString(),
  immigrationStatus: String = randomString(),
  establishment: String? = randomString(),
  nomsId: String = randomString(),
  prisonerCategory: String = randomString(),
  prisonNumber: String = randomString(),
  sentences: List<PpudSentence> = listOf(ppudSentence()),
  status: String = randomString(),
  youngOffender: String = randomString(),
) = PpudOffender(
  id = id,
  croOtherNumber = croOtherNumber,
  dateOfBirth = dateOfBirth,
  ethnicity = ethnicity,
  familyName = familyName,
  firstNames = firstNames,
  gender = gender,
  immigrationStatus = immigrationStatus,
  establishment = establishment,
  nomsId = nomsId,
  prisonerCategory = prisonerCategory,
  prisonNumber = prisonNumber,
  sentences = sentences,
  status = status,
  youngOffender = youngOffender,
)

fun ppudSentence(
  id: String? = randomString(),
  offenceDescription: String? = randomString(),
  sentenceExpiryDate: String? = randomString(),
  dateOfSentence: String = randomString(),
  custodyType: String = randomString(),
  mappaLevel: String? = randomString(),
  licenceExpiryDate: String? = randomString(),
  tariffExpiryDate: String? = randomString(),
  offence: PpudOffence? = ppudOffence(),
  releaseDate: String? = randomString(),
  releases: List<PpudRelease>? = listOf(ppudRelease()),
  sentenceLength: PpudSentenceLength? = ppudSentenceLength(),
  sentencingCourt: String? = randomString(),
) = PpudSentence(
  id = id,
  offenceDescription = offenceDescription,
  sentenceExpiryDate = sentenceExpiryDate,
  dateOfSentence = dateOfSentence,
  custodyType = custodyType,
  mappaLevel = mappaLevel,
  licenceExpiryDate = licenceExpiryDate,
  tariffExpiryDate = tariffExpiryDate,
  offence = offence,
  releaseDate = releaseDate,
  releases = releases,
  sentenceLength = sentenceLength,
  sentencingCourt = sentencingCourt,
)

fun ppudOffence(
  indexOffence: String? = randomString(),
  dateOfIndexOffence: String? = randomLocalDate().toString(),
) = PpudOffence(
  indexOffence = indexOffence,
  dateOfIndexOffence = dateOfIndexOffence,
)

fun ppudRelease(
  category: String? = randomString(),
  dateOfRelease: String? = randomLocalDate().toString(),
  releasedFrom: String? = randomString(),
  releasedUnder: String? = randomString(),
  releaseType: String? = randomString(),
) = PpudRelease(
  category = category,
  dateOfRelease = dateOfRelease,
  releasedFrom = releasedFrom,
  releasedUnder = releasedUnder,
  releaseType = releaseType,
)

fun ppudSentenceLength(
  partYears: Int? = 0,
  partMonths: Int? = 0,
  partDays: Int? = 0,
) = PpudSentenceLength(
  partYears = partYears,
  partMonths = partMonths,
  partDays = partDays,
)
