package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.anyString
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.kotlin.doReturn
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CvlApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.ConvictionWithLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.MappaAndRoshHistory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Name
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Overview
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.PersonalDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.PersonalDetails.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.PersonalDetails.Manager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.PersonalDetailsOverview.Identifiers
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.DecisionNotToRecallLetterDocumentMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.PartADocumentMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionCvlDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceMatchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonalDetailsOverview
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ProbationTeam
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocumentType
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.MappaResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Officer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProbationArea
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Reason
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentOffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OtherRisksResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskManagementPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskManagementResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryRiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskToSelfResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskVulnerabilityTypeResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import java.time.LocalDate
import java.time.LocalDateTime

internal abstract class ServiceTestBase {

  @Mock
  protected lateinit var communityApiClient: CommunityApiClient

  @Mock
  protected lateinit var deliusClient: DeliusClient

  @Mock
  protected lateinit var arnApiClient: ArnApiClient

  @Mock
  protected lateinit var cvlApiClient: CvlApiClient

  @Mock
  protected lateinit var recommendationRepository: RecommendationRepository

  @Mock
  protected lateinit var recommendationStatusRepository: RecommendationStatusRepository

  @Mock
  protected lateinit var mockPersonDetailService: PersonDetailsService

  protected lateinit var personDetailsService: PersonDetailsService

  protected lateinit var createAndVaryALicenceService: CreateAndVaryALicenceService

  protected lateinit var riskService: RiskService

  protected lateinit var userAccessValidator: UserAccessValidator

  protected lateinit var documentService: DocumentService

  protected lateinit var convictionService: ConvictionService

  protected lateinit var vulnerabilitiesService: VulnerabilitiesService

  protected lateinit var recommendationService: RecommendationService

  protected lateinit var recommendationStatusService: RecommendationStatusService

  protected lateinit var templateReplacementService: TemplateReplacementService

  protected lateinit var partADocumentMapper: PartADocumentMapper

  protected lateinit var decisionNotToRecallLetterDocumentMapper: DecisionNotToRecallLetterDocumentMapper

  protected val crn = "12345"

  @BeforeEach
  fun userValidatorSetup() {
    lenient().`when`(mockPersonDetailService.getPersonDetails(anyString())).doReturn(personDetailsResponse())
    partADocumentMapper = PartADocumentMapper()
    decisionNotToRecallLetterDocumentMapper = DecisionNotToRecallLetterDocumentMapper()
    userAccessValidator = UserAccessValidator(communityApiClient)
    templateReplacementService = TemplateReplacementService(partADocumentMapper, decisionNotToRecallLetterDocumentMapper)
    documentService = DocumentService(communityApiClient, userAccessValidator)
    convictionService = ConvictionService(communityApiClient, documentService)
    personDetailsService = PersonDetailsService(deliusClient, userAccessValidator, null)
    recommendationService = RecommendationService(recommendationRepository, mockPersonDetailService, templateReplacementService, userAccessValidator, convictionService, RiskService(deliusClient, communityApiClient, arnApiClient, userAccessValidator, null), communityApiClient, null)
    recommendationStatusService = RecommendationStatusService(recommendationStatusRepository)
    riskService = RiskService(deliusClient, communityApiClient, arnApiClient, userAccessValidator, recommendationService)
    createAndVaryALicenceService = CreateAndVaryALicenceService(cvlApiClient)
  }

  fun assessmentResponse(crn: String): AssessmentsResponse {
    return AssessmentsResponse(crn, false, listOf(assessment()))
  }

