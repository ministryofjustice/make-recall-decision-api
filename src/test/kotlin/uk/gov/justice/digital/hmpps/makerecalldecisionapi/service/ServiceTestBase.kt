package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.anyString
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.kotlin.doReturn
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ConvictionDocuments
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EstablishmentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.GroupedDocuments
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Institution
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.KeyDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderProfile
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Reason
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import java.time.LocalDate
import java.time.LocalDateTime

internal abstract class ServiceTestBase {

  @Mock
  protected lateinit var communityApiClient: CommunityApiClient

  @Mock
  protected lateinit var recommendationRepository: RecommendationRepository

  @Mock
  protected lateinit var mockPersonDetailService: PersonDetailsService

  protected lateinit var personDetailsService: PersonDetailsService

  protected lateinit var userAccessValidator: UserAccessValidator

  protected lateinit var documentService: DocumentService

  protected lateinit var convictionService: ConvictionService

  protected lateinit var recommendationService: RecommendationService

  protected lateinit var partATemplateReplacementService: PartATemplateReplacementService

  protected val crn = "12345"

  @BeforeEach
  fun userValidatorSetup() {
    lenient().`when`(mockPersonDetailService.getPersonDetails(anyString())).doReturn(
      PersonDetailsResponse(
        personalDetailsOverview = PersonDetails(name = "John Smith", firstName = "John", middleNames = "Homer Bart", surname = "Smith", age = null, crn = null, dateOfBirth = LocalDate.parse("1982-10-24"), gender = "Male", ethnicity = "Ainu", croNumber = "123456/04A", pncNumber = "2004/0712343H", mostRecentPrisonerNumber = "G12345", nomsNumber = "A1234CR")
      )
    )
    userAccessValidator = UserAccessValidator(communityApiClient)
    partATemplateReplacementService = PartATemplateReplacementService()
    documentService = DocumentService(communityApiClient)
    convictionService = ConvictionService(communityApiClient, documentService)
    recommendationService = RecommendationService(recommendationRepository, mockPersonDetailService, partATemplateReplacementService, userAccessValidator, convictionService)
    personDetailsService = PersonDetailsService(communityApiClient, userAccessValidator, recommendationService)
  }

  protected fun allReleaseSummariesResponse(): ReleaseSummaryResponse {

    return ReleaseSummaryResponse(
      lastRelease = LastRelease(date = LocalDate.parse("2017-09-15"), notes = "I am a note", reason = Reason(code = "reasonCode", description = "reasonDescription")),
      lastRecall = LastRecall(date = LocalDate.parse("2020-10-15"), notes = "I am a second note", reason = Reason(code = "another reason code", description = "another reason description"))
    )
  }

  protected fun allOffenderDetailsResponse(): AllOffenderDetailsResponse {
    return AllOffenderDetailsResponse(
      dateOfBirth = LocalDate.parse("1982-10-24"),
      firstName = "John",
      surname = "Smith",
      middleNames = emptyList(),
      gender = "Male",
      otherIds = OtherIds(crn = null, croNumber = "123456/04A", mostRecentPrisonerNumber = "G12345", nomsNumber = "A1234CR", pncNumber = "2004/0712343H"),
      contactDetails = ContactDetails(
        addresses = listOf(
          Address(
            town = "Compton",
            county = "LA",
            buildingName = "HMPPS Digital Studio",
            district = "South Central",
            status = AddressStatus(code = "ABC123", description = "Not Main"),
            postcode = "90210",
            addressNumber = "339",
            streetName = "Sesame Street"
          ),
          Address(
            postcode = "S3 7BS",
            district = "Sheffield City Centre",
            addressNumber = "32",
            buildingName = "HMPPS Digital Studio",
            town = "Sheffield",
            county = "South Yorkshire", status = AddressStatus(code = "ABC123", description = "Main"),
            streetName = "Jump Street"
          )
        )
      ),
      offenderManagers = listOf(
        OffenderManager(
          active = true,
          trustOfficer = TrustOfficer(forenames = "Sheila Linda", surname = "Hancock"),
          staff = Staff(forenames = "Sheila Linda", surname = "Hancock"),
          providerEmployee = ProviderEmployee(forenames = "Sheila Linda", surname = "Hancock"),
          team = Team(
            telephone = "09056714321",
            emailAddress = "first.last@digital.justice.gov.uk",
            code = "C01T04",
            description = "OMU A"
          )
        ),
        OffenderManager(
          active = false,
          trustOfficer = TrustOfficer(forenames = "Dua", surname = "Lipa"),
          staff = Staff(forenames = "Sheila Linda", surname = "Hancock"),
          providerEmployee = ProviderEmployee(forenames = "Sheila Linda", surname = "Hancock"),
          team = Team(
            telephone = "123",
            emailAddress = "dua.lipa@digital.justice.gov.uk",
            code = "C01T04",
            description = "OMU A"
          )
        )
      ),
      offenderProfile = OffenderProfile(ethnicity = "Ainu")
    )
  }

