package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

fun updateRecommendationRequest(status: Status = Status.DRAFT) = """
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
  "status": "$status",
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
    "behaviourSimilarToIndexOffence": "some behaviour similar to index offence",
    "behaviourLeadingToSexualOrViolentOffence": "behaviour leading to sexual or violent behaviour",
    "outOfTouch": "out of touch"
  }
}
""".trimIndent()
