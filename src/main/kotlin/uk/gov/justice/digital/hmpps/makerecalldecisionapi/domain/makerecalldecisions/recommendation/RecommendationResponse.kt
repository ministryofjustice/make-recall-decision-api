package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import java.time.LocalDate

data class RecommendationResponse(
  val id: Long? = null,
  val status: Status? = null,
  val custodyStatus: CustodyStatus? = null,
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
  // FIXME: the data structure here might need to be changed based on the outcome of the questions raised in comments on MRD-464.
  // There is no place in the Part A to put the IOM details so we might not need to send the details from the frontend.
  @JsonProperty("isUnderIntegratedOffenderManagement") val underIntegratedOffenderManagement: SelectedWithDetails? = null
)

data class PersonOnProbation(
  val name: String? = null,
  val firstName: String? = null,
  val surname: String? = null
)
