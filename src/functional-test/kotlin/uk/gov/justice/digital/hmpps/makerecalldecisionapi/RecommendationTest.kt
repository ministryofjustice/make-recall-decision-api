package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

class RecommendationTest() : FunctionalTest() {

  @Test
  fun `make a recommendation, expected 201`() {
    // given
    // nothing to do here!!

    // when
    lastResponse = createRecommendation(token, testCrn)

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedCreated)
    val actualResponse = JSONObject(lastResponse.asString()).toString()
    assertResponse(lastResponse, "")
  }

  @Test
  fun `update a recommendation, expected 200`() {
    // given
    lastResponse = createRecommendation(token, testCrn)

    // and
    val recommendationId = JSONObject(lastResponse.asString()).getString("id")

    // when
    updateRecommendation(recommendationId, token)

    // then
    lastResponse = getRecommendation(recommendationId, token)
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)

    assertResponse(lastResponse, "")
  }

  @Test
  fun `generate a part a document, expected 200`() {
    // given
    lastResponse = createRecommendation(token, testCrn)

    // and
    val recommendationId = JSONObject(lastResponse.asString()).getString("id")

    // and
    updateRecommendation(recommendationId, token)

    // when
    lastResponse = RestAssured
      .given()
      .contentType(APPLICATION_JSON_VALUE)
      .header("Authorization", token)
      .body(createPartARequest())
      .post("recommendations/$recommendationId/part-a")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
    assertResponse(lastResponse, "")
  }

  @Test
  fun `generate DNTR document preview, expected 200`() {
    // given
    lastResponse = createRecommendation(token, testCrn)

    // and
    val recommendationId = JSONObject(lastResponse.asString()).getString("id")

    // and
    updateRecommendation(recommendationId, token)

    // when
    lastResponse = RestAssured
      .given()
      .contentType(APPLICATION_JSON_VALUE)
      .header("Authorization", token)
      .body(documentRequestQuery("preview"))
      .post("recommendations/$recommendationId/no-recall-letter")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
    assertResponse(lastResponse, "")
  }

  @Test
  fun `generate DNTR document download, expected 200`() {
    // given
    lastResponse = createRecommendation(token, testCrn)

    // and
    val recommendationId = JSONObject(lastResponse.asString()).getString("id")

    // and
    updateRecommendation(recommendationId, token)

    // when
    lastResponse = RestAssured
      .given()
      .contentType(APPLICATION_JSON_VALUE)
      .header("Authorization", token)
      .body(documentRequestQuery("download-docx"))
      .post("recommendations/$recommendationId/no-recall-letter")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
    assertResponse(lastResponse, "")
  }
}
private fun updateRecommendation(recommendationId: String?, token: String) {
  RestAssured
    .given()
    .contentType(APPLICATION_JSON_VALUE)
    .header("Authorization", token)
    .body(updateRecommendationRequest())
    .patch("recommendations/$recommendationId")
}

private fun createRecommendation(token: String, testCrn: String) = RestAssured
  .given()
  .contentType(APPLICATION_JSON_VALUE)
  .header("Authorization", token)
  .body(recommendationRequest("$testCrn"))
  .post("${FunctionalTest.path}/recommendations")
private fun getRecommendation(recommendationId: Any?, token: String) = RestAssured
  .given()
  .contentType(APPLICATION_JSON_VALUE)
  .header("Authorization", token)
  .body(updateRecommendationRequest())
  .get("recommendations/$recommendationId")
private fun documentRequestQuery(documentRequestQuery: String) = """
  {
    "format": "$documentRequestQuery"
  }
""".trimIndent()
private fun createPartARequest() = """
  {
    "userEmail": "some.user@email.com"
  }
""".trimIndent()

private fun recommendationRequest(crn: String) = """
  {
    "crn": "$crn"
  }
""".trimIndent()

