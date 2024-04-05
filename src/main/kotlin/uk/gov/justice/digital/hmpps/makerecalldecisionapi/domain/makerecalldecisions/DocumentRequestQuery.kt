package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

internal data class DocumentRequestQuery(
  val format: String,
)

enum class DocumentRequestType(val type: String) {
  DOWNLOAD_DOC_X("download-docx"),
  PREVIEW("preview"),
  ;

  companion object {

    private val map = DocumentRequestType.entries.associateBy(DocumentRequestType::type)

    fun fromString(type: String) = map[type]
  }
}
