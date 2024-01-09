package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PpudServiceTest : ServiceTestBase() {

  @Test
  fun `call search`() {
    val request = mock(PpudSearchRequest::class.java)

    val response = mock(PpudSearchResponse::class.java)

    given(ppudAutomationApiClient.search(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient).search(request)

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call details`() {
    val response = mock(PpudDetailsResponse::class.java)

    given(ppudAutomationApiClient.details(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient).details("12345678")

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `call book to ppud`() {
    val request = mock(PpudBookRecall::class.java)

    val response = mock(PpudBookRecallResponse::class.java)

    given(ppudAutomationApiClient.bookToPpud("123", request)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient).bookToPpud("123", request)

    assertThat(result).isEqualTo(response)
  }

  @Test
  fun `reference list`() {
    val response = mock(PpudReferenceListResponse::class.java)

    given(ppudAutomationApiClient.retrieveList("custody-type")).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = PpudService(ppudAutomationApiClient).retrieveList("custody-type")

    assertThat(result).isEqualTo(response)
  }
}
