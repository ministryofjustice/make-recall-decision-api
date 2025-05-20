package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

fun updateRecommendationForNoRecallRequest(status: Status = Status.DRAFT) = """
{
  "recallType": {
    "selected": {
      "value": "NO_RECALL",
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
  "isIndeterminateSentence": false,
  "isExtendedSentence": false,
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
    "selected": "NO",
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
          "note": "Persons wife is Jane Bloggs"
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
    "contactName": "John Doe",
    "phoneNumber": "01234567890",
    "faxNumber": "09876543210",
    "emailAddress": "john.doe@gmail.com"
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
  "reasonsForNoRecall": {
    "licenceBreach": "Reason for breaching licence",
    "noRecallRationale": "Rationale for no recall",
    "popProgressMade": "Progress made so far detail",
    "popThoughts": "Thoughts on bad behaviour",
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
    "dateTimeOfAppointment": "2022-04-24T08:39:00.000Z",
    "probationPhoneNumber": "01238282838"
  }
}
""".trimIndent()
