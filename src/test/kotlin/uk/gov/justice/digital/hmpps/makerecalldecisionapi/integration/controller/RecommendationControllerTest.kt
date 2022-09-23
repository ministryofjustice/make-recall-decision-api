package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.createPartARequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.recommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.secondUpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationForNoRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequestWithClearedValues
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.nowDate

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationControllerTest() : IntegrationTestBase() {

  @Test
  fun `create recommendation`() {
    licenceConditionsResponse(crn, 2500614567)
    convictionResponse(crn, "011")
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    mappaDetailsResponse(crn, category = 1, level = 1)
    allOffenderDetailsResponse(crn)
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

    val idOfRecommendationJustCreated = response.get("id")

    assertThat(response.get("id")).isEqualTo(idOfRecommendationJustCreated)
    assertThat(response.get("status")).isEqualTo("DRAFT")
    val personOnProbation = JSONObject(response.get("personOnProbation").toString())
    assertThat(personOnProbation.get("name")).isEqualTo("John Smith")
    assertThat(personOnProbation.get("gender")).isEqualTo("Male")
    assertThat(personOnProbation.get("ethnicity")).isEqualTo("Ainu")
    assertThat(personOnProbation.get("dateOfBirth")).isEqualTo("1982-10-24")
    assertThat(personOnProbation.get("mostRecentPrisonerNumber")).isEqualTo("G12345")
    assertThat(personOnProbation.get("croNumber")).isEqualTo("123456/04A")
    assertThat(personOnProbation.get("nomsNumber")).isEqualTo("A1234CR")
    assertThat(personOnProbation.get("pncNumber")).isEqualTo("2004/0712343H")
    assertThat(JSONObject(JSONObject(response.get("personOnProbation").toString()).get("mappa").toString()).get("category")).isEqualTo(1)
    assertThat(JSONObject(JSONObject(response.get("personOnProbation").toString()).get("mappa").toString()).get("level")).isEqualTo(1)
    assertThat(response.get("indexOffenceDetails").toString()).isEqualTo("Juicy offence details.")

    val personOnProbationAddress = JSONArray(personOnProbation.get("addresses").toString())
    val address = JSONObject(personOnProbationAddress.get(0).toString())
    assertThat(address.get("line1")).isEqualTo("HMPPS Digital Studio 33 Scotland Street")
    assertThat(address.get("line2")).isEqualTo("Sheffield City Centre")
    assertThat(address.get("town")).isEqualTo("Sheffield")
    assertThat(address.get("postcode")).isEqualTo("S3 7BS")
    assertThat(address.get("noFixedAbode")).isEqualTo(false)
  }

  @Test
  fun `create recommendation when oasys offence date doesn't match in delius`() {
    licenceConditionsResponse(crn, 2500614567)
    convictionResponse(crn, "011", offenceDate = "1984-04-24T20:39:47.778Z")
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    mappaDetailsResponse(crn, category = 1, level = 1)
    allOffenderDetailsResponse(crn)
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
  fun `create recommendation when no active conviction available`() {
    userAccessAllowed(crn)
    licenceConditionsResponse(crn, 2500614567)
    oasysAssessmentsResponse(crn)
    mappaDetailsResponse(crn, category = 1, level = 1)
    allOffenderDetailsResponse(crn)
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
  fun `create recommendation when offence type is not current`() {
    licenceConditionsResponse(crn, 2500614567)
    convictionResponse(crn, "011")
    oasysAssessmentsResponse(crn, offenceType = "NOT_CURRENT")
    userAccessAllowed(crn)
    mappaDetailsResponse(crn, category = 1, level = 1)
    allOffenderDetailsResponse(crn)
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
  fun `create recommendation when multiple active custodial convictions present`() {
    val staffCode = "STFFCDEU"
    licenceConditionsResponse(crn, 2500614567)
    licenceConditionsResponse(crn, 123456789)
    multipleConvictionResponse(crn, staffCode)
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    mappaDetailsResponse(crn, category = 1, level = 1)
    allOffenderDetailsResponse(crn)
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
    convictionResponse(crn, "011", offenceCode = "not a match")
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    mappaDetailsResponse(crn, category = 1, level = 1)
    allOffenderDetailsResponse(crn)
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
    convictionResponse(crn, "011")
    oasysAssessmentsResponse(crn, laterCompleteAssessmentExists = true)
    userAccessAllowed(crn)
    mappaDetailsResponse(crn, category = 1, level = 1)
    allOffenderDetailsResponse(crn)
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
  fun `update and get recommendation`() {
    mappaDetailsResponse(crn)
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    convictionResponse(crn, "011")
    licenceConditionsResponse(crn, 2500614567)
    deleteAndCreateRecommendation()
    updateRecommendation(updateRecommendationRequest())
    updateRecommendation(secondUpdateRecommendationRequest())

    webTestClient.get()
      .uri("/recommendations/$createdRecommendationId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(createdRecommendationId)
      .jsonPath("$.crn").isEqualTo(crn)
      .jsonPath("$.status").isEqualTo("DRAFT")
      .jsonPath("$.recallType.selected.value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.allOptions[1].value").isEqualTo("STANDARD")
      .jsonPath("$.recallType.allOptions[1].text").isEqualTo("Standard")
      .jsonPath("$.recallType.allOptions[0].value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.allOptions[0].text").isEqualTo("Fixed term")
      .jsonPath("$.recallType.allOptions[2].value").isEqualTo("NO_RECALL")
      .jsonPath("$.recallType.allOptions[2].text").isEqualTo("No recall")
      .jsonPath("$.custodyStatus.selected").isEqualTo("YES_PRISON")
      .jsonPath("$.custodyStatus.details").isEqualTo("Bromsgrove Police Station\r\nLondon")
      .jsonPath("$.custodyStatus.allOptions[0].value").isEqualTo("YES_PRISON")
      .jsonPath("$.custodyStatus.allOptions[0].text").isEqualTo("Yes, prison custody")
      .jsonPath("$.custodyStatus.allOptions[1].value").isEqualTo("YES_POLICE")
      .jsonPath("$.custodyStatus.allOptions[1].text").isEqualTo("Yes, police custody")
      .jsonPath("$.custodyStatus.allOptions[2].value").isEqualTo("NO")
      .jsonPath("$.custodyStatus.allOptions[2].text").isEqualTo("No")
      .jsonPath("$.responseToProbation").isEqualTo("They have not responded well")
      .jsonPath("$.whatLedToRecall").isEqualTo("Increasingly violent behaviour")
      .jsonPath("$.isThisAnEmergencyRecall").isEqualTo(true)
      .jsonPath("$.isExtendedSentence").isEqualTo(true)
      .jsonPath("$.isIndeterminateSentence").isEqualTo(true)
      .jsonPath("$.activeCustodialConvictionCount").isEqualTo(1)
      .jsonPath("$.hasVictimsInContactScheme.selected").isEqualTo("YES")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].value").isEqualTo("YES")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].text").isEqualTo("Yes")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].value").isEqualTo("NO")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].text").isEqualTo("No")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].value").isEqualTo("NOT_APPLICABLE")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].text").isEqualTo("N/A")
      .jsonPath("$.dateVloInformed").isEqualTo("2022-08-01")
      .jsonPath("$.personOnProbation.name").isEqualTo("John Smith")
      .jsonPath("$.personOnProbation.gender").isEqualTo("Male")
      .jsonPath("$.alternativesToRecallTried.selected[0].value").isEqualTo("WARNINGS_LETTER")
      .jsonPath("$.alternativesToRecallTried.selected[0].details").isEqualTo("We sent a warning letter on 27th July 2022")
      .jsonPath("$.alternativesToRecallTried.selected[1].value").isEqualTo("DRUG_TESTING")
      .jsonPath("$.alternativesToRecallTried.selected[1].details").isEqualTo("Drug test passed")
      .jsonPath("$.alternativesToRecallTried.allOptions[0].value").isEqualTo("WARNINGS_LETTER")
      .jsonPath("$.alternativesToRecallTried.allOptions[0].text").isEqualTo("Warnings/licence breach letters")
      .jsonPath("$.alternativesToRecallTried.allOptions[1].value").isEqualTo("DRUG_TESTING")
      .jsonPath("$.alternativesToRecallTried.allOptions[1].text").isEqualTo("Drug testing")
      .jsonPath("$.hasArrestIssues.selected").isEqualTo(true)
      .jsonPath("$.hasArrestIssues.details").isEqualTo("Violent behaviour")
      .jsonPath("$.hasContrabandRisk.selected").isEqualTo(true)
      .jsonPath("$.hasContrabandRisk.details").isEqualTo("Contraband risk details")
      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.selected[0]").isEqualTo("GOOD_BEHAVIOUR")
      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.selected[1]").isEqualTo("NO_OFFENCE")
      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[0].text").isEqualTo("Be of good behaviour")
      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[0].value").isEqualTo("GOOD_BEHAVIOUR")
      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[1].text").isEqualTo("Not to commit any offence")
      .jsonPath("$.licenceConditionsBreached.standardLicenceConditions.allOptions[1].value").isEqualTo("NO_OFFENCE")
      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.selected[0]").isEqualTo("NST14")
      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].title").isEqualTo("Disclosure of information")
      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].details").isEqualTo("Notify your supervising officer of any intimate relationships")
      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].note").isEqualTo("Persons wife is Joan Smyth")
      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].mainCatCode").isEqualTo("NLC5")
      .jsonPath("$.licenceConditionsBreached.additionalLicenceConditions.allOptions[0].subCatCode").isEqualTo("NST14")
      .jsonPath("$.isUnderIntegratedOffenderManagement.selected").isEqualTo("YES")
      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[0].text").isEqualTo("Yes")
      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[0].value").isEqualTo("YES")
      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[1].text").isEqualTo("No")
      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[1].value").isEqualTo("NO")
      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[2].text").isEqualTo("N/A")
      .jsonPath("$.isUnderIntegratedOffenderManagement.allOptions[2].value").isEqualTo("NOT_APPLICABLE")
      .jsonPath("$.localPoliceContact.contactName").isEqualTo("Thomas Magnum")
      .jsonPath("$.localPoliceContact.phoneNumber").isEqualTo("555-0100")
      .jsonPath("$.localPoliceContact.faxNumber").isEqualTo("555-0199")
      .jsonPath("$.localPoliceContact.emailAddress").isEqualTo("thomas.magnum@gmail.com")
      .jsonPath("$.vulnerabilities.selected[0].value").isEqualTo("RISK_OF_SUICIDE_OR_SELF_HARM")
      .jsonPath("$.vulnerabilities.selected[0].details").isEqualTo("Risk of suicide")
      .jsonPath("$.vulnerabilities.selected[1].value").isEqualTo("RELATIONSHIP_BREAKDOWN")
      .jsonPath("$.vulnerabilities.selected[1].details").isEqualTo("Divorced")
      .jsonPath("$.vulnerabilities.allOptions[0].text").isEqualTo("Risk of suicide or self harm")
      .jsonPath("$.vulnerabilities.allOptions[0].value").isEqualTo("RISK_OF_SUICIDE_OR_SELF_HARM")
      .jsonPath("$.vulnerabilities.allOptions[1].text").isEqualTo("Relationship breakdown")
      .jsonPath("$.vulnerabilities.allOptions[1].value").isEqualTo("RELATIONSHIP_BREAKDOWN")
      .jsonPath("$.convictionDetail.indexOffenceDescription").isEqualTo("Robbery (other than armed robbery)")
      .jsonPath("$.convictionDetail.dateOfOriginalOffence").isEqualTo("2022-04-24")
      .jsonPath("$.convictionDetail.dateOfSentence").isEqualTo("2022-04-26")
      .jsonPath("$.convictionDetail.lengthOfSentence").isEqualTo("12")
      .jsonPath("$.convictionDetail.lengthOfSentenceUnits").isEqualTo("days")
      .jsonPath("$.convictionDetail.sentenceDescription").isEqualTo("Extended Determinate Sentence")
      .jsonPath("$.convictionDetail.licenceExpiryDate").isEqualTo("2020-06-25")
      .jsonPath("$.convictionDetail.sentenceExpiryDate").isEqualTo("2020-06-28")
      .jsonPath("$.convictionDetail.sentenceSecondLength").isEqualTo("19")
      .jsonPath("$.convictionDetail.sentenceSecondLengthUnits").isEqualTo("days")
      .jsonPath("$.region").isEqualTo("NPS North West")
      .jsonPath("$.localDeliveryUnit").isEqualTo("Local delivery unit description 2")
      .jsonPath("$.fixedTermAdditionalLicenceConditions.selected").isEqualTo(true)
      .jsonPath("$.fixedTermAdditionalLicenceConditions.details").isEqualTo("This is an additional licence condition")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[0].details").isEqualTo("Some behaviour similar to index offence")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[0].value").isEqualTo("BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[1].details").isEqualTo("Behaviour leading to sexual or violent behaviour")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[1].value").isEqualTo("BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[2].details").isEqualTo("Out of touch")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.selected[2].value").isEqualTo("OUT_OF_TOUCH")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[0].text").isEqualTo("Some behaviour similar to index offence")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[0].value").isEqualTo("BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[1].text").isEqualTo("Behaviour leading to sexual or violent behaviour")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[1].value").isEqualTo("BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[2].text").isEqualTo("Out of touch")
      .jsonPath("$.indeterminateOrExtendedSentenceDetails.allOptions[2].value").isEqualTo("OUT_OF_TOUCH")
      .jsonPath("$.isMainAddressWherePersonCanBeFound.selected").isEqualTo(false)
      .jsonPath("$.isMainAddressWherePersonCanBeFound.details").isEqualTo("123 Acacia Avenue, Birmingham, B23 1AV")
      .jsonPath("$.whyConsideredRecall.selected").isEqualTo("RISK_INCREASED")
      .jsonPath("$.whyConsideredRecall.allOptions[0].value").isEqualTo("RISK_INCREASED")
      .jsonPath("$.whyConsideredRecall.allOptions[0].text").isEqualTo("Your risk is assessed as increased")
      .jsonPath("$.whyConsideredRecall.allOptions[1].value").isEqualTo("CONTACT_STOPPED")
      .jsonPath("$.whyConsideredRecall.allOptions[1].text").isEqualTo("Contact with your probation practitioner has broken down")
      .jsonPath("$.whyConsideredRecall.allOptions[2].value").isEqualTo("RISK_INCREASED_AND_CONTACT_STOPPED")
      .jsonPath("$.whyConsideredRecall.allOptions[2].text").isEqualTo("Your risk is assessed as increased and contact with your probation practitioner has broken down")
      .jsonPath("$.reasonsForNoRecall.licenceBreach").isEqualTo("Reason for breaching licence")
      .jsonPath("$.reasonsForNoRecall.noRecallRationale").isEqualTo("Rationale for no recall")
      .jsonPath("$.reasonsForNoRecall.popProgressMade").isEqualTo("Progress made so far detail")
      .jsonPath("$.reasonsForNoRecall.futureExpectations").isEqualTo("Future expectations detail")

    val result = repository.findByCrnAndStatus(crn, Status.DRAFT.name)
    assertThat(result[0].data.lastModifiedBy, equalTo("SOME_USER"))
  }

  @Test
  fun `given an update that clears hidden fields then save in database and get recommendation with null values for hidden fields`() {
    mappaDetailsResponse(crn)
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
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

    val result = repository.findByCrnAndStatus(crn, Status.DRAFT.name)
    assertThat(result[0].data.lastModifiedBy, equalTo("SOME_USER"))
  }

  private fun updateRecommendation(recommendationRequest: String) {
    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(recommendationRequest)
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `generate a DNTR document from recommendation data`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    convictionResponse(crn, "011")
    licenceConditionsResponse(crn, 2500614567)
    deleteAndCreateRecommendation()
    updateRecommendation(updateRecommendationForNoRecallRequest())

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations/$createdRecommendationId/no-recall-letter")
        .contentType(MediaType.APPLICATION_JSON)
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    assertThat(response.get("fileName")).isEqualTo("No_Recall" + nowDate() + "_Smith_J_A12345.docx")
    assertNotNull(response.get("fileContents"))

    val result = repository.findByCrnAndStatus(crn, Status.DRAFT.name)
    assertThat(result[0].data.userNameDntrLetterCompletedBy, equalTo("some_user"))
    assertNotNull(result[0].data.lastDntrLetterADownloadDateTime)
  }

  @Test
  fun `generate a Part A from recommendation data`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    convictionResponse(crn, "011")
    licenceConditionsResponse(crn, 2500614567)
    deleteAndCreateRecommendation()
    updateRecommendation(updateRecommendationRequest())

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations/$createdRecommendationId/part-a")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(createPartARequest())
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    assertThat(response.get("fileName")).isEqualTo("NAT_Recall_Part_A_" + nowDate() + "_Smith_J_A12345.docx")
    assertNotNull(response.get("fileContents"))

    val result = repository.findByCrnAndStatus(crn, Status.DRAFT.name)
    assertThat(result[0].data.userNamePartACompletedBy, equalTo("some_user"))
    assertThat(result[0].data.userEmailPartACompletedBy, equalTo("some.user@email.com"))
    assertNotNull(result[0].data.lastPartADownloadDateTime)
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
      allOffenderDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      mappaDetailsResponse(crn)
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
      mappaDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      allOffenderDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      mappaDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      deleteAndCreateRecommendation()
      userAccessAllowedOnce(crn)
      getRecommendation()
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
      mappaDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      allOffenderDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      mappaDetailsResponse(crn)
      userAccessAllowedOnce(crn)
      deleteAndCreateRecommendation()
      userAccessAllowedOnce(crn)
      updateRecommendation(updateRecommendationRequest())
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
