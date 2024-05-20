package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateMinuteRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudContactWithTelephone
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdatePostRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUser
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudYearMonth
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceLength
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationSupportingDocumentEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationSupportingDocumentRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Suppress("SameParameterValue")
@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PpudControllerTest : IntegrationTestBase() {

  @Autowired
  private lateinit var ppudUserRepository: PpudUserRepository

  @Autowired
  private lateinit var recommendationDocumentRepository: RecommendationSupportingDocumentRepository

  @Test
  fun `given request including null CRO Number when search is called then any matching results are returned`() {
    ppudAutomationSearchApiMatchResponse("A1234AB", "123456/12A")
    runTest {
      val requestBody = "{" +
        "\"croNumber\": null, " +
        "\"nomsId\": \"A1234AB\", " +
        "\"familyName\": \"Teal\", " +
        "\"dateOfBirth\": \"2000-01-01\"" +
        "}"

      postToSearch(requestBody)
        .expectStatus().isOk
    }
  }

  @Test
  fun `given request including null NOMIS ID when search is called then any matching results are returned`() {
    ppudAutomationSearchApiMatchResponse("A1234AB", "123456/12A")
    runTest {
      val requestBody = "{" +
        "\"croNumber\": \"123456/12A\", " +
        "\"nomsId\": null, " +
        "\"familyName\": \"Teal\", " +
        "\"dateOfBirth\": \"2000-01-01\"" +
        "}"

      postToSearch(requestBody)
        .expectStatus().isOk
    }
  }

  @Test
  fun `given details request`() {
    ppudAutomationDetailsMatchResponse("12345678")

    runTest {
      webTestClient.post()
        .uri("/ppud/details/12345678")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    }
  }

  private fun postToSearch(requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/ppud/search")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  @Test
  fun `book recall`() {
    ppudAutomationBookRecallApiMatchResponse("A1234AB", "12345678")
    runTest {
      postToBookRecall(
        "A1234AB",
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
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud create offender`() {
    ppudAutomationCreateOffenderApiMatchResponse("12345678")
    runTest {
      postToCreateOffender(
        PpudCreateOffenderRequest(
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
          address = PpudAddress(
            premises = "",
            line1 = "No Fixed Abode",
            line2 = "",
            postcode = "",
            phoneNumber = "",
          ),
        ),
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud create offender accepts null CRO Number and NOMIS ID`() {
    ppudAutomationCreateOffenderApiMatchResponse("12345678")
    val requestBody = """
      {
        "croNumber": null,
        "nomsId": null,
        "prisonNumber": "123",
        "firstNames": "Peter",
        "familyName": "Parker",
        "ethnicity": "W",
        "gender": "M",
        "isInCustody": true,
        "mappaLevel": "",
        "custodyType": "Determinate",
        "dateOfBirth": "2004-01-01",
        "additionalAddresses": [],
        "address": {
          "premises": "",
          "line1": "No Fixed Abode",
          "line2": "",
          "postcode": "",
          "phoneNumber": ""
         }
      }
    """.trimIndent()
    runTest {
      postToCreateOffender(requestBody)
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud update offender`() {
    ppudAutomationUpdateOffenderApiMatchResponse("12345678")
    runTest {
      putToUpdateOffender(
        "12345678",
        PpudUpdateOffenderRequest(
          croNumber = "A/2342",
          nomsId = "A897",
          prisonNumber = "123",
          firstNames = "Spuddy",
          familyName = "Spiffens",
          ethnicity = "W",
          gender = "M",
          isInCustody = true,
          dateOfBirth = LocalDate.of(2004, 1, 1),
          additionalAddresses = listOf(),
          address = PpudAddress(
            premises = "",
            line1 = "No Fixed Abode",
            line2 = "",
            postcode = "",
            phoneNumber = "",
          ),
        ),
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud update offender accepts null CRO Number and NOMS ID`() {
    ppudAutomationUpdateOffenderApiMatchResponse("12345678")
    val requestBody = """
      {
        "croNumber": null,
        "nomsId": null,
        "prisonNumber": "123",
        "firstNames": "Peter",
        "familyName": "Parker",
        "ethnicity": "W",
        "gender": "M",
        "isInCustody": true,
        "dateOfBirth": "2004-01-01",
        "additionalAddresses": [],
        "address": {
          "premises": "",
          "line1": "No Fixed Abode",
          "line2": "",
          "postcode": "",
          "phoneNumber": ""
        }
      }
    """.trimIndent()
    runTest {
      putToUpdateOffender("12345678", requestBody)
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud create sentence`() {
    ppudAutomationCreateSentenceApiMatchResponse("123", "12345678")
    runTest {
      postToCreateSentence(
        "123",
        PpudCreateOrUpdateSentenceRequest(
          custodyType = "Determinate",
          dateOfSentence = LocalDate.of(2004, 1, 2),
          licenceExpiryDate = LocalDate.of(2004, 1, 3),
          mappaLevel = "1",
          releaseDate = LocalDate.of(2004, 1, 4),
          sentenceLength = SentenceLength(1, 1, 1),
          sentenceExpiryDate = LocalDate.of(2004, 1, 5),
          sentencingCourt = "sentencing court",
          espExtendedPeriod = PpudYearMonth(1, 1),
        ),
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud update sentence`() {
    ppudAutomationUpdateSentenceApiMatchResponse("123", "456", "12345678")
    runTest {
      putToUpdateSentence(
        "123",
        "456",
        PpudCreateOrUpdateSentenceRequest(
          custodyType = "Determinate",
          dateOfSentence = LocalDate.of(2004, 1, 2),
          licenceExpiryDate = LocalDate.of(2004, 1, 3),
          mappaLevel = "1",
          releaseDate = LocalDate.of(2004, 1, 4),
          sentenceLength = SentenceLength(1, 1, 1),
          sentenceExpiryDate = LocalDate.of(2004, 1, 5),
          sentencingCourt = "sentencing court",
          espExtendedPeriod = PpudYearMonth(1, 1),
        ),
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud update offence`() {
    ppudAutomationUpdateOffenceApiMatchResponse("123", "456")
    runTest {
      putToUpdateOffence(
        "123",
        "456",
        PpudUpdateOffenceRequest(
          indexOffence = "some dastardly deed",
          dateOfIndexOffence = LocalDate.of(2016, 1, 1),
        ),
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud update release`() {
    ppudAutomationUpdateReleaseApiMatchResponse("123", "456", "12345678")
    runTest {
      postToUpdateRelease(
        "123",
        "456",
        PpudCreateOrUpdateReleaseRequest(
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
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud create recall`() {
    ppudUserRepository.deleteAll()
    ppudUserRepository.save(
      PpudUserEntity(
        userName = "SOME_USER",
        ppudUserFullName = "User Name",
        ppudTeamName = "Team 1",
      ),
    )
    ppudAutomationCreateRecallApiMatchResponse("123", "456", "12345678")
    runTest {
      postToCreateRecall(
        "123",
        "456",
        CreateRecallRequest(
          decisionDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
          isExtendedSentence = false,
          isInCustody = true,
          mappaLevel = "Level 1",
          policeForce = "police force",
          probationArea = "probation area",
          receivedDateTime = LocalDateTime.of(2024, 1, 1, 14, 0),
          riskOfContrabandDetails = "some details",
          riskOfSeriousHarmLevel = RiskOfSeriousHarmLevel.High,
        ),
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud upload mandatory document`() {
    ppudUserRepository.deleteAll()
    ppudUserRepository.save(
      PpudUserEntity(
        userName = "SOME_USER",
        ppudUserFullName = "User Name",
        ppudTeamName = "Team 1",
      ),
    )

    val documentId = UUID.randomUUID()

    recommendationDocumentRepository.deleteAll()
    recommendationDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        id = 456,
        created = null,
        createdBy = null,
        createdByUserFullName = null,
        data = ByteArray(0),
        mimetype = null,
        filename = "",
        title = "",
        type = "",
        recommendationId = 123,
        documentUuid = documentId,
      ),
    )

    ppudAutomationUploadMandatoryDocumentApiMatchResponse("123")
    runTest {
      putToUploadMandatoryDocument(
        "123",
        UploadMandatoryDocumentRequest(
          id = 456,
          category = "PPUDPartA",
        ),
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud upload additional document`() {
    ppudUserRepository.deleteAll()
    ppudUserRepository.save(
      PpudUserEntity(
        userName = "SOME_USER",
        ppudUserFullName = "User Name",
        ppudTeamName = "Team 1",
      ),
    )

    val documentId = UUID.randomUUID()

    recommendationDocumentRepository.deleteAll()
    recommendationDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        id = 456,
        created = null,
        createdBy = null,
        createdByUserFullName = null,
        data = ByteArray(0),
        mimetype = null,
        filename = "",
        title = "some title",
        type = "",
        recommendationId = 123,
        documentUuid = documentId,
      ),
    )

    ppudAutomationUploadAdditionalDocumentApiMatchResponse("123")
    runTest {
      putToUploadAdditionalDocument(
        "123",
        UploadAdditionalDocumentRequest(
          id = 456,
        ),
      )
        .expectStatus().isOk
    }
  }

  @Test
  fun `ppud create minute`() {
    ppudAutomationCreateMinuteApiMatchResponse("123")
    runTest {
      putToCreateMinute(
        "123",
        CreateMinuteRequest(
          subject = "subject",
          text = "text",
        ),
      )
        .expectStatus().isOk
    }
  }

  private fun postToBookRecall(nomisId: String, requestBody: PpudBookRecall): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/ppud/book-recall/$nomisId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun <T> postToCreateOffender(requestBody: T & Any): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/ppud/offender")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun <T> putToUpdateOffender(
    offenderId: String,
    requestBody: T & Any,
  ): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/ppud/offender/$offenderId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun postToCreateSentence(
    offenderId: String,
    requestBody: PpudCreateOrUpdateSentenceRequest,
  ): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/ppud/offender/$offenderId/sentence")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun putToUpdateSentence(
    offenderId: String,
    sentenceId: String,
    requestBody: PpudCreateOrUpdateSentenceRequest,
  ): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/ppud/offender/$offenderId/sentence/$sentenceId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun putToUpdateOffence(
    offenderId: String,
    sentenceId: String,
    requestBody: PpudUpdateOffenceRequest,
  ): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/ppud/offender/$offenderId/sentence/$sentenceId/offence")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun putToUploadMandatoryDocument(
    recallId: String,
    requestBody: UploadMandatoryDocumentRequest,
  ): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/ppud/recall/$recallId/upload-mandatory-document")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun putToUploadAdditionalDocument(
    recallId: String,
    requestBody: UploadAdditionalDocumentRequest,
  ): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/ppud/recall/$recallId/upload-additional-document")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun putToCreateMinute(
    recallId: String,
    requestBody: CreateMinuteRequest,
  ): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/ppud/recall/$recallId/minutes")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun postToUpdateRelease(
    offenderId: String,
    sentenceId: String,
    requestBody: PpudCreateOrUpdateReleaseRequest,
  ): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/ppud/offender/$offenderId/sentence/$sentenceId/release")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun postToCreateRecall(
    offenderId: String,
    releaseId: String,
    requestBody: CreateRecallRequest,
  ): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/ppud/offender/$offenderId/release/$releaseId/recall")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  @Test
  fun `reference list`() {
    ppudAutomationReferenceListApiMatchResponse("custody-type")
    runTest {
      referenceList("custody-type").expectStatus().isOk
    }
  }

  private fun referenceList(name: String): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/ppud/reference/$name")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
}
