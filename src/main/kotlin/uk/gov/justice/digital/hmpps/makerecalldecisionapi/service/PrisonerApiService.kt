package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchResponse

@Service
internal class PrisonerApiService(
  private val prisonApiClient: PrisonApiClient,
) {
  fun searchPrisonApi(nomsId: String): PrisonOffenderSearchResponse {
    val response = getValueAndHandleWrappedException(
      prisonApiClient.retrieveOffender(nomsId),
    )
    return response!!
  }
}
