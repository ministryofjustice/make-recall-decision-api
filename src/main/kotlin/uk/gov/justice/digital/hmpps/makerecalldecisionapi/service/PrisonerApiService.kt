package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchResponse
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
      val responseImage = getValueAndHandleWrappedException(
        prisonApiClient.retrieveImageData(response?.facialImageId.toString()),
      )

      val contentType = responseImage?.headers?.get("Content-Type")?.get(0)
      response.image =
        "data:" + contentType + ";base64," + String(Base64.getEncoder().encode(responseImage?.body?.byteArray))
    }

    return response!!
  }
}
