package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse

@Service
internal class PpudService(
  @Qualifier("ppudAutomationClient") private val ppudAutomationApiClient: PpudAutomationApiClient,
) {
  fun search(request: PpudSearchRequest): PpudSearchResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.search(request),
    )
    return response!!
  }
}
