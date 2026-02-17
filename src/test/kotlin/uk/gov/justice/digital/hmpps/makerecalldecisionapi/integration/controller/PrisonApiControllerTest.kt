package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import com.fasterxml.jackson.core.type.TypeReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.assertMovementsAreEqual
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.prisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonSentencesRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.OffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.movement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.prisonPeriod
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.prisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.sentenceCalculationDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.prisonOffenderSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison.PrisonApiResponseMocker
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDateTime

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PrisonApiControllerTest : IntegrationTestBase() {

  @Value("\${prison.client.timeout}")
  var prisonTimeout: Long = 0

  // TODO use default constructor (and remove non-default one from PrisonApiResponseMocker) once we
  //      move the prisonApi val out of IntegrationTestBase as part of breaking up the latter so it
  //      is no longer a god class. This will also require calling the start-up and tear-down methods
  private val prisonApiResponseMocker: PrisonApiResponseMocker = PrisonApiResponseMocker(prisonApi)

  @Test
  fun `retrieves offender details`() {
    runTest {
      val nomsId = "A123456"
      val locationDescription = "Leeds"
      val facialImageId = "1234"
      val agencyId = "BRX"
      prisonApiOffenderMatchResponse(nomsId, locationDescription, facialImageId, agencyId)
      prisonApiImageResponse(facialImageId, "data")
      val agencyDescription = "HMP Brixton"
      mockPrisonApiAgencyResponse(agencyId, agency(description = agencyDescription))

      val response = convertResponseToJSONObject(
        webTestClient.post()
          .uri("/prison-offender-search")
          .contentType(MediaType.APPLICATION_JSON)
          .body(
            BodyInserters.fromValue(prisonOffenderSearchRequest(nomsId)),
          )
          .headers {
            (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION"))))
          }
          .exchange()
          .expectStatus().isOk,
      )
      assertThat(response.get("locationDescription")).isEqualTo(locationDescription)
      assertThat(response.get("agencyDescription")).isEqualTo(agencyDescription)
    }
  }

  @Test
  fun `retrieves prison sentence`() {
    runTest {
      // given
      val now = LocalDateTime.now()

      val nomisId = randomString()

      val offender = offender(nomsId)
      prisonApiResponseMocker.mockRetrieveOffenderResponse(nomisId, offender)

      val agency = agency()
      val agencyId = agency.agencyId!!
      prisonApiResponseMocker.mockRetrieveAgencyResponse(agencyId, agency)

      val firstPrisonPeriod = prisonPeriod(
        movementDates = listOf(
          movement(dateOutOfPrison = now.minusMonths(27)),
          movement(dateOutOfPrison = now.minusMonths(24)),
          movement(dateOutOfPrison = now.minusMonths(22), releaseFromPrisonId = agencyId),
        ),
      )
      val secondPrisonPeriod = prisonPeriod(
        movementDates = listOf(
          movement(dateOutOfPrison = now.minusMonths(7)),
          movement(dateOutOfPrison = now.minusMonths(4)),
          movement(dateOutOfPrison = now.minusMonths(2), releaseFromPrisonId = agencyId),
        ),
      )
      val prisonPeriods = listOf(firstPrisonPeriod, secondPrisonPeriod)
      prisonApiResponseMocker.mockRetrievePrisonTimelinesResponse(
        nomisId,
        prisonTimelineResponse(prisonPeriod = prisonPeriods),
      )

      val firstPeriodSentences = listOf(
        sentence(
          bookingId = firstPrisonPeriod.bookingId,
          sentenceSequence = 0,
          consecutiveToSequence = null,
        ),
        sentence(
          bookingId = firstPrisonPeriod.bookingId,
          sentenceSequence = 1,
          consecutiveToSequence = 0,
        ),
      )
      prisonApiResponseMocker.mockRetrieveSentencesAndOffencesResponse(
        firstPrisonPeriod.bookingId,
        firstPeriodSentences,
      )
      val secondPeriodSentences = listOf(
        sentence(
          bookingId = secondPrisonPeriod.bookingId,
          sentenceSequence = 0,
          consecutiveToSequence = null,
        ),
        sentence(
          bookingId = secondPrisonPeriod.bookingId,
          sentenceSequence = 1,
          consecutiveToSequence = 0,
        ),
      )
      prisonApiResponseMocker.mockRetrieveSentencesAndOffencesResponse(
        secondPrisonPeriod.bookingId,
        secondPeriodSentences,
      )

      val firstPeriodSentenceExpiryDate = now.plusMonths(5).toLocalDate()
      prisonApiResponseMocker.mockBookingSentenceDetailsResponse(
        firstPrisonPeriod.bookingId,
        sentenceCalculationDates(sentenceExpiryOverrideDate = firstPeriodSentenceExpiryDate),
      )
      val secondPeriodSentenceExpiryDate = now.plusMonths(2).toLocalDate()
      prisonApiResponseMocker.mockBookingSentenceDetailsResponse(
        secondPrisonPeriod.bookingId,
        sentenceCalculationDates(sentenceExpiryOverrideDate = secondPeriodSentenceExpiryDate),
      )

      val convertToExpectedSentence = { sentence: Sentence ->
        uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.sentence(
          bookingId = sentence.bookingId,
          sentenceSequence = sentence.sentenceSequence,
          lineSequence = sentence.lineSequence,
          consecutiveToSequence = sentence.consecutiveToSequence,
          caseSequence = sentence.caseSequence,
          courtDescription = sentence.courtDescription,
          sentenceStatus = sentence.sentenceStatus,
          sentenceCategory = sentence.sentenceCategory,
          sentenceCalculationType = sentence.sentenceCalculationType,
          sentenceTypeDescription = sentence.sentenceTypeDescription,
          sentenceDate = sentence.sentenceDate,
          sentenceStartDate = sentence.sentenceStartDate,
          sentenceEndDate = null, // as all expected sequences have more than one sentence
          sentenceSequenceExpiryDate = null, // set further down, as only expected in index sentences
          terms = sentence.terms.map {
            uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.term(
              years = it.years,
              months = it.months,
              weeks = it.weeks,
              days = it.days,
              code = it.code,
            )
          },
          offences = sentence.offences.map {
            uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.sentenceOffence(
              offenderChargeId = it.offenderChargeId,
              offenceStartDate = it.offenceStartDate,
              offenceStatute = it.offenceStatute,
              offenceCode = it.offenceCode,
              offenceDescription = it.offenceDescription,
              indicators = it.indicators,
            )
          },
          releaseDate = sentence.releaseDate,
          releasingPrison = sentence.releasingPrison,
          licenceExpiryDate = sentence.licenceExpiryDate,
        )
      }
      val overriddenFirstPeriodSentences = firstPeriodSentences.map { sentence: Sentence ->
        sentence.copy(
          releaseDate = firstPrisonPeriod.movementDates.last().dateOutOfPrison,
          releasingPrison = agency.longDescription,
          licenceExpiryDate = offender.sentenceDetail?.licenceExpiryDate,
          offences = sentence.offences.sortedBy { it.offenceDescription },
        ).let { convertToExpectedSentence(it) }
      }
      val overriddenSecondPeriodSentences = secondPeriodSentences.map { sentence: Sentence ->
        sentence.copy(
          releaseDate = secondPrisonPeriod.movementDates.last().dateOutOfPrison,
          releasingPrison = agency.longDescription,
          licenceExpiryDate = offender.sentenceDetail?.licenceExpiryDate,
          offences = sentence.offences.sortedBy { it.offenceDescription },
        ).let { convertToExpectedSentence(it) }
      }
      val expectedSentenceSequences = listOf(
        SentenceSequence(
          overriddenFirstPeriodSentences[0].copy(
            sentenceSequenceExpiryDate = firstPeriodSentenceExpiryDate,
          ),
          sentencesInSequence = mutableMapOf(
            0 to listOf(overriddenFirstPeriodSentences[1]),
          ),
        ),
        SentenceSequence(
          overriddenSecondPeriodSentences[0].copy(
            sentenceSequenceExpiryDate = secondPeriodSentenceExpiryDate,
          ),
          sentencesInSequence = mutableMapOf(
            0 to listOf(overriddenSecondPeriodSentences[1]),
          ),
        ),
      )

      // when
      val response = convertResponseToJSONArray(
        webTestClient.post()
          .uri("/prison-sentences")
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(PrisonSentencesRequest(nomisId)))
          .headers {
            (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION"))))
          }
          .exchange()
          .expectStatus().isOk,
      )

      // then
      val jacksonTypeReference: TypeReference<List<SentenceSequence>> =
        object : TypeReference<List<SentenceSequence>>() {}
      val sentenceSequences = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
      assertThat(sentenceSequences).usingRecursiveComparison().isEqualTo(expectedSentenceSequences)
    }
  }

  @Test
  fun `retrieves offender movements`() {
    runTest {
      // given
      val nomsId = "A123456"
      val prisonApiMovements =
        listOf(prisonApiOffenderMovement(), prisonApiOffenderMovement(), prisonApiOffenderMovement())
      prisonApiResponseMocker.mockRetrieveOffenderMovementsResponse(nomsId, prisonApiMovements)

      // when
      val response = convertResponseToJSONArray(
        webTestClient.get()
          .uri("/offenders/$nomsId/movements")
          .headers {
            (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_PPCS"))))
          }
          .exchange()
          .expectStatus().isOk,
      )

      // then
      val jacksonTypeReference: TypeReference<List<OffenderMovement>> =
        object : TypeReference<List<OffenderMovement>>() {}
      val movements = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
      assertThat(movements).hasSameSizeAs(prisonApiMovements)
      for (i in movements.indices) {
        assertMovementsAreEqual(movements[i], prisonApiMovements[i])
      }
    }
  }

  @Test
  fun `offender movement retrieval fails due to missing authorisation`() {
    runTest {
      // given
      val nomsId = "A123456"

      // when then
      webTestClient.get()
        .uri("/offenders/$nomsId/movements")
        .headers {
          (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION"))))
        }
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Test
  fun `offender movement retrieval fails due to Prison API timeouts`() {
    runTest {
      // given
      val nomsId = "A123456"
      val timeoutMessage = "Prison API Client: [No response within $prisonTimeout seconds]"
      val expectedErrorResponse = ErrorResponse(
        status = GATEWAY_TIMEOUT,
        userMessage = "Client timeout: $timeoutMessage",
        developerMessage = timeoutMessage,
      )
      prisonApiResponseMocker.mockRetrieveOffenderMovementsTimeout(nomsId, prisonTimeout)

      // when
      val response = convertResponseToJSONObject(
        webTestClient.get()
          .uri("/offenders/$nomsId/movements")
          .headers {
            (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_PPCS"))))
          }
          .exchange()
          .expectStatus().isEqualTo(GATEWAY_TIMEOUT),
      )

      // then
      val jacksonTypeReference: TypeReference<ErrorResponse> =
        object : TypeReference<ErrorResponse>() {}
      val actualErrorResponse = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
      assertThat(actualErrorResponse).isEqualTo(expectedErrorResponse)
    }
  }
}
