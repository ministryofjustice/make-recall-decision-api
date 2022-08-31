package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.time.LocalDate

data class RecommendationResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val id: Long? = null,
  val status: Status? = null,
  val custodyStatus: CustodyStatus? = null,
  val localPoliceContact: LocalPoliceContact? = null,
  val crn: String? = null,
  val recallType: RecallType? = null,
  val responseToProbation: String? = null,
  val isThisAnEmergencyRecall: Boolean? = null,
  val hasVictimsInContactScheme: VictimsInContactScheme? = null,
  val dateVloInformed: LocalDate? = null,
  val hasArrestIssues: SelectedWithDetails? = null,
  val personOnProbation: PersonOnProbation? = null,
  val alternativesToRecallTried: AlternativesToRecallTried? = null,
  val licenceConditionsBreached: LicenceConditionsBreached? = null,
  @JsonProperty("isUnderIntegratedOffenderManagement") val underIntegratedOffenderManagement: UnderIntegratedOffenderManagement? = null,
  val vulnerabilities: Vulnerabilities? = null
)

data class UnderIntegratedOffenderManagement(
  val selected: String? = null,
  val allOptions: List<TextValueOption>? = null
)

data class PersonOnProbation(
  val name: String? = null,
  val firstName: String? = null,
  val surname: String? = null
)
