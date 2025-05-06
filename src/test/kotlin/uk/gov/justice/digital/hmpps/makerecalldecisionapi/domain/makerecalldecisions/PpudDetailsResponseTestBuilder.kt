package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

/**
 * Helper functions for generating instances of classes related to
 * PPUD Details responses with their fields pre-filled with random
 * values. Intended for use in unit tests.
 */

internal fun ppudDetailsResponse(
  offender: OffenderDetails = offenderDetails(),
) = PpudDetailsResponse(offender)

internal fun PpudDetailsResponse.toJson() =
  json(toJsonString())

internal fun PpudDetailsResponse.toJsonString(): String =
  ResourceLoader.CustomMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

internal fun offenderDetails(
  id: String = randomString(),
  croOtherNumber: String = randomString(),
  dateOfBirth: LocalDate = randomLocalDate(),
  ethnicity: String = randomString(),
  familyName: String = randomString(),
  firstNames: String = randomString(),
  gender: String = randomString(),
  immigrationStatus: String = randomString(),
  establishment: String = randomString(),
  nomsId: String = randomString(),
  prisonerCategory: String = randomString(),
  prisonNumber: String = randomString(),
  sentences: List<SentenceDetails> = listOf(sentenceDetails()),
  status: String = randomString(),
  youngOffender: String = randomString(),
) = OffenderDetails(
  id,
  croOtherNumber,
  dateOfBirth.toString(),
  ethnicity,
  familyName,
  firstNames,
  gender,
  immigrationStatus,
  establishment,
  nomsId,
  prisonerCategory,
  prisonNumber,
  sentences,
  status,
  youngOffender,
)

internal fun OffenderDetails.toJson() =
  json(toJsonString())

internal fun OffenderDetails.toJsonString(): String =
  ResourceLoader.CustomMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

internal fun sentenceDetails(
  id: String? = randomString(),
  sentenceExpiryDate: String? = randomString(),
  dateOfSentence: String? = randomString(),
  custodyType: String? = randomString(),
  mappaLevel: String? = randomString(),
  licenceExpiryDate: String? = randomString(),
  tariffExpiryDate: String? = randomString(),
  offence: OffenceDetails? = offenceDetails(),
  releaseDate: String? = randomString(),
  sentenceLength: SentenceLength? = sentenceLength(),
  sentencingCourt: String? = randomString(),
) = SentenceDetails(
  id,
  sentenceExpiryDate,
  dateOfSentence,
  custodyType,
  mappaLevel,
  licenceExpiryDate,
  tariffExpiryDate,
  offence,
  releaseDate,
  sentenceLength,
  sentencingCourt,
)

internal fun SentenceDetails.toJson() =
  json(toJsonString())

internal fun SentenceDetails.toJsonString(): String =
  ResourceLoader.CustomMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

internal fun offenceDetails(
  indexOffence: String? = randomString(),
  dateOfIndexOffence: String? = randomString(),
) = OffenceDetails(indexOffence, dateOfIndexOffence)

internal fun OffenceDetails.toJson() =
  json(toJsonString())

internal fun OffenceDetails.toJsonString(): String =
  ResourceLoader.CustomMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

internal fun sentenceLength(
  partYears: Int? = randomInt(),
  partMonths: Int? = randomInt(),
  partDays: Int? = randomInt(),
) = SentenceLength(partYears, partMonths, partDays)

internal fun SentenceLength.toJson() =
  json(toJsonString())

internal fun SentenceLength.toJsonString(): String =
  ResourceLoader.CustomMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
