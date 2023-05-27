package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.joda.time.DateTimeFieldType
import org.joda.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.createPartARequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.documentRequestQuery
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.invalidUpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.managerRecallDecisionRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.managerRecallDecisionRequestWithIsSentToDeliusOnly
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.recommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationForNoRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequestWithClearedValues
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.nowDate
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationControllerTest() : IntegrationTestBase() {

  @Test
  fun `create recommendation`() {
    licenceConditionsResponse(crn, 2500614567)
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    personalDetailsResponse(crn)

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers {
          (
            listOf(
              it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION"))
            )
            )
        }
        .exchange()
        .expectStatus().isCreated
    )

    val idOfRecommendationJustCreated = response.get("id")

    assertThat(response.get("id")).isEqualTo(idOfRecommendationJustCreated)
    assertThat(response.get("status")).isEqualTo("DRAFT")
    val personOnProbation = JSONObject(response.get("personOnProbation").toString())
    assertThat(personOnProbation.get("name")).isEqualTo("John Smith")
    assertThat(personOnProbation.get("gender")).isEqualTo("Male")
    assertThat(personOnProbation.get("ethnicity")).isEqualTo("Ainu")
    assertThat(personOnProbation.get("primaryLanguage")).isEqualTo("English")
    assertThat(personOnProbation.get("dateOfBirth")).isEqualTo("1982-10-24")
    assertThat(personOnProbation.get("mostRecentPrisonerNumber")).isEqualTo("G12345")
    assertThat(personOnProbation.get("croNumber")).isEqualTo("123456/04A")
    assertThat(personOnProbation.get("nomsNumber")).isEqualTo("A1234CR")
    assertThat(personOnProbation.get("pncNumber")).isEqualTo("2004/0712343H")
    val personOnProbationAddress = JSONArray(personOnProbation.get("addresses").toString())
    val address = JSONObject(personOnProbationAddress.get(0).toString())
    assertThat(address.get("line1")).isEqualTo("HMPPS Digital Studio 33 Scotland Street")
    assertThat(address.get("line2")).isEqualTo("Sheffield City Centre")
    assertThat(address.get("town")).isEqualTo("Sheffield")
    assertThat(address.get("postcode")).isEqualTo("S3 7BS")
    assertThat(address.get("noFixedAbode")).isEqualTo(false)
  }

  @Test
  fun `create recommendation with recallConsideredList feature flag active`() {
    userAccessAllowed(crn)
    personalDetailsResponseOneTimeOnly(crn)
    deleteAndCreateRecommendation("{\"flagConsiderRecall\": true, \"unknownFeatureFlag\": true }")

    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/recommendations/$createdRecommendationId")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )
    assertThat(response.get("id")).isEqualTo(createdRecommendationId)
    assertThat(response.get("status")).isEqualTo("RECALL_CONSIDERED")
    val personOnProbation = JSONObject(response.get("personOnProbation").toString())
    assertThat(personOnProbation.get("name")).isEqualTo("John Smith")
    assertThat(personOnProbation.get("gender")).isEqualTo("Male")
    assertThat(personOnProbation.get("ethnicity")).isEqualTo("Ainu")
    assertThat(personOnProbation.get("primaryLanguage")).isEqualTo("English")
    assertThat(personOnProbation.get("dateOfBirth")).isEqualTo("1982-10-24")
    assertThat(personOnProbation.get("mostRecentPrisonerNumber")).isEqualTo("G12345")
    assertThat(personOnProbation.get("croNumber")).isEqualTo("123456/04A")
    assertThat(personOnProbation.get("nomsNumber")).isEqualTo("A1234CR")
    assertThat(personOnProbation.get("pncNumber")).isEqualTo("2004/0712343H")
    val personOnProbationAddress = JSONArray(personOnProbation.get("addresses").toString())
    val address = JSONObject(personOnProbationAddress.get(0).toString())
    assertThat(address.get("line1")).isEqualTo("HMPPS Digital Studio 33 Scotland Street")
    assertThat(address.get("line2")).isEqualTo("Sheffield City Centre")
    assertThat(address.get("town")).isEqualTo("Sheffield")
    assertThat(address.get("postcode")).isEqualTo("S3 7BS")
    assertThat(address.get("noFixedAbode")).isEqualTo(false)
    val recallConsideredList = JSONArray(response.get("recallConsideredList").toString())
    val recallConsidered = JSONObject(recallConsideredList.get(0).toString())
    assertThat(recallConsideredList.length()).isEqualTo(1)
    assertThat(recallConsidered.get("id")).isNotNull
    assertThat(recallConsidered.get("userName")).isEqualTo("some_user")
    assertThat(recallConsidered.get("createdDate")).isNotNull
    assertThat(recallConsidered.get("userId")).isEqualTo("SOME_USER")
    assertThat(recallConsidered.get("recallConsideredDetail")).isEqualTo("I have concerns around their behaviour")
  }

  @Test
  fun `create recommendation when oasys offence date doesn't match in delius`() {
    licenceConditionsResponse(crn, 2500614567)
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isCreated
    )
    assertThat(response.get("indexOffenceDetails")).isEqualTo(null)
  }

  @Test
  fun `create recommendation when Delius and OASys offence codes do not match`() {
    licenceConditionsResponse(crn, 2500614567)
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isCreated
    )
    assertThat(response.get("indexOffenceDetails")).isEqualTo(null)
  }

  @Test
  fun `create recommendation when there is a more recent assessment available from OASys than from Delius`() {
    licenceConditionsResponse(crn, 2500614567)
    oasysAssessmentsResponse(crn, laterCompleteAssessmentExists = true)
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isCreated
    )
    assertThat(response.get("indexOffenceDetails")).isEqualTo(null)
  }

  @Test
  fun `update with manager recall decision when pre-existing decision exists`() {
    userAccessAllowed(crn)
    personalDetailsResponseOneTimeOnly(crn)
    licenceConditionsResponse(crn, 2500614567)
    oasysAssessmentsResponse(crn)
    deleteAndCreateRecommendation()
    updateWithManagerRecallDecision(managerRecallDecisionRequest(decision = "RECALL"))
    updateWithManagerRecallDecision(managerRecallDecisionRequestWithIsSentToDeliusOnly())

    webTestClient.get()
      .uri("/recommendations/$createdRecommendationId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(createdRecommendationId)
      .jsonPath("$.crn").isEqualTo(crn)
      .jsonPath("$.status").isEqualTo("DRAFT")
      .jsonPath("$.managerRecallDecision.createdBy").isEqualTo("some_user")
      .jsonPath("$.managerRecallDecision.createdDate").isNotEmpty
      .jsonPath("$.managerRecallDecision.selected.value").isEqualTo("RECALL")
      .jsonPath("$.managerRecallDecision.allOptions[1].value").isEqualTo("NO_RECALL")
      .jsonPath("$.managerRecallDecision.allOptions[1].text").isEqualTo("Do not recall")
      .jsonPath("$.managerRecallDecision.allOptions[0].value").isEqualTo("RECALL")
      .jsonPath("$.managerRecallDecision.allOptions[0].text").isEqualTo("Recall")
      .jsonPath("$.managerRecallDecision.isSentToDelius").isEqualTo(false)
  }

  private fun updateWithManagerRecallDecision(request: String) {
    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId/manager-recall-decision")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(request)
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")) }
      .exchange()
      .expectStatus().isOk
  }

//  @Test
//  fun `update with refresh and get recommendation`() {
//    // FIXME: if getting stack overflow error when running this test in CircleCI, it may be because there are too many json asserts. Try breaking out the 'refresh' feature into separate tests to reduce the number of asserts in this single test.
//    userAccessAllowed(crn)
//    personalDetailsResponseOneTimeOnly(crn)
//    oasysAssessmentsResponse(crn)
//    deleteAndCreateRecommendation()
//    recommendationModelResponse(crn = crn, delaySeconds = 0L, firstName = "Arthur")
//    roSHSummaryResponse(crn)
//    updateRecommendation(updateRecommendationRequest(), "previousReleases, previousRecalls, mappa, indexOffenceDetails, convictionDetail, personOnProbation, riskOfSeriousHarm")
//    updateRecommendation(secondUpdateRecommendationRequest())
//    webTestClient.get()
//      .uri("/recommendations/$createdRecommendationId")
//      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
//      .exchange()
//      .expectStatus().isOk
//      .expectBody()
//      .jsonPath("$.id").isEqualTo(createdRecommendationId)
//      .jsonPath("$.crn").isEqualTo(crn)
//      .jsonPath("$.status").isEqualTo("DRAFT")
//      .jsonPath("$.recallType.selected.value").isEqualTo("FIXED_TERM")
//      .jsonPath("$.recallType.allOptions[1].value").isEqualTo("STANDARD")
//      .jsonPath("$.recallType.allOptions[1].text").isEqualTo("Standard")
//      .jsonPath("$.recallType.allOptions[0].value").isEqualTo("FIXED_TERM")
//      .jsonPath("$.recallType.allOptions[0].text").isEqualTo("Fixed term")
//      .jsonPath("$.recallType.allOptions[2].value").isEqualTo("NO_RECALL")
//      .jsonPath("$.recallType.allOptions[2].text").isEqualTo("No recall")
//      .jsonPath("$.custodyStatus.selected").isEqualTo("YES_PRISON")
//      .jsonPath("$.custodyStatus.details").isEqualTo("Bromsgrove Police Station\r\nLondon")
//      .jsonPath("$.custodyStatus.allOptions[0].value").isEqualTo("YES_PRISON")
//      .jsonPath("$.custodyStatus.allOptions[0].text").isEqualTo("Yes, prison custody")
//      .jsonPath("$.custodyStatus.allOptions[1].value").isEqualTo("YES_POLICE")
//      .jsonPath("$.custodyStatus.allOptions[1].text").isEqualTo("Yes, police custody")
//      .jsonPath("$.custodyStatus.allOptions[2].value").isEqualTo("NO")
//      .jsonPath("$.custodyStatus.allOptions[2].text").isEqualTo("No")
//      .jsonPath("$.responseToProbation").isEqualTo("They have not responded well")
//      .jsonPath("$.whatLedToRecall").isEqualTo("Increasingly violent behaviour")
//      .jsonPath("$.isThisAnEmergencyRecall").isEqualTo(true)
//      .jsonPath("$.isExtendedSentence").isEqualTo(true)
//      .jsonPath("$.isIndeterminateSentence").isEqualTo(true)
//      .jsonPath("$.indexOffenceDetails").isEqualTo("Juicy offence details.")
//      .jsonPath("$.activeCustodialConvictionCount").isEqualTo(1)
//      .jsonPath("$.hasVictimsInContactScheme.selected").isEqualTo("YES")
//      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].value").isEqualTo("YES")
//      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].text").isEqualTo("Yes")
//      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].value").isEqualTo("NO")
//      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].text").isEqualTo("No")
//      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].value").isEqualTo("NOT_APPLICABLE")
//      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].text").isEqualTo("N/A")
//      .jsonPath("$.dateVloInformed").isEqualTo("2022-08-01")
//      .jsonPath("$.personOnProbation.name").isEqualTo("Arthur Smith")
//      .jsonPath("$.personOnProbation.gender").isEqualTo("Male")
//      .jsonPath("$.personOnProbation.primaryLanguage").isEqualTo("English")
//      .jsonPath("$.personOnProbation.hasBeenReviewed").isEqualTo(true)
//      .jsonPath("$.personOnProbation.mappa.level").isEqualTo(1)
//      .jsonPath("$.personOnProbation.mappa.category").isEqualTo(0)
//      .jsonPath("$.personOnProbation.mappa.lastUpdatedDate").isEqualTo("2021-02-10")
//      .jsonPath("$.personOnProbation.mappa.hasBeenReviewed").isEqualTo(true)
//      .jsonPath("$.personOnProbation.hasBeenReviewed").isEqualTo(true)
//      .jsonPath("$.alternativesToRecallTried.selected[0].value").isEqualTo("WARNINGS_LETTER")
//      .jsonPath("$.alternativesToRecallTried.selected[0].details").isEqualTo("We sent a warning letter on 27th July 2022")
//      .jsonPath("$.alternativesToRecallTried.selected[1].value").isEqualTo("DRUG_TESTING")
//      .jsonPath("$.alternativesToRecallTried.selected[1].details").isEqualTo("Drug test passed")
//      .jsonPath("$.alternativesToRecallTried.allOptions[0].value").isEqualTo("WARNINGS_LETTER")
//      .jsonPath("$.alternativesToRecallTried.allOptions[0].text").isEqualTo("Warnings/licence breach letters")
//      .jsonPath("$.alternativesToRecallTried.allOptions[1].value").isEqualTo("DRUG_TESTING")
//      .jsonPath("$.alternativesToRecallTried.allOptions[1].text").isEqualTo("Drug testing")
//      .jsonPath("$.hasArrestIssues.selected").isEqualTo(true)
//      .jsonPath("$.hasArrestIssues.details").isEqualTo("Violent behaviour")
//      .jsonPath("$.hasContrabandRisk.selected").isEqualTo(true)
//      .jsonPath("$.hasContrabandRisk.details").isEqualTo("Contraband risk details")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.selected[0]").isEqualTo("GOOD_BEHAVIOUR")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.selected[1]").isEqualTo("NO_OFFENCE")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[0].text").isEqualTo("Be of good behaviour")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[0].value").isEqualTo("GOOD_BEHAVIOUR")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[1].text").isEqualTo("Not to commit any offence")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[1].value").isEqualTo("NO_OFFENCE")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[2].text").isEqualTo("Tell your supervising officer if you use a name which is different to the name or names which appear on your licence.")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[2].value").isEqualTo("NAME_CHANGE")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[3].text").isEqualTo("Tell your supervising officer if you change or add any contact details, including phone number or email.")
//      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[3].value").isEqualTo("CONTACT_DETAILS")
//      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.selectedOptions[0].mainCatCode").isEqualTo("NLC5")
//      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.selectedOptions[0].subCatCode").isEqualTo("NST14")
//      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].title").isEqualTo("Disclosure of information")
//      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].details").isEqualTo("Notify your supervising officer of any intimate relationships")
//      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].note").isEqualTo("Persons wife is Joan Smyth")
//      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].mainCatCode").isEqualTo("NLC5")
//      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].subCatCode").isEqualTo("NST14")
//      .jsonPath("$.isUnderIntegratedOffenderManagement.selected").isEqualTo("YES")
//      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[0].text").isEqualTo("Yes")
//      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[0].value").isEqualTo("YES")
//      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[1].text").isEqualTo("No")
//      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[1].value").isEqualTo("NO")
//      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[2].text").isEqualTo("N/A")
//      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[2].value").isEqualTo("NOT_APPLICABLE")
//      .jsonPath("$.localPoliceContact.contactName").isEqualTo("Thomas Magnum")
//      .jsonPath("$.localPoliceContact.phoneNumber").isEqualTo("555-0100")
//      .jsonPath("$.localPoliceContact.faxNumber").isEqualTo("555-0199")
//      .jsonPath("$.localPoliceContact.emailAddress").isEqualTo("thomas.magnum@gmail.com")
//      .jsonPath("$.vulnerabilities.selected[0].value").isEqualTo("RISK_OF_SUICIDE_OR_SELF_HARM")
//      .jsonPath("$.vulnerabilities.selected[0].details").isEqualTo("Risk of suicide")
//      .jsonPath("$.vulnerabilities.selected[1].value").isEqualTo("RELATIONSHIP_BREAKDOWN")
//      .jsonPath("$.vulnerabilities.selected[1].details").isEqualTo("Divorced")
//      .jsonPath("$.vulnerabilities.allOptions[0].text").isEqualTo("Risk of suicide or self harm")
//      .jsonPath("$.vulnerabilities.allOptions[0].value").isEqualTo("RISK_OF_SUICIDE_OR_SELF_HARM")
//      .jsonPath("$.vulnerabilities.allOptions[1].text").isEqualTo("Relationship breakdown")
//      .jsonPath("$.vulnerabilities.allOptions[1].value").isEqualTo("RELATIONSHIP_BREAKDOWN")
//      .jsonPath("$.convictionDetail.indexOffenceDescription").isEqualTo("Robbery (other than armed robbery)")
//      .jsonPath("$.convictionDetail.dateOfOriginalOffence").isEqualTo("2022-04-24")
//      .jsonPath("$.convictionDetail.dateOfSentence").isEqualTo("2022-04-26")
//      .jsonPath("$.convictionDetail.lengthOfSentence").isEqualTo("12")
//      .jsonPath("$.convictionDetail.lengthOfSentenceUnits").isEqualTo("days")
//      .jsonPath("$.convictionDetail.sentenceDescription").isEqualTo("Extended Determinate Sentence")
//      .jsonPath("$.convictionDetail.licenceExpiryDate").isEqualTo("2020-06-25")
//      .jsonPath("$.convictionDetail.sentenceExpiryDate").isEqualTo("2020-06-28")
//      .jsonPath("$.convictionDetail.sentenceSecondLength").isEqualTo("19")
//      .jsonPath("$.convictionDetail.sentenceSecondLengthUnits").isEqualTo("days")
//      .jsonPath("$.convictionDetail.custodialTerm").isEqualTo("12 days")
//      .jsonPath("$.convictionDetail.extendedTerm").isEqualTo("19 days")
//      .jsonPath("$.convictionDetail.hasBeenReviewed").isEqualTo(true)
//      .jsonPath("$.region").isEqualTo("NPS North West")
//      .jsonPath("$.localDeliveryUnit").isEqualTo("Some description")
//      .jsonPath("$.fixedTermAdditionalLicenceConditions.selected").isEqualTo(true)
//      .jsonPath("$.fixedTermAdditionalLicenceConditions.details").isEqualTo("This is an additional licence condition")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[0].details").isEqualTo("Some behaviour similar to index offence")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[0].value").isEqualTo("BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[1].details").isEqualTo("Behaviour leading to sexual or violent behaviour")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[1].value").isEqualTo("BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[2].details").isEqualTo("Out of touch")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[2].value").isEqualTo("OUT_OF_TOUCH")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[0].text").isEqualTo("Some behaviour similar to index offence")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[0].value").isEqualTo("BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[1].text").isEqualTo("Behaviour leading to sexual or violent behaviour")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[1].value").isEqualTo("BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[2].text").isEqualTo("Out of touch")
//      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[2].value").isEqualTo("OUT_OF_TOUCH")
//      .jsonPath("$.isMainAddressWherePersonCanBeFound.selected").isEqualTo(false)
//      .jsonPath("$.isMainAddressWherePersonCanBeFound.details").isEqualTo("123 Acacia Avenue, Birmingham, B23 1AV")
//      .jsonPath("$.whyConsideredRecall.selected").isEqualTo("RISK_INCREASED")
//      .jsonPath("$.whyConsideredRecall.allOptions[0].value").isEqualTo("RISK_INCREASED")
//      .jsonPath("$.whyConsideredRecall.allOptions[0].text").isEqualTo("Your risk is assessed as increased")
//      .jsonPath("$.whyConsideredRecall.allOptions[1].value").isEqualTo("CONTACT_STOPPED")
//      .jsonPath("$.whyConsideredRecall.allOptions[1].text").isEqualTo("Contact with your probation practitioner has broken down")
//      .jsonPath("$.whyConsideredRecall.allOptions[2].value").isEqualTo("RISK_INCREASED_AND_CONTACT_STOPPED")
//      .jsonPath("$.whyConsideredRecall.allOptions[2].text").isEqualTo("Your risk is assessed as increased and contact with your probation practitioner has broken down")
//      .jsonPath("$.reasonsForNoRecall.licenceBreach").isEqualTo("Reason for breaching licence")
//      .jsonPath("$.reasonsForNoRecall.noRecallRationale").isEqualTo("Rationale for no recall")
//      .jsonPath("$.reasonsForNoRecall.popProgressMade").isEqualTo("Progress made so far detail")
//      .jsonPath("$.reasonsForNoRecall.futureExpectations").isEqualTo("Future expectations detail")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.selected").isEqualTo("TELEPHONE")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.allOptions[0].value").isEqualTo("TELEPHONE")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.allOptions[0].text").isEqualTo("Telephone")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.allOptions[1].value").isEqualTo("VIDEO_CALL")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.allOptions[1].text").isEqualTo("Video call")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.allOptions[2].value").isEqualTo("OFFICE_VISIT")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.allOptions[2].text").isEqualTo("Office visit")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.allOptions[3].value").isEqualTo("HOME_VISIT")
//      .jsonPath("$.nextAppointment.howWillAppointmentHappen.allOptions[3].text").isEqualTo("Home visit")
//      .jsonPath("$.nextAppointment.dateTimeOfAppointment").isEqualTo("2022-04-24T20:39:00.000Z")
//      .jsonPath("$.nextAppointment.probationPhoneNumber").isEqualTo("01238282838")
//      .jsonPath("$.offenceAnalysis").isEqualTo("This is the offence analysis")
//      .jsonPath("$.hasBeenReviewed").doesNotExist()
//      .jsonPath("$.previousReleases.lastReleaseDate").isEqualTo("2017-09-15")
//      .jsonPath("$.previousReleases.lastReleasingPrisonOrCustodialEstablishment").isEqualTo("Addiewell")
//      .jsonPath("$.previousReleases.previousReleaseDates").isEqualTo("2015-04-24")
//      .jsonPath("$.previousReleases.hasBeenReleasedPreviously").isEqualTo(true)
//      .jsonPath("$.previousRecalls.lastRecallDate").isEqualTo("2020-10-15")
//      .jsonPath("$.previousRecalls.previousRecallDates[0]").isEqualTo("2018-10-10")
//      .jsonPath("$.previousRecalls.previousRecallDates[1]").isEqualTo("2016-04-30")
//      .jsonPath("$.previousRecalls.hasBeenRecalledPreviously").isEqualTo(true)
//      .jsonPath("$.recallConsideredList[0].recallConsideredDetail").isEqualTo("This is an updated recall considered detail")
//      .jsonPath("$.currentRoshForPartA.riskToChildren").isEqualTo("LOW")
//      .jsonPath("$.currentRoshForPartA.riskToPublic").isEqualTo("HIGH")
//      .jsonPath("$.currentRoshForPartA.riskToKnownAdult").isEqualTo("MEDIUM")
//      .jsonPath("$.currentRoshForPartA.riskToStaff").isEqualTo("VERY_HIGH")
//      .jsonPath("$.currentRoshForPartA.riskToPrisoners").isEqualTo("NOT_APPLICABLE")
//      .jsonPath("$.countersignSpoName").isEqualTo("some_user")
//      .jsonPath("$.countersignSpoTelephone").isEqualTo("12345678")
//      .jsonPath("$.countersignSpoDateTime").isNotEmpty
//      .jsonPath("$.countersignSpoExposition").isEqualTo("Spo comments on case")
//      .jsonPath("$.countersignAcoName").isEqualTo("some_user")
//      .jsonPath("$.countersignAcoTelephone").isEqualTo("87654321")
//      .jsonPath("$.countersignAcoDateTime").isNotEmpty
//      .jsonPath("$.countersignAcoExposition").isEqualTo("Aco comments on case")
//  }

  @Test
  fun `update roshSummary with refresh and get recommendation`() {
    userAccessAllowed(crn)
    personalDetailsResponseOneTimeOnly(crn)
    deleteAndCreateRecommendation()
    roSHSummaryResponse(crn)
    updateRecommendation(updateRecommendationRequest(), "riskOfSeriousHarm")
    webTestClient.get()
      .uri("/recommendations/$createdRecommendationId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.roshSummary.riskOfSeriousHarm.overallRisk").isEqualTo("HIGH")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToChildren").isEqualTo("LOW")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToPublic").isEqualTo("LOW")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToKnownAdult").isEqualTo("HIGH")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToStaff").isEqualTo("VERY_HIGH")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToPrisoners").isEqualTo("VERY_HIGH")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToChildren").isEqualTo("HIGH")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToPublic").isEqualTo("HIGH")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToKnownAdult").isEqualTo("HIGH")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToStaff").isEqualTo("MEDIUM")
      .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToPrisoners").isEqualTo("")
      .jsonPath("$.roshSummary.lastUpdatedDate").isEqualTo("2022-05-19T08:26:31.000Z")
  }

  @Test
  fun `given an update that clears hidden fields then save in database and get recommendation with null values for hidden fields`() {
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    deleteAndCreateRecommendation()
    updateRecommendation(updateRecommendationRequest())
    updateRecommendation(updateRecommendationRequestWithClearedValues())

    webTestClient.get()
      .uri(
        "/recommendations/$createdRecommendationId"
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(createdRecommendationId)
      .jsonPath("$.crn").isEqualTo(crn)
      .jsonPath("$.status").isEqualTo("DRAFT")
      .jsonPath("$.hasVictimsInContactScheme.selected").isEqualTo("NO")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].value").isEqualTo("YES")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].text").isEqualTo("Yes")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].value").isEqualTo("NO")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].text").isEqualTo("No")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].value").isEqualTo("NOT_APPLICABLE")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].text").isEqualTo("N/A")
      .jsonPath("$.dateVloInformed").isEqualTo(null)
      .jsonPath("$.hasArrestIssues.selected").isEqualTo(false)
      .jsonPath("$.hasArrestIssues.details").isEqualTo(null)
      .jsonPath("$.hasContrabandRisk.selected").isEqualTo(false)
      .jsonPath("$.hasContrabandRisk.details").isEqualTo(null)

    val result = repository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name))
    assertThat(result[0].data.lastModifiedBy, equalTo("SOME_USER"))
    assertThat(result[0].data.lastModifiedByUserName, equalTo("some_user"))
  }

  @Test
  fun `generate a DNTR document from recommendation data`() {
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    deleteAndCreateRecommendation()
    recommendationModelResponse(crn)
    updateRecommendation(updateRecommendationRequest())

    val featureFlagString = "{\"flagSendDomainEvent\": false, \"unknownFeatureFlag\": true }"

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations/$createdRecommendationId/no-recall-letter")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(documentRequestQuery("download-docx"))
        )
        .headers {
          (
            listOf(
              it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")),
              it.set("X-Feature-Flags", featureFlagString)
            )
            )
        }
        .exchange()
        .expectStatus().isOk

    )

    assertThat(response.get("fileName")).isEqualTo("No_Recall_" + nowDate() + "_Smith_J_A12345.docx")
    assertNotNull(response.get("fileContents"))

    val result = repository.findByCrnAndStatus(crn, listOf(Status.DOCUMENT_DOWNLOADED.name))
    assertThat(result[0].data.userNameDntrLetterCompletedBy, equalTo("some_user"))
    assertThat(result[0].data.personOnProbation?.addresses?.get(0)?.line1, equalTo("HMPPS Digital Studio 33 Scotland Street"))
    assertThat(result[0].data.personOnProbation?.addresses?.get(0)?.line2, equalTo("Sheffield City Centre"))
    assertThat(result[0].data.personOnProbation?.addresses?.get(0)?.town, equalTo("Sheffield"))
    assertThat(result[0].data.personOnProbation?.addresses?.get(0)?.postcode, equalTo("S3 7BS"))
    assertThat(result[0].data.personOnProbation?.addresses?.get(0)?.noFixedAbode, equalTo(false))

    assertNotNull(result[0].data.lastDntrLetterADownloadDateTime)
    assertThat(result[0].data.status, equalTo(Status.DOCUMENT_DOWNLOADED))
  }

  @Test
  fun `get recommendations when no open recommendation available`() {
    // given
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    deleteAndCreateRecommendation()
    recommendationModelResponse(crn)
    updateRecommendation(updateRecommendationRequest())
    val featureFlagString = "{\"flagSendDomainEvent\": false, \"flagTriggerWork\": false }"

    // and
    webTestClient.post()
      .uri("/recommendations/$createdRecommendationId/part-a")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(createPartARequest())
      )
      .headers {
        (
          listOf(
            it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")),
            it.set("X-Feature-Flags", featureFlagString)
          )
          )
      }
      .exchange()
      .expectStatus().isOk

    // and
    val response = convertResponseToJSONArray(
      webTestClient.get()
        .uri("/recommendations/$createdRecommendationId/statuses")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")))) }
        .exchange()
        .expectStatus().isOk
    )

    // and
    val recommendationStatus = JSONObject(response.get(0).toString())
    assertThat(recommendationStatus.get("name")).isEqualTo("CLOSED")

    // when
    val results = convertResponseToJSONObject(
      webTestClient.get()
        .uri(
          "/cases/$crn/recommendations"
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(JSONArray(results.get("recommendations").toString()).isEmpty)
  }

  @Test
  fun `preview a DNTR document from recommendation data`() {
    userAccessAllowed(crn)
    oasysAssessmentsResponse(crn)
    personalDetailsResponse(crn)
    licenceConditionsResponse(crn, 2500614567)
    deleteAndCreateRecommendation()
    updateRecommendation(updateRecommendationForNoRecallRequest())

    val nextAppointmentDateTimeString = JSONObject(updateRecommendationForNoRecallRequest()).getJSONObject("nextAppointment").getString("dateTimeOfAppointment")
    val nextAppointmentDateTime = LocalDateTime(ZonedDateTime.parse(nextAppointmentDateTimeString).toInstant().toEpochMilli())

    webTestClient.post()
      .uri("/recommendations/$createdRecommendationId/no-recall-letter")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(documentRequestQuery("preview"))
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.letterContent.letterAddress").isEqualTo("John Smith\nHMPPS Digital Studio 33 Scotland Street\nSheffield City Centre\nSheffield\nS3 7BS")
      .jsonPath("$.letterContent.letterDate").isEqualTo(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
      .jsonPath("$.letterContent.salutation").isEqualTo("Dear John Smith,")
      .jsonPath("$.letterContent.letterTitle").isEqualTo("DECISION NOT TO RECALL")
      .jsonPath("$.letterContent.section1").isEqualTo(
        "I am writing to you because you have breached your licence conditions in such a way that .\n\n" +
          "This breach has been discussed with a Probation manager and a decision has been made that you will not be recalled to prison. This letter explains this decision. If you have any questions, please contact me.\n\n" +
          "Reason for breaching licence\n\n" +
          "Rationale for no recall\n\n" +
          "Progress made so far detail\n\n" +
          "Thoughts on bad behaviour\n\n" +
          "Future expectations detail\n\n" +
          "I hope our conversation and/or this letter has helped to clarify what is required of you going forward and that we can continue to work together to enable you to successfully complete your licence period.\n\n" +
          "Your next appointment is by telephone on:"
      )
      .jsonPath("$.letterContent.section2").isEqualTo("Sunday 24th April 2022 at ${nextAppointmentDateTime.get(DateTimeFieldType.hourOfDay())}:39am\n")
      .jsonPath("$.letterContent.section3").isEqualTo("You must please contact me immediately if you are not able to keep this appointment. Should you wish to discuss anything before then, please contact me by the following telephone number: 01238282838\n")
      .jsonPath("$.letterContent.signedByParagraph").isEqualTo("Yours sincerely,\n\n\nProbation Practitioner/Senior Probation Officer/Head of PDU")
  }

  @Test
  fun `generate a Part A from recommendation data`() {
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    personalDetailsResponseOneTimeOnly(crn)
    licenceConditionsResponse(crn, 2500614567)
    personalDetailsResponseOneTimeOnly(crn)
    deleteAndCreateRecommendation()
    personalDetailsResponseOneTimeOnly(crn)
    updateRecommendation(updateRecommendationRequest())

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations/$createdRecommendationId/part-a")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(createPartARequest())
        )
        .headers {
          (
            listOf(
              it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")),
            )
            )
        }
        .exchange()
        .expectStatus().isOk
    )

    assertThat(response.get("fileName")).isEqualTo("NAT_Recall_Part_A_" + nowDate() + "_Smith_J_A12345.docx")
    assertNotNull(response.get("fileContents"))

    val result = repository.findByCrnAndStatus(crn, listOf(Status.DOCUMENT_DOWNLOADED.name))
    assertThat(result[0].data.userNamePartACompletedBy, equalTo("some_user"))
    assertThat(result[0].data.userEmailPartACompletedBy, equalTo("some.user@email.com"))
    assertNotNull(result[0].data.lastPartADownloadDateTime)
    assertThat(result[0].data.status, equalTo(Status.DOCUMENT_DOWNLOADED))
  }

  @Test
  fun `handles scenario where no recommendation exists for given id on update`() {
    webTestClient.patch()
      .uri("/recommendations/999")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(updateRecommendationRequest())
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
      .jsonPath("$.userMessage")
      .isEqualTo("No recommendation available: No recommendation found for id: 999")
  }

  @Test
  fun `handles scenario where no recommendation exists for given id`() {
    runTest {
      webTestClient.get()
        .uri("/recommendations/999")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
        .jsonPath("$.userMessage")
        .isEqualTo("No recommendation available: No recommendation found for id: 999")
    }
  }

  @Test
  fun `retrieves recommendations for recommendation tab`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      deleteAndCreateRecommendation()
      updateRecommendation(Status.RECALL_CONSIDERED)

      webTestClient.get()
        .uri("/cases/$crn/recommendations")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.fullName").isEqualTo("John Homer Bart Smith")
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("40")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.recommendations[0].recommendationId").isNotEmpty
        .jsonPath("$.recommendations[0].lastModifiedByName").isEqualTo("some_user")
        .jsonPath("$.recommendations[0].createdDate").isNotEmpty
        .jsonPath("$.recommendations[0].lastModifiedDate").isNotEmpty
        .jsonPath("$.recommendations[0].status").isEqualTo("RECALL_CONSIDERED")
        .jsonPath("$.recommendations[0].recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.activeRecommendation.recallConsideredList.length()").isEqualTo(1)
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userName").isEqualTo("some_user")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].createdDate").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].id").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userId").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail").isEqualTo("This is an updated recall considered detail")
        .jsonPath("$.activeRecommendation.status").isEqualTo("RECALL_CONSIDERED")
    }
  }

  @Test
  fun `access denied when insufficient privileges used on recommendations tab request`() {
    runTest {
      webTestClient.get()
        .uri("/cases/$crn/recommendations")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }

  @Test
  fun `access denied when insufficient privileges used for creation request`() {
    val crn = "X123456"
    webTestClient.post()
      .uri("/recommendations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(recommendationRequest(crn))
      )
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `given case is excluded when fetching a recommendation then only return user access details`() {
    runTest {
      userAccessAllowedOnce(crn)
      personalDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      deleteAndCreateRecommendation()
      userAccessExcluded(crn)
      webTestClient.get()
        .uri("/recommendations/$createdRecommendationId")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(false)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(true)
        .jsonPath("$.userAccessResponse.exclusionMessage").isEqualTo("You are excluded from viewing this offender record. Please contact OM John Smith")
        .jsonPath("$.userAccessResponse.restrictionMessage").isEmpty
    }
  }

  @Test
  fun `given case is excluded when creating a recommendation then only return user access details`() {
    runTest {
      userAccessExcluded(crn)
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(false)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(true)
        .jsonPath("$.userAccessResponse.exclusionMessage").isEqualTo("You are excluded from viewing this offender record. Please contact OM John Smith")
        .jsonPath("$.userAccessResponse.restrictionMessage").isEmpty
    }
  }

  @Test
  fun `given case is excluded when updating a recommendation then only return user access details`() {
    runTest {
      userAccessAllowedOnce(crn)
      personalDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      deleteAndCreateRecommendation()
      userAccessExcluded(crn)

      webTestClient.patch()
        .uri("/recommendations/$createdRecommendationId")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(updateRecommendationRequest())
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Test
  fun `given case invalid recall type present in update then return 400`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      deleteAndCreateRecommendation()

      webTestClient.patch()
        .uri("/recommendations/$createdRecommendationId")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(invalidUpdateRecommendationRequest())
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().is4xxClientError
    }
  }

  private fun getRecommendation(): JSONObject {
    return convertResponseToJSONObject(
      webTestClient.get()
        .uri("/recommendations/$createdRecommendationId")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
    )
  }

  @Test
  fun `given case is excluded when generating a Part A then only return user access details`() {
    runTest {
      userAccessAllowedOnce(crn)
      personalDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      deleteAndCreateRecommendation()
      userAccessExcluded(crn)
      webTestClient.post()
        .uri("/recommendations/$createdRecommendationId/part-a")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(createPartARequest())
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(false)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(true)
        .jsonPath("$.userAccessResponse.exclusionMessage").isEqualTo("You are excluded from viewing this offender record. Please contact OM John Smith")
        .jsonPath("$.userAccessResponse.restrictionMessage").isEmpty
    }
  }
}
