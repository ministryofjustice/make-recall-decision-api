package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun limitedAccessOffenderSearchResponse(crn: String) = """
{
    "content": [
        {
            "offenderId": 123456753,
            "otherIds": {
                "crn": "$crn"
            },
            "offenderManagers": [
                {
                    "trustOfficer": {
                        "forenames": "Test",
                        "surname": "Officername"
                    },
                    "staff": {
                        "code": "A123456",
                        "forenames": "Tester",
                        "surname": "Name",
                        "unallocated": false
                    },
                    "partitionArea": "National Data",
                    "softDeleted": false,
                    "team": {
                        "code": "N123",
                        "description": "Ox 4",
                        "localDeliveryUnit": {
                            "code": "NE1",
                            "description": "Ox"
                        },
                        "district": {
                            "code": "N123",
                            "description": "Ox"
                        },
                        "borough": {
                            "code": "N123",
                            "description": "Ox"
                        }
                    },
                    "probationArea": {
                        "code": "N22",
                        "description": "NPS North West Region",
                        "nps": true
                    },
                    "fromDate": "2019-06-27",
                    "active": true,
                    "allocationReason": {
                        "code": "TR2",
                        "description": "Transfer to support NPS Restructure"
                    }
                }
            ],
            "accessDenied": true
        }
    ],
    "pageable": {
        "sort": {
            "empty": true,
            "unsorted": true,
            "sorted": false
        },
        "offset": 0,
        "pageNumber": 0,
        "pageSize": 10,
        "paged": true,
        "unpaged": false
    },
    "probationAreaAggregations": [
        {
            "code": "N123",
            "count": 1
        }
    ],
    "suggestions": {
        "suggest": {
            "firstName": [
                {
                    "text": "$crn",
                    "offset": 0,
                    "length": 7,
                    "options": []
                }
            ],
            "surname": [
                {
                    "text": "$crn",
                    "offset": 0,
                    "length": 7,
                    "options": []
                }
            ]
        }
    },
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": true,
        "unsorted": true,
        "sorted": false
    },
    "first": true,
    "numberOfElements": 1,
    "empty": false
}
""".trimIndent()
