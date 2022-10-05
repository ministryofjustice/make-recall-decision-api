package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocumentType

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class DocumentServiceTest : ServiceTestBase() {

  private val documentId = "12345"
  private val responseEntity: ResponseEntity<Resource>? = mock(ResponseEntity::class.java) as ResponseEntity<Resource>?

  @BeforeEach
  fun setup() {
    documentService = DocumentService(communityApiClient)
  }

  @Test
  fun `given a contact document type then only return all contact documents`() {
    runTest {

      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })

      val response = documentService.getDocumentsByDocumentType(crn, "CONTACT_DOCUMENT")

      then(communityApiClient).should().getGroupedDocuments(crn)

      assertThat(response, equalTo(expectedContactDocumentResponse()))
    }
  }

  @Test
  fun `given no contact documents then handle request`() {
    runTest {
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.empty())

      val response = documentService.getDocumentsByDocumentType(crn, "CONTACT_DOCUMENT")

      then(communityApiClient).should().getGroupedDocuments(crn)

      assertThat(response, equalTo(null))
    }
  }

  @Test
  fun `given a get document request then return the requested document`() {
    runTest {

      given(communityApiClient.getDocumentByCrnAndId(anyString(), anyString()))
        .willReturn(Mono.fromCallable { responseEntity })

      val response = documentService.getDocumentByCrnAndId(crn, documentId)

      then(communityApiClient).should().getDocumentByCrnAndId(crn, documentId)

      assertThat(response, equalTo(responseEntity))
    }
  }

  @Test
  fun `given no document in request then return empty result`() {
    runTest {

      given(communityApiClient.getDocumentByCrnAndId(anyString(), anyString()))
        .willReturn(Mono.empty())

      val response = documentService.getDocumentByCrnAndId(crn, documentId)

      then(communityApiClient).should().getDocumentByCrnAndId(crn, documentId)

      assertThat(response, equalTo(null))
    }
  }

  private fun expectedContactDocumentResponse(): List<CaseDocument>? {
    return listOf(
      CaseDocument(
        id = "f2943b31-2250-41ab-a04d-004e27a97add",
        documentName = "test doc.docx",
        author = "Trevor Small",
        type = CaseDocumentType(
          code = "CONTACT_DOCUMENT",
          description = "Contact related document"
        ),
        extendedDescription = "Contact on 21/06/2022 for Information - from 3rd Party",
        lastModifiedAt = "2022-06-21T20:27:23.407",
        createdAt = "2022-06-21T20:27:23",
        parentPrimaryKeyId = 2504763194L
      ),
      CaseDocument(
        id = "630ca741-cbb6-4f2e-8e86-73825d8c4d82",
        documentName = "a test.pdf",
        author = "Jackie Gough",
        type = CaseDocumentType(
          code = "CONTACT_DOCUMENT",
          description = "Contact related document"
        ),
        extendedDescription = "Contact on 21/06/2020 for Complementary Therapy Session (NS)",
        lastModifiedAt = "2022-06-21T20:29:17.324",
        createdAt = "2022-06-21T20:29:17",
        parentPrimaryKeyId = 2504763206L
      ),
      CaseDocument(
        id = "630ca741-cbb6-4f2e-8e86-73825d8c4999",
        documentName = "conviction contact doc.pdf",
        author = "Luke Smith",
        type = CaseDocumentType(
          code = "CONTACT_DOCUMENT",
          description = "Contact related conviction document"
        ),
        extendedDescription = "Contact on 23/06/2020 for Complementary Therapy Session (NS)",
        lastModifiedAt = "2022-06-23T20:29:17.324",
        createdAt = "2022-06-23T20:29:17",
        parentPrimaryKeyId = 2504763206L
      )
    )
  }

  private fun expectedConvictionDocumentResponse(): List<CaseDocument>? {
    return listOf(
      CaseDocument(
        id = "374136ce-f863-48d8-96dc-7581636e461e",
        documentName = "GKlicencejune2022.pdf",
        author = "Tom Thumb",
        type = CaseDocumentType(
          code = "CONVICTION_DOCUMENT",
          description = "Sentence related"
        ),
        extendedDescription = null,
        lastModifiedAt = "2022-06-07T17:00:29.493",
        createdAt = "2022-06-07T17:00:29",
        parentPrimaryKeyId = 2500614567L
      ),
      CaseDocument(
        id = "374136ce-f863-48d8-96dc-7581636e123e",
        documentName = "TDlicencejuly2022.pdf",
        author = "Wendy Rose",
        type = CaseDocumentType(
          code = "CONVICTION_DOCUMENT",
          description = "Sentence related"
        ),
        extendedDescription = null,
        lastModifiedAt = "2022-07-08T10:00:29.493",
        createdAt = "2022-06-08T10:00:29",
        parentPrimaryKeyId = 2500614567L
      ),
    )
  }
}
