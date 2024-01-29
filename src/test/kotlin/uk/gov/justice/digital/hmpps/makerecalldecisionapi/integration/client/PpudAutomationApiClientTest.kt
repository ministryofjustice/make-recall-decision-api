package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
class PpudAutomationApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var ppudAutomationApiClient: PpudAutomationApiClient

  @Test
  fun `retrieves licence matches`() {
    // given
    val croNumber = "123456/12A"
    val nomsId = "AB234A"

    ppudAutomationSearchApiMatchResponse(nomsId, croNumber)

    // when
    val actual = ppudAutomationApiClient.search(
      PpudSearchRequest(
        croNumber = croNumber,
        nomsId = nomsId,
        familyName = "Smith",
        dateOfBirth = LocalDate.of(2023, 1, 1),
      ),
    ).block()

    // then
    assertThat(actual.results[0].croNumber, equalTo(croNumber))
  }

  @Test
  fun `retrieve details`() {
    // given
    val id = "12345678"

    ppudAutomationDetailsMatchResponse(id)

    // when
    val actual = ppudAutomationApiClient.details(id).block()

    // then
    assertThat(actual.offender.id, equalTo(id))
  }

  @Test
  fun `book recall to ppud`() {
    // given
    val id = "12345678"
    val nomsId = "AB234A"

    ppudAutomationBookRecallApiMatchResponse(nomsId, id)

    // when
    val actual = ppudAutomationApiClient.bookToPpud(
      nomsId,
      PpudBookRecall(
        LocalDateTime.of(2023, 11, 1, 12, 5, 10),
        isInCustody = true,
        mappaLevel = "Level 3 – MAPPP",
        policeForce = "Kent Police",
        probationArea = "Merseyside",
        recommendedToOwner = "Consider a Recall Test(Recall 1)",
        receivedDateTime = LocalDateTime.of(2023, 11, 20, 11, 30),
        releaseDate = LocalDate.of(2023, 11, 5),
        riskOfContrabandDetails = "Smuggling in cigarettes",
        riskOfSeriousHarmLevel = "Low",
        sentenceDate = LocalDate.of(2023, 11, 4),
      ),
    ).block()

    // then
    assertThat(actual.recall.id, equalTo("12345678"))
  }

  @Test
  fun `create offender to ppud`() {
    // given
    val id = "12345678"

    ppudAutomationCreateOffenderApiMatchResponse(id)

    // when
    val actual = ppudAutomationApiClient.createOffender(
      PpudCreateOffender(
        croNumber = "A/2342",
        nomsId = "A897",
        prisonNumber = "123",
        firstNames = "Spuddy",
        familyName = "Spiffens",
        indexOffence = "bad language",
        ethnicity = "W",
        gender = "M",
        mappaLevel = "",
        custodyType = "Determinate",
        isInCustody = true,
        dateOfBirth = LocalDate.of(2004, 1, 1),
        dateOfSentence = LocalDate.of(2004, 1, 2),
        additionalAddresses = listOf(),
        address = PpudAddress(premises = "", line1 = "No Fixed Abode", line2 = "", postcode = "", phoneNumber = ""),
      ),
    ).block()

    // thenp
    assertThat(actual.offender.id, equalTo(id))
  }

  @Test
  fun `reference list`() {
    // given
    ppudAutomationReferenceListApiMatchResponse("custody-types")

    // when
    val actual = ppudAutomationApiClient.retrieveList("custody-types").block()

    // then
    assertThat(actual.values, equalTo(listOf("one", "two")))
  }
}
