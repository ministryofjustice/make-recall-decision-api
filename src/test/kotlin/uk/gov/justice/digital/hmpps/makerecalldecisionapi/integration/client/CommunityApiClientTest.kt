package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.EnforcementActionType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Registration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Type
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
            postcode = "S3 7BS",
            district = "Sheffield City Centre",
            addressNumber = "32",
            buildingName = "HMPPS Digital Studio",
            town = "Sheffield",
            county = "South Yorkshire", status = AddressStatus(code = "ABC123", description = "Main")
          ),
          Address(
            town = "Sheffield",
            county = "South Yorkshire",
            buildingName = "HMPPS Digital Studio",
            district = "Sheffield City Centre",
            status = AddressStatus(code = "ABC123", description = "Not Main"),
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
          type = ContactType(description = "Registration Review"),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison"),
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
          type = ContactType(description = "Registration Review"),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison"),
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
