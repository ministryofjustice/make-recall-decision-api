package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses

fun contactSummaryResponse() = """
{
    "content": [
        {
            "contactId": 2504412185,
            "contactStart": "2022-06-03T08:00:00+01:00",
            "contactEnd": "2022-06-03T00:00:00+01:00",
            "type": {
                "code": "COAP",
                "description": "Registration Review",
                "shortDescription": "ERGR - SGC",
                "appointment": false,
                "nationalStandard": false,
                "categories": [
                    {
                        "code": "RR",
                        "description": "Risk & Registers"
                    },
                    {
                        "code": "AL",
                        "description": "All/Always"
                    }
                ],
                "systemGenerated": true
            },
            "notes": "Comment added by John Smith on 05/05/2022",
            "provider": {
                "code": "N07",
                "description": "NPS London"
            },
            "team": {
                "code": "N07UAT",
                "description": "Unallocated Team(N07)"
            },
            "staff": {
                "code": "N07A726",
                "forenames": "ndelius126Forename",
                "surname": "ndelius126Surname",
                "unallocated": false
            },
            "rarActivity": false,
            "lastUpdatedDateTime": "2022-05-05T17:45:01+01:00",
            "lastUpdatedByUser": {
                "forenames": "André",
                "surname": "Petheram"
            }
        },
        {
            "contactId": 2504435532,
            "contactStart": "2022-05-10T11:39:00+01:00",
            "contactEnd": "2022-05-10T00:00:00",
            "type": {
                "code": "C204",
                "description": "Police Liaison",
                "appointment": false,
                "nationalStandard": false,
                "categories": [
                    {
                        "code": "TC",
                        "description": "Throughcare"
                    },
                    {
                        "code": "AL",
                        "description": "All/Always"
                    }
                ],
                "systemGenerated": false
            },
            "notes": "This is a test",
            "provider": {
                "code": "N07",
                "description": "NPS London"
            },
            "team": {
                "code": "N07UAT",
                "description": "Unallocated Team(N07)"
            },
            "staff": {
                "code": "N07UATU",
                "forenames": "Unallocated Staff(N07)",
                "surname": "Staff",
                "unallocated": true
            },
            "sensitive": true,
            "enforcement": {
                "enforcementAction": {
                    "code": "WLS",
                    "description": "Enforcement Letter Requested"
                }
            },
            "outcome": {
                "code": "TDRT",
                "description": "Test - Not Clean / Not Acceptable / Unsuitable",
                "attended": true,
                "complied": false
            },
            "rarActivity": false,
            "lastUpdatedDateTime": "2022-05-10T11:44:07+01:00",
            "lastUpdatedByUser": {
                "forenames": "John",
                "surname": "Smith"
            },
            "description": "This is a contact description"
        }
    ],
    "pageable": {
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "pageSize": 1000,
        "pageNumber": 0,
        "unpaged": false,
        "paged": true
    },
    "last": true,
    "totalElements": 15,
    "totalPages": 1,
    "size": 1000,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 2,
    "empty": false
}
""".trimIndent()
