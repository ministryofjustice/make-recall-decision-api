package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class DocumentServiceTest : ServiceTestBase() {

  private val documentId = "12345"
  private val responseEntity: ResponseEntity<Resource>? = mock(ResponseEntity::class.java) as ResponseEntity<Resource>?

  @BeforeEach
  fun setup() {
    documentService = DocumentService(deliusClient, userAccessValidator)
  }

  @Test
  fun `throws exception when case excluded`() {
    given(deliusClient.getUserAccess(anyString(), anyString()))
      .willReturn(userAccessResponse(true, false, false))

    Assertions.assertThatThrownBy {
      runTest {
        documentService.getDocumentByCrnAndId(crn, "SOME_ID")
      }
    }.isInstanceOf(UserAccessException::class.java)
      .hasMessage("Access excluded for case:: 12345, message:: I am an exclusion message")

    then(deliusClient).shouldHaveNoMoreInteractions()
  }

  @Test
  fun `throws exception when case restricted`() {
    given(deliusClient.getUserAccess(anyString(), anyString()))
      .willReturn(userAccessResponse(false, true, false))

    Assertions.assertThatThrownBy {
      runTest {
        documentService.getDocumentByCrnAndId(crn, "SOME_ID")
      }
    }.isInstanceOf(UserAccessException::class.java)
      .hasMessage("Access restricted for case:: 12345, message:: I am a restriction message")

    then(deliusClient).shouldHaveNoMoreInteractions()
  }

  @Test
  fun `throws exception when calling user not found`() {
    given(deliusClient.getUserAccess(anyString(), anyString()))
      .willReturn(userAccessResponse(false, false, true))

    Assertions.assertThatThrownBy {
      runTest {
        documentService.getDocumentByCrnAndId(crn, "SOME_ID")
      }
    }.isInstanceOf(UserAccessException::class.java)
      .hasMessage("User trying to access case:: 12345 not found")

    then(deliusClient).shouldHaveNoMoreInteractions()
  }

  @Test
  fun `given a get document request then return the requested document`() {
    runTest {
      given(deliusClient.getDocument(anyString(), anyString()))
        .willReturn(responseEntity)

      val response = documentService.getDocumentByCrnAndId(crn, documentId)

      then(deliusClient).should().getDocument(crn, documentId)

      assertThat(response, equalTo(responseEntity))
    }
  }

  @Test
  fun `given no document in request then return empty result`() {
    runTest {
      given(deliusClient.getDocument(anyString(), anyString()))
        .willReturn(null)

      val response = documentService.getDocumentByCrnAndId(crn, documentId)

      then(deliusClient).should().getDocument(crn, documentId)

      assertThat(response, equalTo(null))
    }
  }
}