  protected fun userAccessResponse(excluded: Boolean, restricted: Boolean) = UserAccessResponse(
    userRestricted = restricted,
    userExcluded = excluded,
    exclusionMessage = "I am an exclusion message",
    restrictionMessage = "I am a restriction message"
  )

  protected fun groupedDocumentsResponse(): GroupedDocuments {

    return GroupedDocuments(
      documents = listOf(
        CaseDocument(id = "f2943b31-2250-41ab-a04d-004e27a97add", documentName = "test doc.docx", author = "Trevor Small", type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related document"), extendedDescription = "Contact on 21/06/2022 for Information - from 3rd Party", lastModifiedAt = "2022-06-21T20:27:23.407", createdAt = "2022-06-21T20:27:23", parentPrimaryKeyId = 2504763194L),
        CaseDocument(id = "630ca741-cbb6-4f2e-8e86-73825d8c4d82", documentName = "a test.pdf", author = "Jackie Gough", type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related document"), extendedDescription = "Contact on 21/06/2020 for Complementary Therapy Session (NS)", lastModifiedAt = "2022-06-21T20:29:17.324", createdAt = "2022-06-21T20:29:17", parentPrimaryKeyId = 2504763206L),
        CaseDocument(id = "444ca741-cbb6-4f2e-8e86-73825d8c4d83", documentName = "another test.pdf", author = "Brenda Jones", type = CaseDocumentType(code = "NSI_DOCUMENT", description = "Non Statutory Intervention related document"), extendedDescription = "Another description", lastModifiedAt = "2022-06-22T20:29:17.324", createdAt = "2022-06-22T20:29:17", parentPrimaryKeyId = 2504763207L)
      ),
      convictions = listOf(
        ConvictionDocuments(
          convictionId = "2500614567",
          listOf(
            CaseDocument(id = "374136ce-f863-48d8-96dc-7581636e461e", documentName = "GKlicencejune2022.pdf", author = "Tom Thumb", type = CaseDocumentType(code = "CONVICTION_DOCUMENT", description = "Sentence related"), extendedDescription = null, lastModifiedAt = "2022-06-07T17:00:29.493", createdAt = "2022-06-07T17:00:29", parentPrimaryKeyId = 2500614567L),
            CaseDocument(id = "374136ce-f863-48d8-96dc-7581636e123e", documentName = "TDlicencejuly2022.pdf", author = "Wendy Rose", type = CaseDocumentType(code = "CONVICTION_DOCUMENT", description = "Sentence related"), extendedDescription = null, lastModifiedAt = "2022-07-08T10:00:29.493", createdAt = "2022-06-08T10:00:29", parentPrimaryKeyId = 2500614567L),
            CaseDocument(id = "342234a8-b279-4d6e-b9ff-c7910afce95e", documentName = "Part A Recall Report 08 06 2022.doc", author = "Harry Wilson", type = CaseDocumentType(code = "NSI_DOCUMENT", description = "Non Statutory Intervention related document"), extendedDescription = "Non Statutory Intervention for Request for Recall on 08/06/2022", lastModifiedAt = "2022-06-08T14:24:53.217", createdAt = "2022-06-08T14:24:53", parentPrimaryKeyId = 2500049480L)
          )
        )
      )
    )
  }

  protected fun custodialConvictionResponse(sentenceDescription: String = "CJA - Extended Sentence") = Conviction(
    convictionDate = LocalDate.parse("2021-06-10"),
    sentence = Sentence(
      startDate = LocalDate.parse("2022-04-26"),
      terminationDate = LocalDate.parse("2022-04-26"),
      expectedSentenceEndDate = LocalDate.parse("2022-04-26"),
      description = sentenceDescription,
      originalLength = 6,
      originalLengthUnits = "Days",
      secondLength = 10,
      secondLengthUnits = "Days",
      sentenceType = SentenceType(code = "ABC123")
    ),
    active = true,
    offences = listOf(
      Offence(
        mainOffence = true,
        offenceDate = LocalDate.parse("2022-08-26"),
        detail = OffenceDetail(
          mainCategoryDescription = "string", subCategoryDescription = "string",
          description = "Robbery (other than armed robbery)",
          code = "ABC123",
        )
      ),
      Offence(
        mainOffence = false,
        offenceDate = LocalDate.parse("2022-08-26"),
        detail = OffenceDetail(
          mainCategoryDescription = "string", subCategoryDescription = "string",
          description = "Arson",
          code = "ZYX789"
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
      bookingNumber = "12345",
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
      status = CustodyStatus(code = "ABC123", description = "custody status"),
      keyDates = KeyDates(
        conditionalReleaseDate = LocalDate.parse("2020-06-20"),
        expectedPrisonOffenderManagerHandoverDate = LocalDate.parse("2020-06-21"),
        expectedPrisonOffenderManagerHandoverStartDate = LocalDate.parse("2020-06-22"),
        expectedReleaseDate = LocalDate.parse("2020-06-23"),
        hdcEligibilityDate = LocalDate.parse("2020-06-24"),
        licenceExpiryDate = LocalDate.parse("2022-05-10"),
        paroleEligibilityDate = LocalDate.parse("2020-06-26"),
        sentenceExpiryDate = LocalDate.parse("2022-06-10"),
        postSentenceSupervisionEndDate = LocalDate.parse("2022-05-11"),
      ),
      sentenceStartDate = LocalDate.parse("2022-04-26")
    )
  )

  protected fun nonCustodialConvictionResponse() = Conviction(
    convictionDate = LocalDate.parse("2021-06-10"),
    sentence = Sentence(
      startDate = LocalDate.parse("2022-04-26"),
      terminationDate = LocalDate.parse("2022-04-26"),
      expectedSentenceEndDate = LocalDate.parse("2022-04-26"),
      description = "Sentence description",
      originalLength = 6,
      originalLengthUnits = "Days",
      secondLength = 10,
      secondLengthUnits = "Days",
      sentenceType = SentenceType(code = "ABC123")
    ),
    active = true,
    offences = listOf(
      Offence(
        mainOffence = true,
        offenceDate = LocalDate.parse("2022-08-26"),
        detail = OffenceDetail(
          mainCategoryDescription = "string", subCategoryDescription = "string",
          description = "Robbery (other than armed robbery)",
          code = "ABC123"
        )
      ),
      Offence(
        mainOffence = false,
        offenceDate = LocalDate.parse("2022-08-26"),
        detail = OffenceDetail(
          mainCategoryDescription = "string", subCategoryDescription = "string",
          description = "Arson",
          code = "ZYX789"
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
    )
  )
}
