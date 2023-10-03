package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

/*
  This is a temporary solution that should be replaced by a service call to
  obtain region descriptions from codes. It was introduced for MRD-1800.
 */
class Regions {
  companion object {
    val regionMap = mapOf(
      "N43" to "National Security Division",
      "N53" to "East Midlands",
      "N56" to "East of England",
      "N50" to "Greater Manchester",
      "N57" to "Kent, Surrey and Sussex",
      "N07" to "London",
      "N54" to "North East",
      "N51" to "North West",
      "N59" to "South Central",
      "N58" to "South West",
      "N03" to "Wales",
      "N52" to "West Midlands",
      "N55" to "Yorkshire and the Humber",
    )
  }
}
