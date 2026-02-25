package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentOffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class CaseSummaryOverviewServiceTest : ServiceTestBase() {

  private lateinit var caseSummaryOverviewService: CaseSummaryOverviewService

  @BeforeEach
  fun setup() {
    caseSummaryOverviewService = CaseSummaryOverviewService(
      deliusClient,
      riskService,
      userAccessValidator,
      recommendationService,
    )

    given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(noAccessLimitations())
  }

  @Test
  fun `retrieves case summary when convictions and offences available`() {
    runTest {
      given(arnApiClient.getAssessments(anyString())).willReturn(Mono.fromCallable { assessmentResponse(crn) })
      given(deliusClient.getOverview(anyString())).willReturn(
        deliusOverviewResponse(
          activeConvictions = listOf(
            activeConviction(),
          ),
        ),
      )
      given(arnApiClient.getRiskManagementPlan(anyString())).willReturn(
        Mono.fromCallable {
          riskManagementResponse(
            crn,
            "COMPLETE",
          )
        },
      )

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val convictions = response.activeConvictions
      val riskFlags = response.risk!!.flags
      val riskManagementPlan = response.risk.riskManagementPlan
      val assessments = response.risk.assessmentInfo

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(deliusPersonalDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("Joe Bloggs")
      assertThat(convictions.size).isEqualTo(1)
      assertThat(convictions[0].additionalOffences.size).isEqualTo(1)
      assertThat(convictions[0].mainOffence.description).isEqualTo("Robbery (other than armed robbery)")
      assertThat(convictions[0].sentence?.description).isEqualTo("CJA - Extended Sentence")
      assertThat(convictions[0].sentence?.length).isEqualTo(6)
      assertThat(convictions[0].sentence?.lengthUnits).isEqualTo("Days")
      assertThat(convictions[0].sentence?.sentenceExpiryDate).isEqualTo("2022-06-10")
      assertThat(convictions[0].sentence?.licenceExpiryDate).isEqualTo("2022-05-10")
      assertThat(convictions[0].sentence?.isCustodial).isTrue
      assertThat(convictions[0].sentence?.custodialStatusCode).isEqualTo("ABC123")
      assertThat(riskFlags!!.size).isEqualTo(1)
      assertThat(riskManagementPlan!!.assessmentStatusComplete).isEqualTo(true)
      assertThat(riskManagementPlan.lastUpdatedDate).isEqualTo("2022-10-01T14:20:27.000Z")
      assertThat(riskManagementPlan.contingencyPlans).isEqualTo("I am the contingency plan text")
      assertThat(assessments?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(assessments?.offenceDataFromLatestCompleteAssessment).isEqualTo(true)
      assertThat(assessments?.offencesMatch).isEqualTo(true)
      assertThat(assessments?.offenceDescription).isEqualTo("Offence details.")
      assertThat(response.lastRelease?.releaseDate).isEqualTo("2017-09-15")
      then(deliusClient).should().getOverview(crn)
    }
  }

  @Test
  fun `retrieves case summary when conviction is non custodial`() {
    runTest {
      given(arnApiClient.getAssessments(anyString())).willReturn(Mono.fromCallable { assessmentResponse(crn) })
      given(deliusClient.getOverview(anyString())).willReturn(deliusOverviewResponse())
      given(arnApiClient.getRiskManagementPlan(anyString())).willReturn(
        Mono.fromCallable {
          riskManagementResponse(
            crn,
            "COMPLETE",
          )
        },
      )

      val response = caseSummaryOverviewService.getOverview(crn)

      assertThat(response.activeConvictions[0].sentence!!.isCustodial).isFalse
      then(deliusClient).should().getOverview(crn)
    }
  }

  @Test
  fun `retrieves case summary when no convictions available`() {
    runTest {
      given(arnApiClient.getAssessments(anyString())).willReturn(Mono.fromCallable { assessmentResponse(crn) })
      given(deliusClient.getOverview(anyString())).willReturn(
        deliusOverviewResponse(
          registerFlags = emptyList(),
          activeConvictions = emptyList(),
        ),
      )
      given(arnApiClient.getRiskManagementPlan(anyString())).willReturn(
        Mono.fromCallable {
          riskManagementResponse(
            crn,
            "COMPLETE",
          )
        },
      )

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val convictionsShouldBeEmpty = response.activeConvictions
      val riskFlagsShouldBeEmpty = response.risk!!.flags

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(deliusPersonalDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("Joe Bloggs")
      assertThat(convictionsShouldBeEmpty).isEmpty()
      assertThat(riskFlagsShouldBeEmpty).isEmpty()
      assertThat(response.lastRelease?.releaseDate).isEqualTo("2017-09-15")
      then(deliusClient).should().getOverview(crn)
    }
  }

  @Test
  fun `given case user not found for user then return user access response details`() {
    runTest {
      given(deliusClient.getUserAccess(anyString(), anyString())).willThrow(PersonNotFoundException("Not found"))

      val response = caseSummaryOverviewService.getOverview(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          CaseSummaryOverviewResponse(
            userAccessResponse(false, false, true).copy(restrictionMessage = null, exclusionMessage = null),
            null,
            emptyList(),
            null,
          ),
        ),
      )
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {
      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val response = caseSummaryOverviewService.getOverview(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          CaseSummaryOverviewResponse(
            userAccessResponse(true, false, false).copy(restrictionMessage = null),
            null,
            emptyList(),
            null,
          ),
        ),
      )
    }
  }

  @Test
  fun `retrieves case summary with optional fields missing`() {
    runTest {
      val crn = "CRN1234"
      given(arnApiClient.getAssessments(anyString())).willReturn(
        Mono.fromCallable {
          AssessmentsResponse(
            crn = crn,
            limitedAccessOffender = null,
            assessments = listOf(
              Assessment(
                offenceDetails = listOf(
                  AssessmentOffenceDetail(
                    type = null,
                    offenceCode = null,
                    offenceSubCode = null,
                    offenceDate = null,
                  ),
                  AssessmentOffenceDetail(
                    type = null,
                    offenceCode = null,
                    offenceSubCode = null,
                    offenceDate = null,
                  ),
                ),
                assessmentStatus = null,
                superStatus = null,
                dateCompleted = null,
                initiationDate = null,
                laterWIPAssessmentExists = null,
                laterSignLockAssessmentExists = null,
                laterPartCompUnsignedAssessmentExists = null,
                laterPartCompSignedAssessmentExists = null,
                laterCompleteAssessmentExists = null,
                offence = null,
                keyConsiderationsCurrentSituation = null,
                furtherConsiderationsCurrentSituation = null,
                supervision = null,
                monitoringAndControl = null,
                interventionsAndTreatment = null,
                victimSafetyPlanning = null,
                contingencyPlans = null,
              ),
            ),
          )
        },
      )

      given(deliusClient.getOverview(anyString())).willReturn(
        deliusOverviewResponse(
          registerFlags = emptyList(),
          activeConvictions = emptyList(),
        ),
      )

      given(arnApiClient.getRiskManagementPlan(anyString())).willReturn(
        Mono.fromCallable {
          riskManagementResponse(
            crn,
            "COMPLETE",
          )
        },
      )

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val convictions = response.activeConvictions
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth.until(LocalDate.now()).years
      val riskFlags = response.risk!!.flags
      val assessments = response.risk?.assessmentInfo

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("Joe Bloggs")
      assertThat(convictions).isEmpty()
      assertThat(riskFlags).isEmpty()
      assertThat(assessments?.lastUpdatedDate).isEqualTo(null)
      assertThat(assessments?.offencesMatch).isEqualTo(false)
      assertThat(assessments?.offenceDescription).isEqualTo(null)
      assertThat(assessments?.offenceDataFromLatestCompleteAssessment).isEqualTo(false)
      then(deliusClient).should().getOverview(crn)
    }
  }
}
