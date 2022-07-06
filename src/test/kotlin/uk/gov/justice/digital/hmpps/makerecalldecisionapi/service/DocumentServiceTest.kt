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
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocumentType

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class DocumentServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    documentService = DocumentService(communityApiClient)
  }

  @Test
  fun `given a contact document request then only return all contact documents`() {
    runTest {

      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })

      val response = documentService.getDocumentsForContacts(crn)

      then(communityApiClient).should().getGroupedDocuments(crn)

      assertThat(response, equalTo(expectedContactDocumentResponse()))
    }
  }

  @Test
  fun `given no release summary details then still retrieve contact summary details`() {
    runTest {
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.empty())

      val response = documentService.getDocumentsForContacts(crn)

      then(communityApiClient).should().getGroupedDocuments(crn)

      assertThat(response, equalTo(null))
    }
  }

  protected fun expectedContactDocumentResponse(): List<CaseDocument>? {
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
        parentPrimaryKeyId = "2504763194"
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
        parentPrimaryKeyId = "2504763206"
      )
    )
  }
}
