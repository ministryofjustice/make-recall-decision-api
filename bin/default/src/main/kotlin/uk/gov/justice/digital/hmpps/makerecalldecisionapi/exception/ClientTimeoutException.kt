package uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.MakeRecallDecisionException

class ClientTimeoutException(clientName: String, errorType: String) : MakeRecallDecisionException("$clientName: [$errorType]")
