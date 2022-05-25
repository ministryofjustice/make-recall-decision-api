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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EnforcementActionType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.MappaResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Officer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProbationArea
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Registration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Type
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
    val expected = ConvictionResponse(
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
            description = "Robbery (other than armed robbery)"
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
      custody = Custody(status = CustodyStatus(code = "ABC123"))
    )

    // when
    val actual = communityApiClient.getConvictions(crn).block()!![0]

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves empty convictions list when no active convictions present`() {
    // given
    val crn = "X123456"
    noActiveConvictionResponse(crn)

    // and
    val expected = emptyList<ConvictionResponse>()

    // when
    val actual = communityApiClient.getConvictions(crn).block()!!

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
  fun `given contact summary request with filter contact parameter true, retrieve contact summaries with request filtered`() {
    // given
    val crn = "X123456"
    contactSummaryResponse(
      crn,
      uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.contactSummaryResponse(),
      true
    )

    // and
    val expected = ContactSummaryResponseCommunity(
      content = listOf(
        Content(
          contactStart = OffsetDateTime.parse("2022-06-03T07:00Z"),
          type = ContactType(description = "Registration Review", systemGenerated = true),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison", systemGenerated = false),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "This is a test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
        )
      )
    )

    // when
    val actual = communityApiClient.getContactSummary(crn, true).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `given contact summary request with filter contact parameter false, retrieve contact summaries with no request filtered`() {
    // given
    val crn = "X123456"
    contactSummaryResponse(
      crn,
      uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.contactSummaryResponse(),
      false
    )

    // and
    val expected = ContactSummaryResponseCommunity(
      content = listOf(
        Content(
          contactStart = OffsetDateTime.parse("2022-06-03T07:00Z"),
          type = ContactType(description = "Registration Review", systemGenerated = true),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison", systemGenerated = false),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "This is a test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
        )
      )
    )

    // when
    val actual = communityApiClient.getContactSummary(crn, true).block()

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
            status = AddressStatus(code = "ABC123", description = "Main"),
            postcode = "S3 7BS",
            addressNumber = "33"
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
  fun `retrieves mappa details`() {
    // given
    val crn = "X123456"
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
        telephone = null
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
    val crn = "X123456"
    releaseSummaryResponse(crn)

    // and
    val expected = ReleaseSummaryResponse(
      lastRelease = LastRelease(date = LocalDate.parse("2017-09-15")),
      lastRecall = LastRecall(date = LocalDate.parse("2020-10-15"))
    )

    // when
    val actual = communityApiClient.getReleaseSummary(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
