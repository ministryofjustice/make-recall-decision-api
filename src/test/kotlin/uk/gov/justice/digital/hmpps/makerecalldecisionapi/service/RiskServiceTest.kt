package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.MappaResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Officer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProbationArea
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCustody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class RiskServiceTest {

  private lateinit var riskService: RiskService

  @Mock
  private lateinit var communityApiClient: CommunityApiClient

  @Mock
  private lateinit var arnApiClient: ArnApiClient

  @BeforeEach
  fun setup() {
    riskService = RiskService(communityApiClient, arnApiClient)
  }

  @Test
  fun `retrieves risk`() {
    runBlockingTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse })
      given(arnApiClient.getRiskSummary(anyString()))
        .willReturn(Mono.fromCallable { riskSummaryResponse })
      given(communityApiClient.getAllMappaDetails(anyString()))
        .willReturn(Mono.fromCallable { mappaResponse })

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val riskOfSeriousHarm = response.riskOfSeriousHarm!!
      val mappa = response.mappa!!
      val natureOfRisk = response.natureOfRisk
      val whoIsAtRisk = response.whoIsAtRisk
      val circumstancesIncreaseRisk = response.circumstancesIncreaseRisk
      val factorsToReduceRisk = response.factorsToReduceRisk
      val whenRiskHighest = response.whenRiskHighest

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(riskOfSeriousHarm.overallRisk).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskToChildren).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskToPublic).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskToKnownAdult).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskToStaff).isEqualTo("MEDIUM")
      assertThat(riskOfSeriousHarm.lastUpdated).isEqualTo("2021-10-09")
      assertThat(mappa.isNominal).isTrue() // TODO how is this derived?
      assertThat(mappa.level).isEqualTo("MAPPA Level 1")
      assertThat(mappa.lastUpdated).isEqualTo("10 May 2021")
      assertThat(natureOfRisk?.oasysHeading?.number).isEqualTo("10.2")
      assertThat(natureOfRisk?.oasysHeading?.description).isEqualTo("What is the nature of the risk?")
      assertThat(natureOfRisk?.description)
        .isEqualTo("The nature of the risk is X")
      assertThat(whoIsAtRisk?.oasysHeading?.number).isEqualTo("10.1")
      assertThat(whoIsAtRisk?.oasysHeading?.description).isEqualTo("Who is at risk?")
      assertThat(whoIsAtRisk?.description).isEqualTo("X, Y and Z are at risk")
      assertThat(circumstancesIncreaseRisk?.oasysHeading?.number).isEqualTo("10.4")
      assertThat(circumstancesIncreaseRisk?.oasysHeading?.description).isEqualTo("What circumstances are likely to increase the risk?")
      assertThat(circumstancesIncreaseRisk?.description).isEqualTo("If offender in situation X the risk can be higher")
      assertThat(factorsToReduceRisk?.oasysHeading?.number).isEqualTo("10.5")
      assertThat(factorsToReduceRisk?.oasysHeading?.description).isEqualTo("What factors are likely to reduce the risk?")
      assertThat(factorsToReduceRisk?.description).isEqualTo("Giving offender therapy in X will reduce the risk")
      assertThat(whenRiskHighest?.oasysHeading?.number).isEqualTo("10.3")
      assertThat(whenRiskHighest?.oasysHeading?.description).isEqualTo("When is the risk likely to be greatest?")
      assertThat(whenRiskHighest?.description).isEqualTo("the risk is imminent and more probably in X situation")

      then(arnApiClient).should().getRiskSummary(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getAllMappaDetails(crn)
    }
  }

  @Test
  fun `retrieves risk with optional fields missing`() {
    runBlockingTest {
      val crn = "my wonderful crn"
      given(arnApiClient.getRiskSummary(anyString()))
        .willReturn(
          Mono.fromCallable {
            riskSummaryResponse.copy(
              whoIsAtRisk = null,
              natureOfRisk = null,
              riskImminence = null,
              riskIncreaseFactors = null,
              riskMitigationFactors = null,
              riskInCommunity = RiskInCommunity(
                veryHigh = null,
                high = null,
                medium = null,
                low = null
              ),
              riskInCustody = RiskInCustody(
                veryHigh = null,
                high = null,
                medium = null,
                low = null
              ),
              assessedOn = LocalDateTime.parse("2021-10-09T08:26:31.349"),
              overallRiskLevel = null
            )
          }
        )

      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(
          Mono.fromCallable {
            allOffenderDetailsResponse.copy(
              firstName = null,
              surname = null,
              gender = null,
              contactDetails = ContactDetails(
                addresses = listOf(
                  Address(
                    postcode = null,
                    district = null,
                    addressNumber = null,
                    buildingName = null,
                    town = null,
                    county = null, status = AddressStatus(code = "ABC123", description = "Main")
                  )
                )
              ),
              offenderManagers = listOf(
                OffenderManager(
                  active = true,
                  trustOfficer = TrustOfficer(forenames = null, surname = null),
                  staff = Staff(forenames = null, surname = null),
                  providerEmployee = ProviderEmployee(forenames = null, surname = null),
                  team = Team(
                    telephone = null,
                    emailAddress = null,
                    code = null,
                    description = null
                  )
                )
              )
            )
          }
        )

      given(communityApiClient.getAllMappaDetails(anyString()))
        .willReturn(
          Mono.fromCallable {
            mappaResponse.copy(levelDescription = null)
          }
        )

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val riskOfSeriousHarm = response.riskOfSeriousHarm!!
      val mappa = response.mappa!!
      val natureOfRisk = response.natureOfRisk
      val whoIsAtRisk = response.whoIsAtRisk
      val circumstancesIncreaseRisk = response.circumstancesIncreaseRisk
      val factorsToReduceRisk = response.factorsToReduceRisk
      val whenRiskHighest = response.whenRiskHighest

      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("")
      assertThat(riskOfSeriousHarm.overallRisk).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskToChildren).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskToPublic).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskToKnownAdult).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskToStaff).isEqualTo("")
      assertThat(riskOfSeriousHarm.lastUpdated).isEqualTo("2021-10-09")
      assertThat(mappa.isNominal).isTrue() // TODO how is this derived?
      assertThat(mappa.level).isEqualTo("")
      assertThat(mappa.lastUpdated).isEqualTo("10 May 2021")
      assertThat(natureOfRisk?.description).isEqualTo("")
      assertThat(whoIsAtRisk?.oasysHeading?.number).isEqualTo("10.1")
      assertThat(whoIsAtRisk?.oasysHeading?.description).isEqualTo("Who is at risk?")
      assertThat(whoIsAtRisk?.description).isEqualTo("")
      assertThat(circumstancesIncreaseRisk?.oasysHeading?.number).isEqualTo("10.4")
      assertThat(circumstancesIncreaseRisk?.oasysHeading?.description).isEqualTo("What circumstances are likely to increase the risk?")
      assertThat(circumstancesIncreaseRisk?.description).isEqualTo("")
      assertThat(factorsToReduceRisk?.oasysHeading?.number).isEqualTo("10.5")
      assertThat(factorsToReduceRisk?.oasysHeading?.description).isEqualTo("What factors are likely to reduce the risk?")
      assertThat(factorsToReduceRisk?.description).isEqualTo("")
      assertThat(whenRiskHighest?.oasysHeading?.number).isEqualTo("10.3")
      assertThat(whenRiskHighest?.oasysHeading?.description).isEqualTo("When is the risk likely to be greatest?")
      assertThat(whenRiskHighest?.description).isEqualTo("")
      then(arnApiClient).should().getRiskSummary(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  fun age(offenderDetails: AllOffenderDetailsResponse) = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years

  private val riskSummaryResponse = RiskSummaryResponse(
    whoIsAtRisk = "X, Y and Z are at risk",
    natureOfRisk = "The nature of the risk is X",
    riskImminence = "the risk is imminent and more probably in X situation",
    riskIncreaseFactors = "If offender in situation X the risk can be higher",
    riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
    riskInCommunity = RiskInCommunity(
      veryHigh = null,
      high = listOf(
        "Children",
        "Public",
        "Known adult"
      ),
      medium = listOf("Staff"),
      low = listOf("Prisoners")
    ),
    riskInCustody = RiskInCustody(
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
    assessedOn = LocalDateTime.parse("2021-10-09T08:26:31.349"),
    overallRiskLevel = "HIGH"
  )

  private val mappaResponse = MappaResponse(
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

  private val allOffenderDetailsResponse = AllOffenderDetailsResponse(
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
}
