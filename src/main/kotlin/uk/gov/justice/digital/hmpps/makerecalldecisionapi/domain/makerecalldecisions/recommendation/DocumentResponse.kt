package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess

data class DocumentResponse(
  val userAccessResponse: UserAccess? = null,
  val fileName: String? = null,
  val fileContents: String? = null,
  val letterContent: LetterContent? = null,
)
