package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison

fun prisonSentencesAndOffences(bookingId: Int) = """
[
  {
    "bookingId": $bookingId,
    "sentenceSequence": 1,
    "lineSequence": 1,
    "caseSequence": 1,
    "courtDescription": "Amersham Crown Court",
    "sentenceStatus": "I",
    "sentenceCategory": "2003",
    "sentenceCalculationType": "ADIMP",
    "sentenceTypeDescription": "CJA03 Standard Determinate Sentence",
    "sentenceDate": "2019-02-01",
    "sentenceStartDate": "2019-02-01",
    "sentenceEndDate": "2029-01-31",
    "terms": [
      {
        "years": 10,
        "months": 0,
        "weeks": 0,
        "days": 0,
        "code": "IMP"
      }
    ],
    "offences": [
      {
        "offenderChargeId": 3934354,
        "offenceStartDate": "2018-01-31",
        "offenceStatute": "DD91",
        "offenceCode": "DD91011",
        "offenceDescription": "Abandon a fighting dog",
        "indicators": [
          "99"
        ]
      },
      {
        "offenderChargeId": 3934356,
        "offenceStartDate": "2018-12-21",
        "offenceStatute": "AN16",
        "offenceCode": "AN16252",
        "offenceDescription": "Act as member of flight crew of aircraft without holding appropriate licence",
        "indicators": [
          "99"
        ]
      }
    ]
  },
  {
    "bookingId": $bookingId,
    "sentenceSequence": 2,
    "lineSequence": 2,
    "caseSequence": 1,
    "courtDescription": "Amersham Crown Court",
    "sentenceStatus": "I",
    "sentenceCategory": "2003",
    "sentenceCalculationType": "EPP",
    "sentenceTypeDescription": "Extended Sent Public Protection CJA 03",
    "sentenceDate": "2019-02-01",
    "sentenceStartDate": "2020-02-01",
    "sentenceEndDate": "2028-01-31",
    "terms": [
      {
        "years": 8,
        "months": 6,
        "weeks": 3,
        "days": 2,
        "code": "IMP"
      }
    ],
    "offences": [
      {
        "offenderChargeId": 3934355,
        "offenceStartDate": "2019-02-25",
        "offenceStatute": "TH68",
        "offenceCode": "TH68058",
        "offenceDescription": "Abstract / use without authority electricity",
        "indicators": [
          "52"
        ]
      },
      {
        "offenderChargeId": 3934357,
        "offenceStartDate": "2018-12-21",
        "offenceStatute": "HT04",
        "offenceCode": "HT04001",
        "offenceDescription": "Activity without appropriate consent - Human Tissue Act 2004",
        "indicators": [
          "99"
        ]
      }
    ]
  }
]
""".trimIndent()
