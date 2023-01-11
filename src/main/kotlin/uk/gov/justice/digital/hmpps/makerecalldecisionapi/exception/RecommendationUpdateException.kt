package uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception

class RecommendationUpdateException(message: String, val error: String) : RuntimeException(message)