private fun updateRecommendationRequest() = """
  {
    "custodyStatus": {
      "selected": "YES_PRISON",
      "details": "Bromsgrove Police Station\r\nLondon",
      "allOptions": [
        {
          "value": "YES_PRISON",
          "text": "Yes, prison custody"
        },
        {
          "value": "YES_POLICE",
          "text": "Yes, police custody"
        },
        {
          "value": "NO",
          "text": "No"
        }
      ]
    },
    "recallType": {
      "selected": {
        "value": "FIXED_TERM",
        "details": "My details"
      },
      "allOptions": [
        {
          "value": "FIXED_TERM",
          "text": "Fixed term"
        },
        {
          "value": "STANDARD",
          "text": "Standard"
        },
        {
          "value": "NO_RECALL",
          "text": "No recall"
        }
      ]
    },
    "responseToProbation": "They have not responded well",
    "whatLedToRecall": "Increasingly violent behaviour",
    "isThisAnEmergencyRecall": true,
    "isIndeterminateSentence": true,
    "isExtendedSentence": true,
    "activeCustodialConvictionCount": 1,
    "hasVictimsInContactScheme": {
      "selected": "YES",
      "allOptions": [
        {
          "value": "YES",
          "text": "Yes"
        },
        {
          "value": "NO",
          "text": "No"
        },
        {
          "value": "NOT_APPLICABLE",
          "text": "N/A"
        }
      ]
    },
    "indeterminateSentenceType": {
      "selected": "LIFE",
      "allOptions": [
        {
          "value": "LIFE",
          "text": "Life sentence"
        },
        {
          "value": "IPP",
          "text": "Imprisonment for Public Protection (IPP) sentence"
        },
        {
          "value": "DPP",
          "text": "Detention for Public Protection (DPP) sentence"
        },
        {
          "value": "NO",
          "text": "No"
        }
      ]
    },
    "dateVloInformed": "2022-08-01",
    "status": "DRAFT",
    "alternativesToRecallTried": {
      "selected": [
        {
          "value": "WARNINGS_LETTER",
          "details": "We sent a warning letter on 27th July 2022"
        },
        {
          "value": "DRUG_TESTING",
          "details": "Drug test passed"
        }
      ],
      "allOptions": [
        {
          "value": "WARNINGS_LETTER",
          "text": "Warnings/licence breach letters"
        },
        {
          "value": "DRUG_TESTING",
          "text": "Drug testing"
        }
      ]
    },
    "hasArrestIssues": {
      "selected": true,
      "details": "Violent behaviour"
    },
    "hasContrabandRisk": {
      "selected": true,
      "details": "Contraband risk details"
    },
    "licenceConditionsBreached": {
      "standardLicenceConditions": {
        "selected": [
          "GOOD_BEHAVIOUR",
          "NO_OFFENCE"
        ],
        "allOptions": [
          {
            "value": "GOOD_BEHAVIOUR",
            "text": "Be of good behaviour"
          },
          {
            "value": "NO_OFFENCE",
            "text": "Not to commit any offence"
          },
          {
            "value": "NAME_CHANGE",
            "text": "Tell your supervising officer if you use a name which is different to the name or names which appear on your licence."        
          },
          {
            "value": "CONTACT_DETAILS",
            "text": "Tell your supervising officer if you change or add any contact details, including phone number or email."        
          }        
        ]
      },
      "additionalLicenceConditions": {
        "selected": [
          "NST14"
        ],
        "allOptions": [
          {
            "mainCatCode": "NLC5",
            "subCatCode": "NST14",
            "title": "Disclosure of information",
            "details": "Notify your supervising officer of any intimate relationships",
            "note": "Persons wife is Joan Smyth"
          }
        ]
      }
    },
    "isUnderIntegratedOffenderManagement": {
      "selected": "YES",
      "allOptions": [
        {
          "value": "YES",
          "text": "Yes"
        },
        {
          "value": "NO",
          "text": "No"
        },
        {
          "value": "NOT_APPLICABLE",
          "text": "N/A"
        }
      ]
    },
    "localPoliceContact": {
      "contactName": "Thomas Magnum",
      "phoneNumber": "555-0100",
      "faxNumber": "555-0199",
      "emailAddress": "thomas.magnum@gmail.com"
    },
    "vulnerabilities": {
      "selected": [
        {
          "value": "RISK_OF_SUICIDE_OR_SELF_HARM",
          "details": "Risk of suicide"
        },
        {
          "value": "RELATIONSHIP_BREAKDOWN",
          "details": "Divorced"
        }
      ],
      "allOptions": [
        {
          "value": "RISK_OF_SUICIDE_OR_SELF_HARM",
          "text": "Risk of suicide or self harm"
        },
        {
          "value": "RELATIONSHIP_BREAKDOWN",
          "text": "Relationship breakdown"
        }
      ]
    },
    "fixedTermAdditionalLicenceConditions": {
      "selected": true,
      "details": "This is an additional licence condition"
    },
    "indeterminateOrExtendedSentenceDetails": {
      "selected": [
        {
          "value": "BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE",
          "details": "Some behaviour similar to index offence"
        },
        {
          "value": "BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE",
          "details": "Behaviour leading to sexual or violent behaviour"
        },
        {
          "value": "OUT_OF_TOUCH",
          "details": "Out of touch"
        }
      ],
      "allOptions": [
        {
          "text": "Some behaviour similar to index offence",
          "value": "BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE"
        },
        {
          "text": "Behaviour leading to sexual or violent behaviour",
          "value": "BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE"
        },
        {
          "text": "Out of touch",
          "value": "OUT_OF_TOUCH"
        }
      ]
    },
    "isMainAddressWherePersonCanBeFound": {
      "selected": false,
      "details": "123 Acacia Avenue, Birmingham, B23 1AV"
    },
    "whyConsideredRecall": {
      "selected": "RISK_INCREASED",
      "allOptions": [
        {
          "value": "RISK_INCREASED",
          "text": "Your risk is assessed as increased"
        },
        {
          "value": "CONTACT_STOPPED",
          "text": "Contact with your probation practitioner has broken down"
        },
        {
          "value": "RISK_INCREASED_AND_CONTACT_STOPPED",
          "text": "Your risk is assessed as increased and contact with your probation practitioner has broken down"
        }
      ]
    },
    "reasonsForNoRecall": {
      "licenceBreach": "Reason for breaching licence",
      "noRecallRationale": "Rationale for no recall",
      "popProgressMade": "Progress made so far detail",
      "futureExpectations": "Future expectations detail"
    },
    "nextAppointment": {
      "howWillAppointmentHappen": {
        "selected": "TELEPHONE",
        "allOptions": [
          {
            "value": "TELEPHONE",
            "text": "Telephone"
          },
          {
            "value": "VIDEO_CALL",
            "text": "Video call"
          },
          {
            "value": "OFFICE_VISIT",
            "text": "Office visit"
          },
          {
            "value": "HOME_VISIT",
            "text": "Home visit"
          }
        ]
      },
      "dateTimeOfAppointment": "2022-04-24T20:39:00.000Z",
      "probationPhoneNumber": "01238282838"
    }
  }
""".trimIndent()
