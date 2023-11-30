package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Sentence
import java.time.LocalDate
import java.util.*

@Service
internal class PrisonerApiService(
  private val prisonApiClient: PrisonApiClient,
) {
  fun searchPrisonApi(nomsId: String): PrisonOffenderSearchResponse {
    val response = getValueAndHandleWrappedException(
      prisonApiClient.retrieveOffender(nomsId),
    )

    response?.facialImageId?.let {
      if (response.facialImageId > 0) {
        val responseImage = getValueAndHandleWrappedException(
          prisonApiClient.retrieveImageData(response.facialImageId.toString()),
        )

        val contentType = responseImage?.headers?.get("Content-Type")?.get(0)
        response.image =
          "data:" + contentType + ";base64," + String(Base64.getEncoder().encode(responseImage?.body?.byteArray))
      }
    }

    return response!!
  }

  fun retrieveOffences(nomsId: String): List<Sentence> {
    return getValueAndHandleWrappedException(
      prisonApiClient.retrievePrisonTimelines(nomsId),
    )!!.prisonPeriod
      .flatMap { prisonApiClient.retrieveSentencesAndOffences(it.bookingId).block()!! }
      .filter { it.sentenceEndDate == null || !it.sentenceEndDate.isBefore(LocalDate.now()) }
      .sortedBy { it.courtDescription }
      .sortedByDescending { it.sentenceDate }
  }
}
