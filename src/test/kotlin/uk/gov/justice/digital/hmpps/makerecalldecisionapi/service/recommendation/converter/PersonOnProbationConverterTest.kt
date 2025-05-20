package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbationDto
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

class PersonOnProbationConverterTest {
  private val converter = PersonOnProbationConverter()

  @Test
  fun convertsCorrectly() {
    val firstName = "Joe"
    val surname = "Michael"
    val middleNames = "Bloggs"
    val personOnProbation = PersonOnProbation(
      name = "Joseph",
      firstName = firstName,
      surname = surname,
      middleNames = middleNames,
      gender = randomString(),
      ethnicity = randomString(),
      dateOfBirth = LocalDate.parse("1982-10-24"),
      croNumber = randomString(),
      mostRecentPrisonerNumber = randomString(),
      nomsNumber = randomString(),
      pncNumber = randomString(),
      mappa = Mappa(level = 1, category = 1, lastUpdatedDate = null),
      addresses = listOf(
        Address(
          line1 = "Line 1 address",
          line2 = "Line 2 address",
          town = "Town address",
          postcode = "TS1 1ST",
          noFixedAbode = false,
        ),
      ),
      primaryLanguage = "English",
      hasBeenReviewed = randomBoolean(),
    )

    val expectedPersonOnProbationDto = PersonOnProbationDto(
      fullName = "$firstName $middleNames $surname",
      name = personOnProbation.name,
      firstName = firstName,
      surname = surname,
      middleNames = middleNames,
      gender = personOnProbation.gender,
      ethnicity = personOnProbation.ethnicity,
      dateOfBirth = personOnProbation.dateOfBirth,
      croNumber = personOnProbation.croNumber,
      mostRecentPrisonerNumber = personOnProbation.mostRecentPrisonerNumber,
      nomsNumber = personOnProbation.nomsNumber,
      pncNumber = personOnProbation.pncNumber,
      mappa = personOnProbation.mappa,
      addresses = personOnProbation.addresses,
      primaryLanguage = personOnProbation.primaryLanguage,
      hasBeenReviewed = personOnProbation.hasBeenReviewed,
    )

    val actualPersonOnProbationDto = converter.convert(personOnProbation)

    assertThat(actualPersonOnProbationDto).isEqualTo(expectedPersonOnProbationDto)
  }
}
