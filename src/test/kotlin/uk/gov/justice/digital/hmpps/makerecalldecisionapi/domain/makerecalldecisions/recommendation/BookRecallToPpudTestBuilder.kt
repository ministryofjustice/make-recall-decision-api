package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate
import java.time.LocalDateTime

internal fun bookRecallToPpud(
  decisionDateTime: LocalDateTime? = randomLocalDateTime(),
  custodyGroup: String? = randomString(),
  custodyType: String? = randomString(),
  currentEstablishment: String? = randomString(),
  releasingPrison: String? = randomString(),
  indexOffence: String? = randomString(),
  indexOffenceComment: String? = randomString(),
  ppudSentenceId: String? = randomString(),
  ppudSentenceData: IndeterminateSentenceData? = IndeterminateSentenceData(
    offenceDescription = randomString(),
    releaseDate = randomLocalDate(),
    sentencingCourt = randomString(),
    dateOfSentence = randomLocalDate()
  ),
  mappaLevel: String? = randomString(),
  policeForce: String? = randomString(),
  probationArea: String? = randomString(),
  receivedDateTime: LocalDateTime? = randomLocalDateTime(),
  sentenceDate: LocalDate? = randomLocalDate(),
  gender: String? = randomString(),
  ethnicity: String? = randomString(),
  firstNames: String? = randomString(),
  lastName: String? = randomString(),
  dateOfBirth: LocalDate? = randomLocalDate(),
  cro: String? = randomString(),
  prisonNumber: String? = randomString(),
  legislationReleasedUnder: String? = randomString(),
  legislationSentencedUnder: String? = randomString(),
  minute: String? = randomString(),
) = BookRecallToPpud(
  decisionDateTime,
  custodyGroup,
  custodyType,
  currentEstablishment,
  releasingPrison,
  indexOffence,
  indexOffenceComment,
  ppudSentenceId,
  ppudSentenceData,
  mappaLevel,
  policeForce,
  probationArea,
  receivedDateTime,
  sentenceDate,
  gender,
  ethnicity,
  firstNames,
  lastName,
  dateOfBirth,
  cro,
  prisonNumber,
  legislationReleasedUnder,
  legislationSentencedUnder,
  minute,
)

internal fun BookRecallToPpud.toJsonBody() = json(toJsonString())

internal fun BookRecallToPpud.toJsonString() = ResourceLoader.CustomMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
