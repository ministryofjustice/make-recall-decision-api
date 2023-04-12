package uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception

class RecommendationStatusUpdateException(message: String, val error: String) : RuntimeException(message)
