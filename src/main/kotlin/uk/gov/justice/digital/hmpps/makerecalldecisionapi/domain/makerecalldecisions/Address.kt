package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class Address(
  val line1: String?,
  val line2: String?,
  val town: String?,
  val postcode: String?,
  val noFixedAbode: Boolean,
) {

  fun separatorFormattedAddress(separator: String, includeName: Boolean = false, name: String? = null): String {
    val addressLines: MutableList<String> = ArrayList()

    if (includeName && !name.isNullOrEmpty()) {
      addressLines.add(name)
    }
    if (!line1.isNullOrEmpty()) {
      addressLines.add(line1)
    }
    if (!line2.isNullOrEmpty()) {
      addressLines.add(line2)
    }
    if (!town.isNullOrEmpty()) {
      addressLines.add(town)
    }
    if (!postcode.isNullOrEmpty()) {
      addressLines.add(postcode)
    }

    return addressLines.joinToString(separator) { "$it" }
  }
}
