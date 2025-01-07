package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.toJsonNullableStringField
import java.time.LocalDate
import java.time.LocalDateTime

internal fun bookRecallToPpud(
  decisionDateTime: LocalDateTime? = randomLocalDateTime(),
  custodyType: String? = randomString(),
  releasingPrison: String? = randomString(),
  indexOffence: String? = randomString(),
  ppudSentenceId: String? = randomString(),
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
  custodyType,
  releasingPrison,
  indexOffence,
  ppudSentenceId,
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

internal fun BookRecallToPpud.toJsonString() =
  """
      {
          "decisionDateTime":${toJsonNullableStringField(decisionDateTime)},
          "custodyType":${toJsonNullableStringField(custodyType)},
          "releasingPrison":${toJsonNullableStringField(releasingPrison)},
          "indexOffence":${toJsonNullableStringField(indexOffence)},
          "ppudSentenceId":${toJsonNullableStringField(ppudSentenceId)},
          "mappaLevel":${toJsonNullableStringField(mappaLevel)},
          "policeForce":${toJsonNullableStringField(policeForce)},
          "probationArea":${toJsonNullableStringField(probationArea)},
          "receivedDateTime":${toJsonNullableStringField(receivedDateTime)},
          "sentenceDate":${toJsonNullableStringField(sentenceDate)},
          "gender":${toJsonNullableStringField(gender)},
          "ethnicity":${toJsonNullableStringField(ethnicity)},
          "firstNames":${toJsonNullableStringField(firstNames)},
          "lastName":${toJsonNullableStringField(lastName)},
          "dateOfBirth":${toJsonNullableStringField(dateOfBirth)},
          "cro":${toJsonNullableStringField(cro)},
          "prisonNumber":${toJsonNullableStringField(prisonNumber)},
          "legislationReleasedUnder":${toJsonNullableStringField(legislationReleasedUnder)},
          "legislationSentencedUnder":${toJsonNullableStringField(legislationSentencedUnder)},
          "minute":${toJsonNullableStringField(minute)}
        }
    """.trimIndent()