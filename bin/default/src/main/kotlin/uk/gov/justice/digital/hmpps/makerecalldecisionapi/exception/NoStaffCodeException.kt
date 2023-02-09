package uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception

class NoStaffCodeException(message: String, val error: String) : RuntimeException(message)
