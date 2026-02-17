package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate

interface TemplateVersion {
  val path: String
}

enum class PartATemplateVersion(val flagVariantKey: String, override val path: String) : TemplateVersion {
  DEFAULT("default", "default"),
  FTR48("ftr48", "2025-09-02 - FTR48 Phase 1"),
  RISK_TO_SELF("riskToSelf", "2026-02-02 - Risk to self"),
  ;

  companion object {
    fun forVariantKey(flagVariantKey: String): PartATemplateVersion = PartATemplateVersion.entries.first { it.flagVariantKey == flagVariantKey }
  }
}

enum class DntrTemplateVersion(override val path: String) : TemplateVersion {
  DEFAULT("default"),
}
