package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.cvl

fun licenceIdResponse(licenceId: Int, nomisId: String, crn: String) = """
{
  "id": $licenceId,
  "typeCode": "AP",
  "version": "1.4",
  "statusCode": "IN_PROGRESS",
  "nomsId": "$nomisId",
  "bookingNo": "F12333",
  "bookingId": 989898,
  "crn": "$crn",
  "pnc": "2015/12444",
  "cro": "A/12444",
  "prisonCode": "LEI",
  "prisonDescription": "Leeds (HMP)",
  "prisonTelephone": "0161 234 4747",
  "forename": "Michael",
  "middleNames": "John Peter",
  "surname": "Smith",
  "dateOfBirth": "2022-10-19",
  "conditionalReleaseDate": "10/06/2022",
  "actualReleaseDate": "11/06/2022",
  "sentenceStartDate": "12/06/2022",
  "sentenceEndDate": "13/06/2022",
  "licenceStartDate": "14/06/2022",
  "licenceExpiryDate": "15/06/2022",
  "topupSupervisionStartDate": "16/06/2022",
  "topupSupervisionExpiryDate": "17/06/2022",
  "comUsername": "X32122",
  "comStaffId": 12345,
  "comEmail": "jane.jones@nps.gov.uk",
  "probationAreaCode": "N01",
  "probationAreaDescription": "Wales",
  "probationPduCode": "PDU01",
  "probationPduDescription": "North Wales",
  "probationLauCode": "LAU01",
  "probationLauDescription": "North Wales",
  "probationTeamCode": "Cardiff-A",
  "probationTeamDescription": "Cardiff South",
  "appointmentPerson": "Duty officer",
  "appointmentTime": "11/11/2027 10:30",
  "appointmentAddress": "Manchester Probation Service, Unit 4, Smith Street, Stockport, SP1 3DN",
  "appointmentContact": "0114 2557665",
  "spoDiscussion": "Yes",
  "vloDiscussion": "Yes",
  "approvedDate": "12/11/2027 10:30",
  "approvedByUsername": "X33221",
  "approvedByName": "John Smith",
  "supersededDate": "13/11/2027 10:30",
  "dateCreated": "24/10/2022 14:09:35",
  "createdByUsername": "X12333",
  "dateLastUpdated": "14/11/2027 10:30",
  "updatedByUsername": "X34433",
  "standardLicenceConditions": [
    {
      "id": 98987,
      "code": "9ce9d594-e346-4785-9642-c87e764bee37",
      "sequence": 1,
      "text": "This is a standard licence condition"
    }
  ],
  "standardPssConditions": [
    {
      "id": 98987,
      "code": "9ce9d594-e346-4785-9642-c87e764bee37",
      "sequence": 1,
      "text": "This is a standard PSS licence condition"
    }
  ],
  "additionalLicenceConditions": [
    {
      "id": 98989,
      "code": "meetingAddress",
      "category": "Freedom of movement",
      "sequence": 1,
      "text": "This is an additional licence condition",
      "expandedText": "Expanded additional licence condition",
      "data": [
        {
          "id": 98989,
          "field": "location",
          "value": "Norfolk",
          "sequence": 1
        }
      ],
      "uploadSummary": [
        {
          "id": 98989,
          "filename": "exclusion-zone.pdf",
          "fileType": "application/pdf",
          "fileSize": 27566,
          "uploadedTime": "13/10/2022 10:30",
          "description": "A description of the exclusion zone boundaries",
          "thumbnailImage": "Base64 string",
          "uploadDetailId": 9999
        }
      ]
    }
  ],
  "additionalPssConditions": [
    {
      "id": 98989,
      "code": "meetingAddress",
      "category": "Freedom of movement",
      "sequence": 1,
      "text": "This is an additional PSS licence condition",
      "expandedText": "Expanded additional PSS licence condition",
      "data": [
        {
          "id": 98989,
          "field": "location",
          "value": "Norfolk",
          "sequence": 1
        }
      ],
      "uploadSummary": [
        {
          "id": 98989,
          "filename": "exclusion-zone.pdf",
          "fileType": "application/pdf",
          "fileSize": 27566,
          "uploadedTime": "13/10/2022 10:30",
          "description": "A description of the exclusion zone boundaries",
          "thumbnailImage": "Base64 string",
          "uploadDetailId": 9999
        }
      ]
    }
  ],
  "bespokeConditions": [
    {
      "id": 98989,
      "sequence": 1,
      "text": "This is a bespoke condition"
    }
  ],
  "isVariation": true,
  "variationOf": 0,
  "createdByFullName": "Gordon Sumner"
}
""".trimIndent()
