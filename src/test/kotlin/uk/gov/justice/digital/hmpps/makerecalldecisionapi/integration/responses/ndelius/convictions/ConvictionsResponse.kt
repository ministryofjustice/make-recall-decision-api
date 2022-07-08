package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.convictions

fun convictionsResponse(crn: String, staffCode: String) = """
 [
   {
     "active": true,
     "awaitingPsr": true,
     "breachEnd": "2021-05-13",
     "convictionDate": "2021-06-10",
     "convictionId": 2500614567,
     "courtAppearance": {
       "appearanceDate": "2019-09-04T00:00:00",
       "appearanceType": {
         "code": "ABC123",
         "description": "Some description"
       },
       "courtAppearanceId": 2500000001,
       "courtCode": "SHEFMC",
       "courtName": "Sheffield Magistrates Court",
       "crn": "$crn"
     },
     "custody": {
       "bookingNumber": "V74111",
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
       "keyDates": {
         "conditionalReleaseDate": "2020-06-20",
         "expectedPrisonOffenderManagerHandoverDate": "2020-06-21",
         "expectedPrisonOffenderManagerHandoverStartDate": "2020-06-22",
         "expectedReleaseDate": "2020-06-23",
         "hdcEligibilityDate": "2020-06-24",
         "licenceExpiryDate": "2020-06-25",
         "paroleEligibilityDate": "2020-06-26",
         "postSentenceSupervisionEndDate": "2020-06-27",
         "sentenceExpiryDate": "2020-06-28"
       },
       "sentenceStartDate": "2022-04-26",
       "status": {
         "code": "ABC123",
         "description": "I am the custody status description"
       }
     },
     "failureToComplyCount": 3,
     "inBreach": true,
     "index": 1,
     "latestCourtAppearanceOutcome": {
       "code": "ABC123",
       "description": "Some description"
     },
     "offences": [
       {
         "createdDatetime": "2022-04-26T20:39:47.778Z",
         "detail": {
           "abbreviation": "string",
           "cjitCode": "string",
           "code": "1234",
           "description": "Robbery (other than armed robbery)",
           "form20Code": "string",
           "mainCategoryAbbreviation": "string",
           "mainCategoryCode": "string",
           "mainCategoryDescription": "string",
           "ogrsOffenceCategory": "string",
           "subCategoryAbbreviation": "string",
           "subCategoryCode": "string",
           "subCategoryDescription": "string"
         },
         "lastUpdatedDatetime": "2022-04-26T20:39:47.778Z",
         "mainOffence": true,
         "offenceCount": 0,
         "offenceDate": "2022-04-26T20:39:47.778Z",
         "offenceId": "string",
         "offenderId": 0,
         "tics": 0,
         "verdict": "string"
       }
     ],
     "orderManagers": [
       {
         "dateEndOfAllocation": "2022-04-26T20:39:47.778Z",
         "dateStartOfAllocation": "2022-04-26T20:39:47.778Z",
         "gradeCode": "string",
         "name": "string",
         "officerId": 0,
         "probationAreaId": 0,
         "staffCode": "$staffCode",
         "teamId": 0
       }
     ],
     "referralDate": "2021-06-10",
     "responsibleCourt": {
       "buildingName": "Sheffield Magistrates Court",
       "code": "SHEFMC",
       "country": "England",
       "county": "South Yorkshire",
       "courtId": 2500000001,
       "courtName": "Sheffield Magistrates Court",
       "courtType": {
         "code": "ABC123",
         "description": "Some description"
       },
       "courtTypeId": 310,
       "createdDatetime": "2014-05-29T21:50:16",
       "fax": "0114 2756 373",
       "lastUpdatedDatetime": "2014-05-29T21:50:16",
       "locality": "Sheffield City Centre",
       "postcode": "S3 8LU",
       "probationArea": {
         "code": "ABC123",
         "description": "Some description"
       },
       "probationAreaId": 1500001001,
       "secureEmailAddress": "example@example.com",
       "selectable": true,
       "street": "Castle Street",
       "telephoneNumber": "0300 047 0777",
       "town": "Sheffield"
     },
     "sentence": {
       "additionalSentences": [
         {
           "additionalSentenceId": 2500000001,
           "amount": 100,
           "length": 14,
           "notes": "Some additional sentence notes",
           "type": {
             "code": "ABC123",
             "description": "Some description"
           }
         }
       ],
       "cja2003Order": true,
       "defaultLength": 0,
       "description": "string",
       "effectiveLength": 0,
       "expectedSentenceEndDate": "2022-04-26",
       "failureToComplyLimit": 0,
       "legacyOrder": true,
       "lengthInDays": 0,
       "originalLength": 0,
       "originalLengthUnits": "string",
       "secondLength": 0,
       "secondLengthUnits": "string",
       "sentenceId": 0,
       "sentenceType": {
         "code": "ABC123",
         "description": "Some description"
       },
       "startDate": "2022-04-26",
       "terminationDate": "2022-04-26",
       "terminationReason": "string",
       "unpaidWork": {
         "appointments": {
           "acceptableAbsences": 0,
           "attended": 0,
           "noOutcomeRecorded": 0,
           "total": 0,
           "unacceptableAbsences": 0
         },
         "minutesCompleted": 0,
         "minutesOrdered": 0,
         "status": "string"
       }
     }
   }
 ]
""".trimIndent()
