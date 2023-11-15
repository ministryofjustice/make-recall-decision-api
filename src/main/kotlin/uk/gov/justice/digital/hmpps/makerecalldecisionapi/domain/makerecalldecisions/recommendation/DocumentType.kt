package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

enum class DocumentType(val fileName: String) {
  PART_A_DOCUMENT("NAT Recall Part A London Template - obtained 231114.docx"),
  PREVIEW_PART_A_DOCUMENT("Preview NAT Recall Part A London Template - obtained 231114.docx"),
  DNTR_DOCUMENT("DNTR Template.docx"),
}
