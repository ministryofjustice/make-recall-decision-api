package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

enum class RiskScoreType(val printName: String) {
  OSPDC("OSP-DC"),
  OSPIIC("OSP-IIC"),
  OSPC("OSP-C"),
  OSPI("OSP-I"),
  OGRS("OGRS3"),
  OGP("OGP"),
  OVP("OVP"),
  RSR("RSR"),
  ;

  override fun toString(): String = printName
}
