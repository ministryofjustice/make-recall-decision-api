package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison

import org.mockserver.integration.ClientAndServer
import org.mockserver.model.MediaType.JPEG
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.PrisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.PrisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ResponseMocker

internal class PrisonApiResponseMocker : ResponseMocker {

  constructor(prisonApi: ClientAndServer) : super(prisonApi)

  // port aligned with those specified in application-test.yml
  constructor() : super(8098)

  fun mockRetrieveOffenderResponse(
    nomsId: String,
    offender: Offender,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/api/offenders/$nomsId",
      responseBody = offender,
      delaySeconds = delaySeconds,
    )
  }

  fun mockRetrieveImageDataResponse(
    facialImageId: String,
    imageData: ByteArray,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/api/images/$facialImageId/data",
      responseBody = imageData,
      delaySeconds = delaySeconds,
      mediaType = JPEG,
    )
  }

  fun mockRetrievePrisonTimelinesResponse(
    nomsId: String,
    prisonTimelineResponse: PrisonTimelineResponse,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/api/offenders/$nomsId/prison-timeline",
      responseBody = prisonTimelineResponse,
      delaySeconds = delaySeconds,
    )
  }

  fun mockRetrieveSentencesAndOffencesResponse(
    bookingId: Long,
    sentences: List<Sentence>,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/api/offender-sentences/booking/$bookingId/sentences-and-offences",
      responseBody = sentences,
      delaySeconds = delaySeconds,
    )
  }

  fun mockRetrieveAgencyResponse(
    agencyId: String,
    agency: Agency,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/api/agencies/$agencyId",
      responseBody = agency,
      delaySeconds = delaySeconds,
    )
  }

  fun mockRetrieveOffenderMovementsResponse(
    nomsId: String,
    offenderMovements: List<PrisonApiOffenderMovement>,
    delaySeconds: Long = 0,
  ) {
    mockResponse(
      path = "/api/movements/offender/$nomsId",
      responseBody = offenderMovements,
      delaySeconds = delaySeconds,
    )
  }

  fun mockRetrieveOffenderMovementsTimeout(
    nomsId: String,
    prisonTimeoutInSeconds: Long,
  ) {
    mockTimeout("/api/movements/offender/$nomsId", prisonTimeoutInSeconds)
  }
}
