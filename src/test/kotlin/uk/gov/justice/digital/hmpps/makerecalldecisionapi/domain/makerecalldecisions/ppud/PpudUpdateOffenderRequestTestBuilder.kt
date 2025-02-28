package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppud

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.toJsonNullableStringField
import java.time.LocalDate

internal fun ppudUpdateOffenderRequest(
  address: PpudAddress = ppudAddress(),
  additionalAddresses: List<PpudAddress> = listOf(ppudAddress()),
  croNumber: String? = randomLong().toString(),
  dateOfBirth: LocalDate = randomLocalDate(),
  ethnicity: String = randomString(),
  familyName: String = randomString(),
  firstNames: String = randomString(),
  gender: String = randomString(),
  isInCustody: Boolean = randomBoolean(),
  nomsId: String? = randomString(),
  prisonNumber: String = randomString(),
  establishment: String = randomString(),
) =
  PpudUpdateOffenderRequest(
    address,
    additionalAddresses,
    croNumber,
    dateOfBirth,
    ethnicity,
    familyName,
    firstNames,
    gender,
    isInCustody,
    nomsId,
    prisonNumber,
    establishment,
  )

internal fun PpudUpdateOffenderRequest.toJsonBody() = json(toJsonString())

internal fun PpudUpdateOffenderRequest.toJsonString() =
  """
      {
          "address" : ${address.toJsonString()},
          "additionalAddresses" : [${additionalAddresses.joinToString(", ") { it.toJsonString() }}],
          "croNumber" : ${toJsonNullableStringField(croNumber)},
          "dateOfBirth" : ${toJsonNullableStringField(dateOfBirth)},
          "ethnicity" : ${toJsonNullableStringField(ethnicity)},
          "familyName" : ${toJsonNullableStringField(familyName)},
          "firstNames" : ${toJsonNullableStringField(firstNames)},
          "gender" : ${toJsonNullableStringField(gender)},
          "isInCustody" : $isInCustody,
          "nomsId" : ${toJsonNullableStringField(nomsId)},
          "prisonNumber" : ${toJsonNullableStringField(prisonNumber)},
          "establishment" : ${toJsonNullableStringField(establishment)}
        }
  """.trimIndent()
