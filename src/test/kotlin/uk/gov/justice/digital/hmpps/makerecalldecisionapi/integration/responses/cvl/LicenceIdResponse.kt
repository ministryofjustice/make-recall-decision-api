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
  "conditionalReleaseDate": "2022-06-10",
  "actualReleaseDate": "2022-06-11",
  "sentenceStartDate": "2022-06-12",
  "sentenceEndDate": "2022-06-13",
  "licenceStartDate": "2022-06-14",
  "licenceExpiryDate": "2022-06-15",
  "topupSupervisionStartDate": "2022-06-16",
  "topupSupervisionExpiryDate": "2022-06-17",
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
  "appointmentTime": "2022-10-19T16:13:29.279Z",
  "appointmentAddress": "Manchester Probation Service, Unit 4, Smith Street, Stockport, SP1 3DN",
  "appointmentContact": "0114 2557665",
  "spoDiscussion": "Yes",
  "vloDiscussion": "Yes",
  "approvedDate": "2022-10-19T16:13:29.279Z",
  "approvedByUsername": "X33221",
  "approvedByName": "John Smith",
  "supersededDate": "2022-10-19T16:13:29.279Z",
  "dateCreated": "2022-10-19T16:13:29.279Z",
  "createdByUsername": "X12333",
  "dateLastUpdated": "2022-10-19T16:13:29.279Z",
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
          "uploadedTime": "2022-10-19T16:13:29.279Z",
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
          "uploadedTime": "2022-10-19T16:13:29.279Z",
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
