package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateSentence

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

  fun details(id: String): PpudDetailsResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.details(id),
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

  fun createOffender(request: PpudCreateOffender): PpudCreateOffenderResponse {
    val response = getValueAndHandleWrappedException(
      ppudAutomationApiClient.createOffender(request),
    )
    return response!!
  }

  fun updateSentence(offenderId: String, sentenceId: String, request: PpudUpdateSentence) {
    getValueAndHandleWrappedException(
      ppudAutomationApiClient.updateSentence(offenderId, sentenceId, request),
    )
    println("DONE")
  }
}
