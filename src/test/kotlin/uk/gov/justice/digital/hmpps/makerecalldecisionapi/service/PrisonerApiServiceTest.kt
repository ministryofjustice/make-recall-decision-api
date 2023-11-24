package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchResponse

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PrisonerApiServiceTest : ServiceTestBase() {

  @Test
  fun `call service`() {
    val nomsId = "AB234A"

    val response = mock(PrisonOffenderSearchResponse::class.java)

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val headers = HttpHeaders()
    headers.put("Content-Type", listOf("image/jpeg"))

    given(prisonApiClient.retrieveImageData(any()))
      .willReturn(
        Mono.fromCallable {
          ResponseEntity(ByteArrayResource("data".toByteArray()), headers, HttpStatus.OK)
        },
      )

    val result = PrisonerApiService(prisonApiClient).searchPrisonApi(nomsId)

    assertThat(result).isEqualTo(response)

    verify(result).image = "data:image/jpeg;base64,ZGF0YQ=="
  }
}
