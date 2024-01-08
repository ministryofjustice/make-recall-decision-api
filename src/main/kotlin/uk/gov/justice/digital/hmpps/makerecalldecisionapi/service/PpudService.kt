package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse

@Service
internal class PpudService(
  @Qualifier("ppudAutomationApiClient") private val ppudAutomationApiClient: PpudAutomationApiClient,
) {
  fun search(request: PpudSearchRequest): PpudSearchResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.search(request),
    )
    return response!!
  }

  fun bookToPpud(nomisId: String, payload: PpudBookRecall): PpudBookRecallResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.bookToPpud(nomisId, payload),
    )
    return response!!
  }

  fun retrieveList(name: String): PpudReferenceListResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.retrieveList(name),
    )
    return response!!
  }
}
