package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison

fun prisonTimelineResponse(prisonerNumber: String) = """
{
  "prisonerNumber": "$prisonerNumber",
  "prisonPeriod": [
    {
      "bookNumber": "47359A",
      "bookingId": 12,
      "entryDate": "2020-09-23T15:55:00",
      "releaseDate": "2022-11-10T16:52:30",
      "movementDates": [
        {
          "reasonInToPrison": "Imprisonment Without Option",
          "dateInToPrison": "2020-09-23T15:55:00",
          "inwardType": "ADM",
          "reasonOutOfPrison": "Conditional Release (CJA91) -SH Term>1YR",
          "dateOutOfPrison": "2022-11-10T16:52:30",
          "outwardType": "REL",
          "admittedIntoPrisonId": "MDI",
          "releaseFromPrisonId": "MDI"
        }
      ],
      "prisons": [
        "MDI"
      ]
    },
    {
      "bookNumber": "47364A",
      "bookingId": 13,
      "entryDate": "2023-11-10T16:56:34",
      "movementDates": [
        {
          "reasonInToPrison": "Imprisonment Without Option",
          "dateInToPrison": "2023-11-10T16:56:34",
          "inwardType": "ADM",
          "reasonOutOfPrison": "Post Recall Release",
          "dateOutOfPrison": "2023-11-20T11:09:09",
          "outwardType": "REL",
          "admittedIntoPrisonId": "MDI",
          "releaseFromPrisonId": "MDI"
        },
        {
          "reasonInToPrison": "Recall From Intermittent Custody",
          "dateInToPrison": "2023-11-20T11:10:29",
          "inwardType": "ADM",
          "admittedIntoPrisonId": "MDI"
        }
      ],
      "prisons": [
        "MDI"
      ]
    }
  ]
}
""".trimIndent()
