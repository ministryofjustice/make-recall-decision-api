package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun allOffenderDetailsResponse(district: String? = "Sheffield City Centre", firstName: String? = "John") = """
{
  "activeProbationManagedSentence": true,
  "contactDetails": {
    "addresses": [
      {
        "addressNumber": 45,
        "buildingName": "Death Row Records",
        "county": "LA",
        "createdDatetime": "2021-06-11T13:00:00",
        "district": "South Central",
        "from": "2021-06-10",
        "lastUpdatedDatetime": "2021-06-11T14:00:00",
        "latestAssessmentDate": "2021-06-11T12:00:00",
        "noFixedAbode": false,
        "notes": "Some address notes",
        "postcode": "90220",
        "status": {
          "code": "ABC123",
          "description": "Not Main"
        },
        "streetName": "Skid Row",
        "telephoneNumber": "0123456789",
        "to": "2021-07-10",
        "town": "Compton",
        "type": {
          "code": "ABC123",
          "description": "Some description"
        },
        "typeVerified": true
      },
      {
        "addressNumber": 33,
        "buildingName": "HMPPS Digital Studio",
        "county": "South Yorkshire",
        "createdDatetime": "2021-06-11T13:00:00",
        "district": "$district",
        "from": "2020-06-10",
        "lastUpdatedDatetime": "2021-06-11T14:00:00",
        "latestAssessmentDate": "2021-06-11T12:00:00",
        "noFixedAbode": false,
        "notes": "Some address notes",
        "postcode": "S3 7BS",
        "status": {
          "code": "ABC123",
          "description": "Main"
        },
        "streetName": "Scotland Street",
        "telephoneNumber": "0123456789",
        "to": "2020-07-10",
        "town": "Sheffield",
        "type": {
          "code": "ABC123",
          "description": "Some description"
        },
        "typeVerified": true
      }
    ],
    "allowSMS": true,
    "emailAddresses": [
      "string"
    ],
    "phoneNumbers": [
      {
        "number": "string",
        "type": "MOBILE"
      }
    ]
  },
  "currentDisposal": 1,
  "currentExclusion": true,
  "currentRestriction": true,
  "currentTier": "D2",
  "dateOfBirth": "1982-10-24",
  "exclusionMessage": "string",
  "firstName": "$firstName",
  "gender": "Male",
  "middleNames": [
    "Homer",
    "Bart"
  ],
  "offenderAliases": [
    {
      "dateOfBirth": "2022-04-26",
      "firstName": "string",
      "gender": "string",
      "id": "string",
      "middleNames": [
        "string"
      ],
      "surname": "string"
    }
  ],
  "offenderId": 0,
  "offenderManagers": [
    {
      "active": true,
      "allocationReason": {
        "code": "ABC123",
        "description": "Some description"
      },
      "fromDate": "2022-04-26",
      "partitionArea": "string",
      "probationArea": {
        "code": "N01",
        "description": "NPS North West",
        "institution": {
          "code": "string",
          "description": "string",
          "establishmentType": {
            "code": "ABC123",
            "description": "Local delivery unit description"
          },
          "institutionId": 0,
          "institutionName": "string",
          "isEstablishment": true,
          "isPrivate": true,
          "nomsPrisonInstitutionCode": "string"
        },
        "nps": true,
        "organisation": {
          "code": "ABC123",
          "description": "Some description"
        },
        "probationAreaId": 0,
        "teams": [
          {
            "borough": {
              "code": "ABC123",
              "description": "Some description"
            },
            "code": "string",
            "description": "string",
            "district": {
              "code": "ABC123",
              "description": "Some description"
            },
            "externalProvider": {
              "code": "ABC123",
              "description": "Some description"
            },
            "isPrivate": true,
            "localDeliveryUnit": {
              "code": "ABC123",
              "description": "Local delivery unit description"
            },
            "name": "string",
            "providerTeamId": 0,
            "scProvider": {
              "code": "ABC123",
              "description": "Some description"
            },
            "teamId": 0
          }
        ]
      },
      "providerEmployee": {
        "forenames": "Sheila Linda",
        "surname": "Hancock"
      },
      "softDeleted": true,
      "staff": {
        "code": "AN001A",
        "forenames": "Sheila Linda",
        "surname": "Hancock",
        "unallocated": true
      },
      "team": {
        "borough": {
          "code": "ABC123",
          "description": "Some description"
        },
        "code": "C01T04",
        "description": "OMU A",
        "district": {
          "code": "ABC123",
          "description": "Some description"
        },
        "emailAddress": "first.last@digital.justice.gov.uk",
        "endDate": "2022-04-26",
        "localDeliveryUnit": {
          "code": "ABC123",
          "description": "Local delivery unit description 2"
        },
        "startDate": "2022-04-26",
        "teamType": {
          "code": "ABC123",
          "description": "Some description"
        },
        "telephone": "09056714321"
      },
      "toDate": "2022-04-26",
      "trustOfficer": {
        "forenames": "Sheila Linda",
        "surname": "Hancock"
      }
    },
    {
      "active": false,
      "allocationReason": {
        "code": "ABC123",
        "description": "Some description"
      },
      "fromDate": "2022-04-26",
      "partitionArea": "string",
      "probationArea": {
        "code": "N01",
        "description": "NPS North West",
        "institution": {
          "code": "string",
          "description": "string",
          "establishmentType": {
            "code": "ABC123",
            "description": "Some description"
          },
          "institutionId": 0,
          "institutionName": "string",
          "isEstablishment": true,
          "isPrivate": true,
          "nomsPrisonInstitutionCode": "string"
        },
        "nps": true,
        "organisation": {
          "code": "ABC123",
          "description": "Some description"
        },
        "probationAreaId": 0,
        "teams": [
          {
            "borough": {
              "code": "ABC123",
              "description": "Some description"
            },
            "code": "string",
            "description": "string",
            "district": {
              "code": "ABC123",
              "description": "Some description"
            },
            "externalProvider": {
              "code": "ABC123",
              "description": "Some description"
            },
            "isPrivate": true,
            "localDeliveryUnit": {
              "code": "ABC123",
              "description": "Local delivery unit description 3"
            },
            "name": "string",
            "providerTeamId": 0,
            "scProvider": {
              "code": "ABC123",
              "description": "Some description"
            },
            "teamId": 0
          }
        ]
      },
      "providerEmployee": {
        "forenames": "Sheila Linda",
        "surname": "Hancock"
      },
      "softDeleted": true,
      "staff": {
        "code": "AN001A",
        "forenames": "Sheila Linda",
        "surname": "Hancock",
        "unallocated": true
      },
      "team": {
        "borough": {
          "code": "ABC123",
          "description": "Some description"
        },
        "code": "C01T04",
        "description": "OMU A",
        "district": {
          "code": "ABC123",
          "description": "Some description"
        },
        "emailAddress": "dua.lipa@digital.justice.gov.uk",
        "endDate": "2022-04-26",
        "localDeliveryUnit": {
          "code": "ABC123",
          "description": "Local delivery unit description 4"
        },
        "startDate": "2022-04-26",
        "teamType": {
          "code": "ABC123",
          "description": "Some description"
        },
        "telephone": "123"
      },
      "toDate": "2022-04-26",
      "trustOfficer": {
        "forenames": "Dua",
        "surname": "Lipa"
      }
    }
  ],
  "offenderProfile": {
    "disabilities": [
      {
        "disabilityId": 0,
        "disabilityType": {
          "code": "ABC123",
          "description": "Some description"
        },
        "endDate": "2022-04-26",
        "isActive": true,
        "lastUpdatedDateTime": "2020-09-20T11:00:00+01:00",
        "notes": "string",
        "provisions": [
          {
            "finishDate": "2022-04-26",
            "notes": "string",
            "provisionId": 0,
            "provisionType": {
              "code": "ABC123",
              "description": "Some description"
            },
            "startDate": "2022-04-26"
          }
        ],
        "startDate": "2022-04-26"
      }
    ],
    "ethnicity": "Ainu",
    "genderIdentity": "Prefer to self-describe",
    "immigrationStatus": "string",
    "nationality": "string",
    "notes": "string",
    "offenderDetails": "string",
    "offenderLanguages": {
      "languageConcerns": "string",
      "otherLanguages": [
        "string"
      ],
      "primaryLanguage": "English",
      "requiresInterpreter": true
    },
    "previousConviction": {
      "convictionDate": "2022-04-26",
      "detail": {
        "additionalProp1": "string",
        "additionalProp2": "string",
        "additionalProp3": "string"
      }
    },
    "religion": "string",
    "remandStatus": "string",
    "riskColour": "string",
    "secondaryNationality": "string",
    "selfDescribedGender": "Jedi",
    "sexualOrientation": "string"
  },
  "otherIds": {
    "crn": "12345C",
    "croNumber": "123456/04A",
    "immigrationNumber": "A1234567",
    "mostRecentPrisonerNumber": "G12345",
    "niNumber": "AA112233B",
    "nomsNumber": "A1234CR",
    "pncNumber": "2004/0712343H"
  },
  "partitionArea": "National Data",
  "preferredName": "Bob",
  "previousSurname": "Davis",
  "restrictionMessage": "string",
  "softDeleted": true,
  "surname": "Smith",
  "title": "Mr"
}
""".trimIndent()
