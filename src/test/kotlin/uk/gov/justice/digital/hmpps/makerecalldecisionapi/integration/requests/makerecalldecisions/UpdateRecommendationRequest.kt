package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

fun updateRecommendationRequest(status: Status = Status.DRAFT) = """
{
	"custodyStatus": {
		"selected": "YES_PRISON",
		"allOptions": [{
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
		"allOptions": [{
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
	"isThisAnEmergencyRecall": true,
	"hasVictimsInContactScheme": {
		"selected": "YES",
		"allOptions": [{
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
	"dateVloInformed": "2022-08-01",
	"status": "$status",
	"alternativesToRecallTried": {
		"selected": [{
				"value": "WARNINGS_LETTER",
				"details": "We sent a warning letter on 27th July 2022"
			},
			{
				"value": "DRUG_TESTING",
				"details": "Drug test passed"
			}
		],
		"allOptions": [{
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
	"licenceConditionsBreached": {
		"standardLicenceConditions": {
			"selected": [
				"GOOD_BEHAVIOUR",
				"NO_OFFENCE"
			],
			"allOptions": [{
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
			"allOptions": [{
				"mainCatCode": "NLC5",
				"subCatCode": "NST14",
				"title": "Disclosure of information",
				"details": "Notify your supervising officer of any intimate relationships",
				"note": "Persons wife is Joan Smyth"
			}]
		}
	},
	"isUnderIntegratedOffenderManagement": {
		"selected": "YES",
		"allOptions": [{
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
	}
}
""".trimIndent()
