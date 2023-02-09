package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

open class RecommendationDataToDocumentMapper {
  companion object {
    fun formatFullName(firstName: String?, middleNames: String?, surname: String?): String? {
      val formattedField = if (firstName.isNullOrBlank()) {
        surname
      } else if (middleNames.isNullOrBlank()) {
        "$firstName $surname"
      } else "$firstName $middleNames $surname"
      return formattedField
    }
  }
}
