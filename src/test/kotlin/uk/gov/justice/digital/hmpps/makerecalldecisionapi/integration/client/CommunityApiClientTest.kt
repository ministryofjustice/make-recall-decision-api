package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.description
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ConvictionDocuments
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementActionType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EstablishmentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.GroupedDocuments
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Institution
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.KeyDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeMainCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeSubCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.MappaResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderProfile
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Officer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProbationArea
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Reason
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Registration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Type
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@ActiveProfiles("test")
class CommunityApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var communityApiClient: CommunityApiClient

  @Test
  fun `retrieves convictions`() {
    // given
    val staffCode = "STFFCDEU"
    convictionResponse(crn, staffCode)

    // and
    val expected = Conviction(
      convictionDate = LocalDate.parse("2021-06-10"),
      sentence = Sentence(
        startDate = LocalDate.parse("2022-04-26"),
        terminationDate = LocalDate.parse("2022-04-26"),
        expectedSentenceEndDate = LocalDate.parse("2022-04-26"),
        description = "Extended Determinate Sentence",
        originalLength = 12,
        originalLengthUnits = "days",
        secondLength = 19,
        secondLengthUnits = "days",
        sentenceType = SentenceType(code = "ABC123")
      ),
      active = true,
      offences = listOf(
        Offence(
          mainOffence = true,
          offenceDate = LocalDate.parse("2022-04-24"),
          detail = OffenceDetail(
            mainCategoryDescription = "string", subCategoryDescription = "string",
            description = "Robbery (other than armed robbery)",
            code = "1234"
          )
        )
      ),
      convictionId = 2500614567,
      orderManagers =
      listOf(
        OrderManager(
          dateStartOfAllocation = LocalDateTime.parse("2022-04-26T20:39:47.778"),
          name = "string",
          staffCode = "STFFCDEU",
          gradeCode = "string"
        )
      ),
      custody = Custody(
        bookingNumber = null,
        institution = Institution(
          code = "COMMUN",
          description = "In the Community",
          establishmentType = EstablishmentType(
            code = "E",
            description = "Prison"
          ),
          institutionId = 156,
          institutionName = "In the Community",
          isEstablishment = true,
          isPrivate = false,
          nomsPrisonInstitutionCode = "AB124",
        ),
        status = CustodyStatus(code = "ABC123", description = "I am the custody status description"),
        keyDates = KeyDates(
          conditionalReleaseDate = LocalDate.parse("2020-06-20"),
          expectedPrisonOffenderManagerHandoverDate = LocalDate.parse("2020-06-21"),
          expectedPrisonOffenderManagerHandoverStartDate = LocalDate.parse("2020-06-22"),
          expectedReleaseDate = LocalDate.parse("2020-06-23"),
          hdcEligibilityDate = LocalDate.parse("2020-06-24"),
          licenceExpiryDate = LocalDate.parse("2020-06-25"),
          paroleEligibilityDate = LocalDate.parse("2020-06-26"),
          sentenceExpiryDate = LocalDate.parse("2020-06-28"),
          postSentenceSupervisionEndDate = LocalDate.parse("2020-06-27")
        ),
        sentenceStartDate = LocalDate.parse("2022-04-26")
      )
    )

    // when
    val actual = communityApiClient.getActiveConvictions(crn).block()!![0]

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves empty convictions list when no active convictions present`() {
    // given
    noActiveConvictionResponse(crn)

    // and
    val expected = emptyList<Conviction>()

    // when
    val actual = communityApiClient.getActiveConvictions(crn).block()!!

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves licence conditions`() {
    // given
    val convictionId = 9876789L
    licenceConditionsResponse(crn, convictionId)

    // and
    val expected = LicenceConditions(
      licenceConditions = listOf(
        LicenceCondition(
          startDate = LocalDate.parse("2022-05-18"),
          terminationDate = LocalDate.parse("2022-05-22"),
          createdDateTime = LocalDateTime.parse("2022-05-18T19:33:56"),
          active = true,
          licenceConditionNotes = "I am a licence condition note",
          licenceConditionTypeMainCat = LicenceConditionTypeMainCat(
            code = "NLC8",
            description = "Freedom of movement for conviction $convictionId"
          ),
          licenceConditionTypeSubCat = LicenceConditionTypeSubCat(
            code = "NSTT8",
            description = "To only attend places of worship which have been previously agreed with your supervising officer."
          )
        ),
        LicenceCondition(
          startDate = LocalDate.parse("2022-05-20"),
          terminationDate = null,
          createdDateTime = LocalDateTime.parse("2022-05-20T12:33:56"),
          active = false,
          licenceConditionNotes = "I am a second licence condition note",
          licenceConditionTypeMainCat = LicenceConditionTypeMainCat(
            code = "NLC7",
            description = "Inactive test"
          ),
          licenceConditionTypeSubCat = LicenceConditionTypeSubCat(
            code = "NSTT7",
            description = "I am inactive"
          )
        )
      )
    )

    // when
    val actual = communityApiClient.getLicenceConditionsByConvictionId(crn, convictionId).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves empty licence conditions list when no active licence conditions present`() {
    // given
    noActiveConvictionResponse(crn)

    // and
    val expected = emptyList<Conviction>()

    // when
    val actual = communityApiClient.getActiveConvictions(crn).block()!!

    // then
    assertThat(actual, equalTo(expected))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `throws exception when no person matching crn exists`() {
    val nonExistentCrn = "X123456"
    allOffenderDetailsResponseWithNoOffender(nonExistentCrn)
    assertThatThrownBy {
      runBlockingTest {
        communityApiClient.getAllOffenderDetails(nonExistentCrn).block()
      }
    }.isInstanceOf(PersonNotFoundException::class.java)
      .hasMessage("No details available for crn: $nonExistentCrn")
  }

  @Test
  fun `retrieves registrations`() {
    // given
    registrationsResponse()

    // and
    val expected = RegistrationsResponse(
      registrations = listOf(
        Registration(
          active = true,
          type = Type(code = "ABC123", description = "Victim contact")
        ),
        Registration(
          active = false,
          type = Type(code = "ABC124", description = "Mental health issues")
        )
      )
    )

    // when
    val actual = communityApiClient.getRegistrations(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

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
  fun `retrieves all offender details`() {
    // given
    allOffenderDetailsResponse(crn)

    // and
    val expected = AllOffenderDetailsResponse(
      dateOfBirth = LocalDate.parse("1982-10-24"),
      firstName = "John",
      surname = "Smith",
      gender = "Male",
      middleNames = listOf<String>("Homer", "Bart"),
      otherIds = OtherIds(
        crn = "12345C",
        croNumber = "123456/04A",
        mostRecentPrisonerNumber = "G12345",
        nomsNumber = "A1234CR",
        pncNumber = "2004/0712343H"
      ),
      contactDetails = ContactDetails(
        addresses = listOf(
          Address(
            postcode = "90220",
            district = "South Central",
            streetName = "Skid Row",
            addressNumber = "45",
            buildingName = "Death Row Records",
            town = "Compton",
            county = "LA", status = AddressStatus(code = "ABC123", description = "Not Main"),
            noFixedAbode = false
          ),
          Address(
            town = "Sheffield",
            county = "South Yorkshire",
            buildingName = "HMPPS Digital Studio",
            district = "Sheffield City Centre",
            streetName = "Scotland Street",
            status = AddressStatus(code = "ABC123", description = "Main"),
            postcode = "S3 7BS",
            addressNumber = "33",
            noFixedAbode = false
          )
        )
      ),
      offenderManagers = listOf(
        OffenderManager(
          active = true,
          probationArea = ProbationArea(code = "N01", description = "NPS North West"),
          trustOfficer = TrustOfficer(forenames = "Sheila Linda", surname = "Hancock"),
          staff = Staff(forenames = "Sheila Linda", surname = "Hancock"),
          providerEmployee = ProviderEmployee(forenames = "Sheila Linda", surname = "Hancock"),
          team = Team(
            telephone = "09056714321",
            emailAddress = "first.last@digital.justice.gov.uk",
            code = "C01T04",
            description = "OMU A",
            localDeliveryUnit = LocalDeliveryUnit(code = "ABC123", description = "Local delivery unit description 2")
          )
        ),
        OffenderManager(
          active = false,
          probationArea = ProbationArea(code = "N01", description = "NPS North West"),
          trustOfficer = TrustOfficer(forenames = "Dua", surname = "Lipa"),
          staff = Staff(forenames = "Sheila Linda", surname = "Hancock"),
          providerEmployee = ProviderEmployee(forenames = "Sheila Linda", surname = "Hancock"),
          team = Team(
            telephone = "123",
            emailAddress = "dua.lipa@digital.justice.gov.uk",
            code = "C01T04",
            description = "OMU A",
            localDeliveryUnit = LocalDeliveryUnit(code = "ABC123", description = "Local delivery unit description 4")
          )
        )

      ),
      offenderProfile = OffenderProfile(ethnicity = "Ainu")
    )

    // when
    val actual = communityApiClient.getAllOffenderDetails(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves mappa details`() {
    // given
    mappaDetailsResponse(crn)

    // and
    val expected = MappaResponse(
      level = 1,
      levelDescription = "MAPPA Level 1",
      category = 0,
      categoryDescription = "All - Category to be determined",
      startDate = LocalDate.parse("2021-02-10"),
      reviewDate = LocalDate.parse("2021-05-10"),
      team = Team(
        code = "N07CHT",
        description = "Automation SPG",
        emailAddress = null,
        telephone = null,
        localDeliveryUnit = null
      ),
      officer = Officer(
        code = "N07A060",
        forenames = "NDelius26",
        surname = "Anderson",
        unallocated = false
      ),
      probationArea = ProbationArea(
        code = "N07",
        description = "NPS London"
      ),
      notes = "Please Note - Category 3 offenders require multi-agency management at Level 2 or 3 and should not be recorded at Level 1.\nNote\nnew note"
    )

    // when
    val actual = communityApiClient.getAllMappaDetails(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves release summaries`() {
    // given
    releaseSummaryResponse(crn)

    // and
    val expected = ReleaseSummaryResponse(
      lastRelease = LastRelease(date = LocalDate.parse("2017-09-15"), notes = "I am a note", reason = Reason(code = "ADL", description = "Adult Licence")),
      lastRecall = LastRecall(date = LocalDate.parse("2020-10-15"), notes = "I am a second note", reason = Reason(code = "ABC123", description = "another reason description"))
    )

    // when
    val actual = communityApiClient.getReleaseSummary(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves user access`() {
    // given
    userAccessAllowed(crn)

    // and
    val expected = UserAccessResponse(
      userRestricted = false,
      userExcluded = false,
      exclusionMessage = null,
      restrictionMessage = null,
      userNotFound = false,
      userNotFoundMessage = null
    )

    // when
    val actual = communityApiClient.getUserAccess(crn).block()

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
