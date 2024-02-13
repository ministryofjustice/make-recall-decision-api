package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudContactWithTelephone
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdatePostRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateSentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUser
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceLength
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
        recommendedTo = PpudUser("Consider a Recall Test", "Recall 1"),
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

    // then
    assertThat(actual.offender.id, equalTo(id))
  }

  @Test
  fun `update sentence to ppud`() {
    // given
    val offenderId = "123"
    val sentenceId = "456"
    val id = "12345678"

    ppudAutomationUpdateSentenceApiMatchResponse(offenderId, sentenceId, id)

    // when
    ppudAutomationApiClient.updateSentence(
      offenderId,
      sentenceId,
      PpudUpdateSentence(
        custodyType = "Determinate",
        dateOfSentence = LocalDate.of(2004, 1, 2),
        licenceExpiryDate = LocalDate.of(2004, 1, 3),
        mappaLevel = "1",
        releaseDate = LocalDate.of(2004, 1, 4),
        sentenceLength = SentenceLength(1, 1, 1),
        sentenceExpiryDate = LocalDate.of(2004, 1, 5),
        sentencingCourt = "sentencing court",
      ),
    ).block()

    // then
    // no exception
  }

  @Test
  fun `update offence in ppud`() {
    // given
    val offenderId = "123"
    val sentenceId = "456"

    ppudAutomationUpdateOffenceApiMatchResponse(offenderId, sentenceId)

    // when
    ppudAutomationApiClient.updateOffence(
      offenderId,
      sentenceId,
      PpudUpdateOffence(
        indexOffence = "some dastardly deed",
        dateOfIndexOffence = LocalDate.of(2016, 1, 1),
      ),
    ).block()

    // then
    // no exception
  }

  @Test
  fun `update release to ppud`() {
    // given
    val offenderId = "123"
    val sentenceId = "456"
    val id = "12345678"

    ppudAutomationUpdateReleaseApiMatchResponse(offenderId, sentenceId, id)

    // when
    val actual = ppudAutomationApiClient.createOrUpdateRelease(
      offenderId,
      sentenceId,
      PpudCreateOrUpdateRelease(
        dateOfRelease = LocalDate.of(2016, 1, 1),
        postRelease = PpudUpdatePostRelease(
          assistantChiefOfficer = PpudContact(
            name = "Mr A",
            faxEmail = "1234",
          ),
          offenderManager = PpudContactWithTelephone(
            name = "Mr B",
            faxEmail = "567",
            telephone = "1234",
          ),
          probationService = "Argyl",
          spoc = PpudContact(
            name = "Mr C",
            faxEmail = "123",
          ),
        ),
        releasedFrom = "Hull",
        releasedUnder = "Duress",
      ),
    ).block()

    // then
    assertThat(actual.release.id, equalTo(id))
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
