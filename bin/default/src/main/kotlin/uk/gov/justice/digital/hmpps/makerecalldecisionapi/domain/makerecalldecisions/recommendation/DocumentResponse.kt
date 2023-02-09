package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class DocumentResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val fileName: String? = null,
  val fileContents: String? = null,
  val letterContent: LetterContent? = null
)
