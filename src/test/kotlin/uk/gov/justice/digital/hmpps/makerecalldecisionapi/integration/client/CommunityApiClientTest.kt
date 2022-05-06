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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Registration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Type
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate
import java.time.LocalDateTime

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
}
