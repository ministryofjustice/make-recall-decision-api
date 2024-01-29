package uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.ErrorResponse

class PpudValidationException(val errorResponse: ErrorResponse) : RuntimeException()