  fun assessment() = Assessment(
    dateCompleted = "2022-08-26T15:00:08",
    initiationDate = "2020-06-03T11:42:01",
    assessmentStatus = "COMPLETE",
    keyConsiderationsCurrentSituation = null,
    furtherConsiderationsCurrentSituation = null,
    supervision = null,
    monitoringAndControl = null,
    interventionsAndTreatment = null,
    victimSafetyPlanning = null,
    contingencyPlans = null,
    offenceDetails = listOf(
      AssessmentOffenceDetail(
        type = "CURRENT",
        offenceCode = "ABC123",
        offenceSubCode = "",
        offenceDate = "2022-08-26T12:00:00.000"
      )
    ),
    offence = "Juicy offence details.",
    laterCompleteAssessmentExists = false,
    laterPartCompSignedAssessmentExists = false,
    laterPartCompUnsignedAssessmentExists = false,
    laterSignLockAssessmentExists = false,
    laterWIPAssessmentExists = false,
    superStatus = "COMPLETE"
  )

  fun riskResponse(): RiskResponse {
    return RiskResponse(
      riskToSelf = RiskToSelfResponse(
        suicide = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of suicide concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of suicide concerns due to ..."
        ),
        selfHarm = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of self harm concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of self harm concerns due to ..."
        ),
        custody = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of custody concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of custody concerns due to ..."
        ),
        hostelSetting = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of hostel setting concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of hostel setting concerns due to ..."
        ),
        vulnerability = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of vulnerability concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of vulnerability concerns due to ..."
        ),
      ),
      otherRisks = OtherRisksResponse(
        escapeOrAbscond = "YES",
        controlIssuesDisruptiveBehaviour = "YES",
        breachOfTrust = "YES",
        riskToOtherPrisoners = "YES"
      ),
      summary = RiskSummaryRiskResponse(
        whoIsAtRisk = "X, Y and Z are at risk",
        natureOfRisk = "The nature of the risk is X",
        riskImminence = "the risk is imminent and more probably in X situation",
        riskIncreaseFactors = "If offender in situation X the risk can be higher",
        riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
        riskInCommunity = RiskScore(
          veryHigh = null,
          high = listOf(
            "Children",
            "Public",
            "Known adult"
          ),
          medium = listOf("Staff"),
          low = listOf("Prisoners")
        ),
        riskInCustody = RiskScore(
          veryHigh = listOf(
            "Staff",
            "Prisoners"
          ),
          high = listOf("Known adult"),
          medium = null,
          low = listOf(
            "Children",
            "Public"
          )
        ),
        overallRiskLevel = "HIGH"
      ),
      assessedOn = "2022-11-23T00:01:50"
    )
  }

  fun personDetailsResponse() = PersonDetailsResponse(
    personalDetailsOverview = PersonalDetailsOverview(
      fullName = "John Homer Bart Smith",
      name = "John Smith",
      firstName = "John",
      middleNames = "Homer Bart",
      surname = "Smith",
      age = null,
      crn = null,
      dateOfBirth = LocalDate.parse("1982-10-24"),
      gender = "Male",
      ethnicity = "Ainu",
      croNumber = "123456/04A",
      pncNumber = "2004/0712343H",
      mostRecentPrisonerNumber = "G12345",
      nomsNumber = "A1234CR",
      primaryLanguage = "English"
    ),
    addresses = listOf(
      uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address(
        line1 = "Line 1 address",
        line2 = "Line 2 address",
        town = "Town address",
        postcode = "TS1 1ST",
        noFixedAbode = false
      )
    ),
    offenderManager = uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderManager(
      name = "John Smith",
      phoneNumber = "01234567654",
      email = "tester@test.com",
      probationTeam = ProbationTeam(code = "001", label = "Label", localDeliveryUnitDescription = "LDU description"),
      probationAreaDescription = "Probation area description"
    )
  )

  protected fun allReleaseSummariesResponse(): ReleaseSummaryResponse {

    return ReleaseSummaryResponse(
      lastRelease = LastRelease(
        date = LocalDate.parse("2017-09-15"),
        notes = "I am a note",
        reason = Reason(
          code = "reasonCode",
          description = "reasonDescription"
        ),
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
      ),
      lastRecall = LastRecall(
        date = LocalDate.parse("2020-10-15"),
        notes = "I am a second note",
        reason = Reason(
          code = "another reason code",
          description = "another reason description"
        )
      )
    )
  }

  protected fun deliusPersonalDetailsResponse(
    forename: String = "John",
    middleName: String? = "Homer Bart",
    surname: String = "Smith",
    ethnicity: String? = "Ainu",
    primaryLanguage: String? = "English",
    address: Address? = Address(
      postcode = "S3 7BS",
      district = "Sheffield City Centre",
      addressNumber = "32",
      buildingName = "HMPPS Digital Studio",
      town = "Sheffield",
      county = "South Yorkshire",
      streetName = "Jump Street",
      noFixedAbode = null
    ),
    manager: Manager? = Manager(
      team = Manager.Team(
        telephone = "09056714321",
        email = "first.last@digital.justice.gov.uk",
        code = "C01T04",
        name = "OMU A",
        localAdminUnit = "Local admin unit description"
      ),
      staffCode = "C01T123",
      name = Name(
        forename = "Sheila",
        middleName = "Linda",
        surname = "Hancock"
      ),
      provider = Manager.Provider(
        code = "N01",
        name = "NPS North West"
      )
    )
  ) = PersonalDetails(
    personalDetails = DeliusClient.PersonalDetailsOverview(
      name = Name(forename, middleName, surname),
      dateOfBirth = LocalDate.parse("1982-10-24"),
      gender = "Male",
      ethnicity = ethnicity,
      primaryLanguage = primaryLanguage,
      identifiers = Identifiers(
        pncNumber = "2004/0712343H",
        croNumber = "123456/04A",
        nomsNumber = "A1234CR",
        bookingNumber = "G12345"
      ),
    ),
    mainAddress = address,
    communityManager = manager
  )

  protected fun nonCustodialConviction() = Overview.Conviction(
    number = "1",
    mainOffence = DeliusClient.Offence(
      code = "ABC123",
      description = "Robbery (other than armed robbery)",
      date = LocalDate.of(2022, 8, 26)
    ),
    additionalOffences = listOf(
      DeliusClient.Offence(
        code = "ZYX789",
        description = "Arson",
        date = LocalDate.of(2022, 8, 26)
      )
    ),
    sentence = DeliusClient.Sentence(
      description = "Sentence description",
      length = 6,
      lengthUnits = "Days",
      isCustodial = false,
      custodialStatusCode = null,
      licenceExpiryDate = null,
      sentenceExpiryDate = null
    ),
  )

  protected fun custodialConviction(description: String = "CJA - Extended Sentence") = Overview.Conviction(
    number = "1",
    mainOffence = DeliusClient.Offence(
      code = "ABC123",
      description = "Robbery (other than armed robbery)",
      date = LocalDate.of(2022, 8, 26)
    ),
    additionalOffences = listOf(
      DeliusClient.Offence(
        code = "ZYX789",
        description = "Arson",
        date = LocalDate.of(2022, 8, 26)
      )
    ),
    sentence = DeliusClient.Sentence(
      description = description,
      length = 6,
      lengthUnits = "Days",
      isCustodial = true,
      custodialStatusCode = "ABC123",
      licenceExpiryDate = LocalDate.of(2022, 5, 10),
      sentenceExpiryDate = LocalDate.of(2022, 6, 10)
    ),
  )

  protected fun Overview.Conviction.withLicenceConditions(licenceConditions: List<LicenceCondition>) = ConvictionWithLicenceConditions(
    number = number,
    mainOffence = mainOffence,
    additionalOffences = additionalOffences,
    sentence = sentence,
    licenceConditions = licenceConditions
  )

  protected fun deliusOverviewResponse(
    forename: String = "John",
    middleName: String? = "Homer Bart",
    surname: String = "Smith",
    ethnicity: String? = "Ainu",
    primaryLanguage: String? = "English",
    registerFlags: List<String> = listOf("Victim contact"),
    activeConvictions: List<Overview.Conviction> = listOf(nonCustodialConviction())
  ) = Overview(
    personalDetails = DeliusClient.PersonalDetailsOverview(
      name = Name(forename, middleName, surname),
      dateOfBirth = LocalDate.parse("1982-10-24"),
      gender = "Male",
      ethnicity = ethnicity,
      primaryLanguage = primaryLanguage,
      identifiers = Identifiers(
        pncNumber = "2004/0712343H",
        croNumber = "123456/04A",
        nomsNumber = "A1234CR",
        bookingNumber = "G12345"
      ),
    ),
    registerFlags = registerFlags,
    activeConvictions = activeConvictions,
    lastRelease = Overview.Release(
      releaseDate = LocalDate.of(2017, 9, 15),
      recallDate = LocalDate.of(2020, 10, 15)
    )
  )

  protected fun deliusLicenceConditionsResponse(
    activeConvictions: List<ConvictionWithLicenceConditions> = listOf(nonCustodialConviction().withLicenceConditions(emptyList())),
    forename: String = "John",
    middleName: String? = "Homer Bart",
    surname: String = "Smith",
    ethnicity: String? = "Ainu",
    primaryLanguage: String? = "English",
  ) = LicenceConditions(
    personalDetails = DeliusClient.PersonalDetailsOverview(
      name = Name(forename, middleName, surname),
      dateOfBirth = LocalDate.parse("1982-10-24"),
      gender = "Male",
      ethnicity = ethnicity,
      primaryLanguage = primaryLanguage,
      identifiers = Identifiers(
        pncNumber = "2004/0712343H",
        croNumber = "123456/04A",
        nomsNumber = "A1234CR",
        bookingNumber = "G12345"
      ),
    ),
    activeConvictions = activeConvictions,
  )

  protected fun deliusMappaAndRoshHistoryResponse(
    mappa: MappaAndRoshHistory.Mappa? = MappaAndRoshHistory.Mappa(
      category = 0, level = 1, startDate = LocalDate.parse("2021-02-10")
    ),
    roshHistory: List<MappaAndRoshHistory.Rosh> = listOf(
      MappaAndRoshHistory.Rosh(
        active = true,
        type = "ABC123",
        typeDescription = "Victim contact",
        notes = "Notes on case",
        startDate = LocalDate.parse("2021-01-30")
      )
    ),
    forename: String = "John",
    middleName: String? = "Homer Bart",
    surname: String = "Smith",
    ethnicity: String? = "Ainu",
    primaryLanguage: String? = "English",
  ) = MappaAndRoshHistory(
    personalDetails = DeliusClient.PersonalDetailsOverview(
      name = Name(forename, middleName, surname),
      dateOfBirth = LocalDate.parse("1982-10-24"),
      gender = "Male",
      ethnicity = ethnicity,
      primaryLanguage = primaryLanguage,
      identifiers = Identifiers(
        pncNumber = "2004/0712343H",
        croNumber = "123456/04A",
        nomsNumber = "A1234CR",
        bookingNumber = "G12345"
      ),
    ),
    mappa = mappa,
    roshHistory = roshHistory
  )

  protected fun mappaResponse(): MappaResponse {

    return MappaResponse(
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
  }

  protected fun riskSummaryResponse(): RiskSummaryResponse {
    return RiskSummaryResponse(
      whoIsAtRisk = "X, Y and Z are at risk",
      natureOfRisk = "The nature of the risk is X",
      riskImminence = "the risk is imminent and more probably in X situation",
      riskIncreaseFactors = "If offender in situation X the risk can be higher",
      riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
      riskInCommunity = RiskScore(
        veryHigh = null,
        high = listOf(
          "Children",
          "Public",
          "Known adult"
        ),
        medium = listOf("Staff"),
        low = listOf("Prisoners")
      ),
      riskInCustody = RiskScore(
        veryHigh = listOf(
          "Staff",
          "Prisoners"
        ),
        high = listOf("Known adult"),
        medium = null,
        low = listOf(
          "Children",
          "Public"
        )
      ),
      assessedOn = "2022-10-09T08:26:31",
      overallRiskLevel = "HIGH"
    )
  }

  protected fun userAccessResponse(excluded: Boolean, restricted: Boolean, userNotFound: Boolean) = UserAccessResponse(
    userRestricted = restricted,
    userExcluded = excluded,
    userNotFound = userNotFound,
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
            CaseDocument(id = "630ca741-cbb6-4f2e-8e86-73825d8c4999", documentName = "conviction contact doc.pdf", author = "Luke Smith", type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related conviction document"), extendedDescription = "Contact on 23/06/2020 for Complementary Therapy Session (NS)", lastModifiedAt = "2022-06-23T20:29:17.324", createdAt = "2022-06-23T20:29:17", parentPrimaryKeyId = 2504763206L),
            CaseDocument(id = "374136ce-f863-48d8-96dc-7581636e461e", documentName = "GKlicencejune2022.pdf", author = "Tom Thumb", type = CaseDocumentType(code = "CONVICTION_DOCUMENT", description = "Sentence related"), extendedDescription = null, lastModifiedAt = "2022-06-07T17:00:29.493", createdAt = "2022-06-07T17:00:29", parentPrimaryKeyId = 2500614567L),
            CaseDocument(id = "374136ce-f863-48d8-96dc-7581636e123e", documentName = "TDlicencejuly2022.pdf", author = "Wendy Rose", type = CaseDocumentType(code = "CONVICTION_DOCUMENT", description = "Sentence related"), extendedDescription = null, lastModifiedAt = "2022-07-08T10:00:29.493", createdAt = "2022-06-08T10:00:29", parentPrimaryKeyId = 2500614567L),
            CaseDocument(id = "342234a8-b279-4d6e-b9ff-c7910afce95e", documentName = "Part A Recall Report 08 06 2022.doc", author = "Harry Wilson", type = CaseDocumentType(code = "NSI_DOCUMENT", description = "Non Statutory Intervention related document"), extendedDescription = "Non Statutory Intervention for Request for Recall on 08/06/2022", lastModifiedAt = "2022-06-08T14:24:53.217", createdAt = "2022-06-08T14:24:53", parentPrimaryKeyId = 2500049480L)
          )
        )
      )
    )
  }

  protected fun groupedDocumentsNoConvictionDocumentsResponse(): GroupedDocuments {

    return GroupedDocuments(
      documents = listOf(
        CaseDocument(id = "f2943b31-2250-41ab-a04d-004e27a97add", documentName = "test doc.docx", author = "Trevor Small", type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related document"), extendedDescription = "Contact on 21/06/2022 for Information - from 3rd Party", lastModifiedAt = "2022-06-21T20:27:23.407", createdAt = "2022-06-21T20:27:23", parentPrimaryKeyId = 2504763194L),
        CaseDocument(id = "630ca741-cbb6-4f2e-8e86-73825d8c4d82", documentName = "a test.pdf", author = "Jackie Gough", type = CaseDocumentType(code = "CONTACT_DOCUMENT", description = "Contact related document"), extendedDescription = "Contact on 21/06/2020 for Complementary Therapy Session (NS)", lastModifiedAt = "2022-06-21T20:29:17.324", createdAt = "2022-06-21T20:29:17", parentPrimaryKeyId = 2504763206L),
        CaseDocument(id = "444ca741-cbb6-4f2e-8e86-73825d8c4d83", documentName = "another test.pdf", author = "Brenda Jones", type = CaseDocumentType(code = "NSI_DOCUMENT", description = "Non Statutory Intervention related document"), extendedDescription = "Another description", lastModifiedAt = "2022-06-22T20:29:17.324", createdAt = "2022-06-22T20:29:17", parentPrimaryKeyId = 2504763207L)
      ),
      convictions = listOf(
        ConvictionDocuments(
          convictionId = "2500614567",
          null
        )
      )
    )
  }

  protected fun riskManagementResponse(crn: String, status: String, dateCompleted: String? = "2022-10-01T14:20:27"): RiskManagementResponse {
    return RiskManagementResponse(
      crn = crn,
      limitedAccessOffender = true,
      riskManagementPlan = listOf(
        RiskManagementPlanResponse(
          initiationDate = "2021-10-01T14:20:27"
        ),
        RiskManagementPlanResponse(
          assessmentId = 0,
          dateCompleted = dateCompleted,
          partcompStatus = "Part comp status",
          initiationDate = "2022-10-02T14:20:27",
          assessmentStatus = status,
          assessmentType = "LAYER1",
          superStatus = status,
          keyInformationCurrentSituation = "patternOfOffending",
          furtherConsiderationsCurrentSituation = "string",
          supervision = "string",
          monitoringAndControl = "string",
          interventionsAndTreatment = "string",
          victimSafetyPlanning = "string",
          contingencyPlans = "I am the contingency plan text",
          laterWIPAssessmentExists = true,
          latestWIPDate = "2022-10-03T14:20:27",
          laterSignLockAssessmentExists = true,
          latestSignLockDate = "2022-10-04T14:20:27",
          laterPartCompUnsignedAssessmentExists = true,
          latestPartCompUnsignedDate = "2022-10-05T14:20:27",
          laterPartCompSignedAssessmentExists = true,
          latestPartCompSignedDate = "2022-10-06T14:20:27",
          laterCompleteAssessmentExists = true,
          latestCompleteDate = "2022-10-07T14:20:27"
        ),
        RiskManagementPlanResponse(
          initiationDate = "2020-10-02T14:20:27"
        ),
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
      secondLengthUnits = "Months",
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

  protected fun licenceMatchedResponse(licenceId: Int, crn: String): List<LicenceMatchResponse> {
    return listOf(
      LicenceMatchResponse(
        licenceId = licenceId,
        licenceType = "AP",
        licenceStatus = "IN_PROGRESS",
        crn = crn,
      )
    )
  }

  protected fun licenceByIdResponse(): LicenceConditionCvlResponse {
    return LicenceConditionCvlResponse(
      conditionalReleaseDate = "10/06/2022",
      actualReleaseDate = "11/06/2022",
      sentenceStartDate = "12/06/2022",
      sentenceEndDate = "13/06/2022",
      licenceStartDate = "14/06/2022",
      licenceExpiryDate = "15/06/2022",
      topupSupervisionStartDate = "16/06/2022",
      topupSupervisionExpiryDate = "17/06/2022",
      standardLicenceConditions = listOf(LicenceConditionCvlDetail(text = "This is a standard licence condition")),
      standardPssConditions = listOf(LicenceConditionCvlDetail(text = "This is a standard PSS licence condition")),
      additionalLicenceConditions = listOf(LicenceConditionCvlDetail(text = "This is an additional licence condition", expandedText = "Expanded additional licence condition")),
      additionalPssConditions = listOf(LicenceConditionCvlDetail(text = "This is an additional PSS licence condition", expandedText = "Expanded additional PSS licence condition")),
      bespokeConditions = listOf(LicenceConditionCvlDetail(text = "This is a bespoke condition"))
    )
  }

  protected fun expectedPersonDetailsResponse(): PersonalDetailsOverview {
    val dateOfBirth = LocalDate.parse("1982-10-24")

    return PersonalDetailsOverview(
      name = "John Smith",
      firstName = "John",
      surname = "Smith",
      dateOfBirth = dateOfBirth,
      age = dateOfBirth?.until(LocalDate.now())?.years,
      gender = "Male",
      crn = "12345",
      ethnicity = "Ainu",
      middleNames = "Homer Bart",
      croNumber = "123456/04A",
      mostRecentPrisonerNumber = "G12345",
      nomsNumber = "A1234CR",
      pncNumber = "2004/0712343H",
      primaryLanguage = "English",
      fullName = "John Homer Bart Smith"
    )
  }

  fun age(details: PersonalDetails) = details.personalDetails.dateOfBirth.until(LocalDate.now()).years
}
