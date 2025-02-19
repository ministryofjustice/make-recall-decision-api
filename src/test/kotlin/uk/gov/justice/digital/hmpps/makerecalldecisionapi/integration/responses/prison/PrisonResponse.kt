package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison

fun prisonResponse(description: String, facialImageId: String, agencyId: String) = """
{
  "locationDescription": "$description",
  "bookingNo": "string",
  "facialImageId": $facialImageId,
  "firstName": "string",
  "middleName": "string",
  "lastName": "string",
  "dateOfBirth": "1970-03-15",
  "agencyId": "$agencyId",
  "status": "ACTIVE IN",
  "physicalAttributes": {
    "gender": "Male",
    "ethnicity": "White: Eng./Welsh/Scot./N.Irish/British"
  },
  "identifiers": [
    {
      "type": "CRO",
      "value": "123456/12A"
    },
    {
      "type": "PNC",
      "value": "76767675"
    }
  ],
  "sentenceDetail": {
    "licenceExpiryDate": "2020-02-03"
  }
}
""".trimIndent()
