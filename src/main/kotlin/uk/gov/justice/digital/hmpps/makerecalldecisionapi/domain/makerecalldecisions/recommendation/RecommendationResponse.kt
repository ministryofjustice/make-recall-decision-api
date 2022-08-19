package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

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
  val licenceConditionsBreached: LicenceConditionsBreached? = null
)

data class PersonOnProbation(
  val name: String? = null,
  val firstName: String? = null,
  val surname: String? = null
)
