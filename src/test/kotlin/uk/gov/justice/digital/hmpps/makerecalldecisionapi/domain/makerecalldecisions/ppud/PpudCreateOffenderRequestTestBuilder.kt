package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppud

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.toJsonNullableStringField
import java.time.LocalDate

internal fun ppudCreateOffenderRequest(
  address: PpudAddress = ppudAddress(),
  additionalAddresses: List<PpudAddress> = listOf(ppudAddress()),
  croNumber: String? = randomString(),
  custodyType: String? = randomString(),
  establishment: String? = randomString(),
  dateOfBirth: LocalDate? = randomLocalDate(),
  dateOfSentence: LocalDate? = randomLocalDate(),
  ethnicity: String? = randomString(),
  firstNames: String? = randomString(),
  familyName: String? = randomString(),
  gender: String? = randomString(),
  indexOffence: String? = randomString(),
  isInCustody: Boolean? = randomBoolean(),
  mappaLevel: String? = randomString(),
  nomsId: String? = randomString(),
  prisonNumber: String? = randomString(),
) = PpudCreateOffenderRequest(
  address,
  additionalAddresses,
  croNumber,
  custodyType,
  establishment,
  dateOfBirth,
  dateOfSentence,
  ethnicity,
  firstNames,
  familyName,
  gender,
  indexOffence,
  isInCustody,
  mappaLevel,
  nomsId,
  prisonNumber,
)

internal fun PpudCreateOffenderRequest.toJsonBody() = json(toJsonString())

internal fun PpudCreateOffenderRequest.toJsonString() =
  """
      {
          "address" : ${address.toJsonString()},
          "additionalAddresses" : [${additionalAddresses.joinToString(", ") { it.toJsonString() }}],
          "croNumber" : ${toJsonNullableStringField(croNumber)},
          "custodyType" : ${toJsonNullableStringField(custodyType)},
          "establishment" : ${toJsonNullableStringField(establishment)},
          "dateOfBirth" : ${toJsonNullableStringField(dateOfBirth)},
          "dateOfSentence" : ${toJsonNullableStringField(dateOfSentence)},
          "ethnicity" : ${toJsonNullableStringField(ethnicity)},
          "firstNames" : ${toJsonNullableStringField(firstNames)},
          "familyName" : ${toJsonNullableStringField(familyName)},
          "gender" : ${toJsonNullableStringField(gender)},
          "indexOffence" : ${toJsonNullableStringField(indexOffence)},
          "isInCustody" : $isInCustody,
          "mappaLevel" : ${toJsonNullableStringField(mappaLevel)},
          "nomsId" : ${toJsonNullableStringField(nomsId)},
          "prisonNumber" : ${toJsonNullableStringField(prisonNumber)}
        }
  """.trimIndent()
