package uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.MakeRecallDecisionRuntimeException

class ClientTimeoutRuntimeException(clientName: String, errorType: String) :
  MakeRecallDecisionRuntimeException("$clientName: [$errorType]")
