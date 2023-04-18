package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ConvictionDocuments
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementActionType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.GroupedDocuments
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.OffsetDateTime

@ActiveProfiles("test")
class CommunityApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var communityApiClient: CommunityApiClient

  @Test
  fun `given contact summary request retrieve contact summaries`() {
    // given
    contactSummaryResponse(
      crn,
      uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.contactSummaryResponse()
    )

    // and
    val expected = ContactSummaryResponseCommunity(
      content = listOf(
        Content(
          contactId = 2504412185L,
          contactStart = OffsetDateTime.parse("2022-06-03T07:00Z"),
          type = ContactType(description = "Registration Review", systemGenerated = true, code = "COAP", nationalStandard = false, appointment = false),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
          sensitive = null,
          description = null
        ),
        Content(
          contactId = 2504435532L,
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison", systemGenerated = false, code = "C204", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "This is a test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
          sensitive = true,
          description = "This is a contact description"
        ),
        Content(
          contactId = 2504435999L,
          contactStart = OffsetDateTime.parse("2022-06-10T10:39Z"),
          type = ContactType(description = "Police Liaison", code = "C204", systemGenerated = false, nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "Conviction test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
          sensitive = true,
          description = "This is a conviction contact"
        )
      )
    )

    // when
    val actual = communityApiClient.getContactSummary(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves grouped documents`() {
    // given
    groupedDocumentsResponse(crn)

    // and
    val expected = GroupedDocuments(
      documents = listOf(
        CaseDocument(id = "f2943b31-2250-41ab-a04d-004e27a97add", documentName = "test doc.docx", author = "Trevor Small", type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related document"), extendedDescription = "Contact on 21/06/2022 for Information - from 3rd Party", lastModifiedAt = "2022-06-21T20:27:23.407", createdAt = "2022-06-21T20:27:23", parentPrimaryKeyId = 2504412185L),
        CaseDocument(id = "630ca741-cbb6-4f2e-8e86-73825d8c4d82", documentName = "a test.pdf", author = "Jackie Gough", type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related document"), extendedDescription = "Contact on 21/06/2020 for Complementary Therapy Session (NS)", lastModifiedAt = "2022-06-21T20:29:17.324", createdAt = "2022-06-21T20:29:17", parentPrimaryKeyId = 2504435532L),
        CaseDocument(id = "444ca741-cbb6-4f2e-8e86-73825d8c4d83", documentName = "another test.pdf", author = "Brenda Jones", type = CaseDocumentType(code = "NSI_DOCUMENT", description = "Non Statutory Intervention related document"), extendedDescription = "Another description", lastModifiedAt = "2022-06-22T20:29:17.324", createdAt = "2022-06-22T20:29:17", parentPrimaryKeyId = 2504435532L)
      ),
      convictions = listOf(
        ConvictionDocuments(
          convictionId = "2500614567",
          listOf(
            CaseDocument(id = "374136ce-f863-48d8-96dc-7581636e461e", documentName = "GKlicencejune2022.pdf", author = "Tom Thumb", type = CaseDocumentType(code = "CONVICTION_DOCUMENT", description = "Sentence related"), extendedDescription = null, lastModifiedAt = "2022-06-07T17:00:29.493", createdAt = "2022-06-07T17:00:29", parentPrimaryKeyId = 2500614567L),
            CaseDocument(id = "374136ce-f863-48d8-96dc-7581636e123e", documentName = "TDlicencejuly2022.pdf", author = "Wendy Rose", type = CaseDocumentType(code = "CONVICTION_DOCUMENT", description = "Sentence related"), extendedDescription = null, lastModifiedAt = "2022-07-08T10:00:29.493", createdAt = "2022-06-08T10:00:29", parentPrimaryKeyId = 2500614567L),
            CaseDocument(id = "374136ce-f863-48d8-96dc-7581636e461e", documentName = "ContactDoc.pdf", author = "Terry Tibbs", type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact document"), extendedDescription = null, lastModifiedAt = "2022-06-07T17:00:29.493", createdAt = "2022-06-07T17:00:29", parentPrimaryKeyId = 2504435999L),
            CaseDocument(id = "342234a8-b279-4d6e-b9ff-c7910afce95e", documentName = "Part A Recall Report 08 06 2022.doc", author = "Harry Wilson", type = CaseDocumentType(code = "NSI_DOCUMENT", description = "Non Statutory Intervention related document"), extendedDescription = "Non Statutory Intervention for Request for Recall on 08/06/2022", lastModifiedAt = "2022-06-08T14:24:53.217", createdAt = "2022-06-08T14:24:53", parentPrimaryKeyId = 2500049480L)
          )
        )
      )
    )

    // when
    val actual = communityApiClient.getGroupedDocuments(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves requested document`() {
    // given
    val documentId = "54345"
    getDocumentResponse(crn, documentId)

    // when
    val actual = communityApiClient.getDocumentByCrnAndId(crn, documentId).block()

    // then
    assertThat(actual?.statusCode?.is2xxSuccessful, equalTo(true))
  }
}
