package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementActionType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.KeyDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeMainCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeSubCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
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
    val crn = "X123456"
    val staffCode = "STFFCDEU"
    unallocatedConvictionResponse(crn, staffCode)

    // and
    val expected = Conviction(
      convictionDate = LocalDate.parse("2021-06-10"),
      sentence = Sentence(
        startDate = LocalDate.parse("2022-04-26"),
        terminationDate = LocalDate.parse("2022-04-26"),
        expectedSentenceEndDate = LocalDate.parse("2022-04-26"),
        description = "string", originalLength = 0,
        originalLengthUnits = "string",
        sentenceType = SentenceType(code = "ABC123")
      ),
      active = true,
      offences = listOf(
        Offence(
          mainOffence = true,
          detail = OffenceDetail(
            mainCategoryDescription = "string", subCategoryDescription = "string",
            description = "Robbery (other than armed robbery)",
            code = "1234"
          )
        )
      ),
      convictionId = 2500000001,
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
        status = CustodyStatus(code = "ABC123", description = "I am the custody status description"),
        keyDates = KeyDates(licenceExpiryDate = LocalDate.parse("2020-06-25"), postSentenceSupervisionEndDate = LocalDate.parse("2020-06-27"))
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
    val crn = "X123456"
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
    val crn = "X123456"
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
    val crn = "X123456"
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
    val crn = "X123456"
    registrationsResponse(crn)

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
    val crn = "X123456"
    contactSummaryResponse(
      crn,
      uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.contactSummaryResponse()
    )

    // and
    val expected = ContactSummaryResponseCommunity(
      content = listOf(
        Content(
          contactStart = OffsetDateTime.parse("2022-06-03T07:00Z"),
          type = ContactType(description = "Registration Review", systemGenerated = true, code = "COAP", nationalStandard = false, appointment = false),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
          sensitive = null
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison", systemGenerated = false, code = "C204", nationalStandard = false, appointment = false),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "This is a test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
          sensitive = true
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
    val crn = "X123456"
    allOffenderDetailsResponse(crn)

    // and
    val expected = AllOffenderDetailsResponse(
      dateOfBirth = LocalDate.parse("1982-10-24"),
      firstName = "John",
      surname = "Smith",
      gender = "Male",
      contactDetails = ContactDetails(
        addresses = listOf(
          Address(
            postcode = "90220",
            district = "South Central",
            streetName = "Skid Row",
            addressNumber = "45",
            buildingName = "Death Row Records",
            town = "Compton",
            county = "LA", status = AddressStatus(code = "ABC123", description = "Not Main")
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

      )
    )

    // when
    val actual = communityApiClient.getAllOffenderDetails(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves release summaries`() {
    // given
    val crn = "X123456"
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
    val crn = "X123456"
    userAccessAllowed(crn)

    // and
    val expected = UserAccessResponse(
      userRestricted = false,
      userExcluded = false,
      exclusionMessage = null,
      restrictionMessage = null,
    )

    // when
    val actual = communityApiClient.getUserAccess(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
