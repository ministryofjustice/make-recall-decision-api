package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

enum class DocumentType(val fileName: String) {
  PART_A_DOCUMENT("NAT Recall Part A London Template - obtained 131021.docx"),
  DNTR_DOCUMENT("DNTR Template.docx")
}
