package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SelectedLicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.LicenceConditionsService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class LicenceConditionsController(
  private val licenceConditionsService: LicenceConditionsService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/licence-conditions")
  @Operation(summary = "Returns details of the licence conditions on a case")
  suspend fun licenseConditions(@PathVariable("crn") crn: String): LicenceConditionsResponse {
    log.info(normalizeSpace("Licence conditions endpoint hit for CRN: $crn"))
    return licenceConditionsService.getLicenceConditions(crn)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/licence-conditions-cvl")
  @Operation(summary = "Returns details of the licence conditions on a case from CVL")
  suspend fun licenseConditionsCvl(@PathVariable("crn") crn: String): LicenceConditionsCvlResponse {
    log.info(normalizeSpace("Licence conditions CVL endpoint hit for CRN: $crn"))
    return licenceConditionsService.getLicenceConditionsCvl(crn)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/licence-conditions/v2")
  @Operation(summary = "Returns details of the licence conditions on a case")
  suspend fun licenseConditionsV2(@PathVariable("crn") crn: String): SelectedLicenceConditionsResponse {
    log.info(normalizeSpace("Licence conditions endpoint hit for CRN: $crn"))
    return licenceConditionsService.getLicenceConditionsV2(crn)
  }
}
