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
  val whatLedToRecall: String? = null,
  val isThisAnEmergencyRecall: Boolean? = null,
  val hasVictimsInContactScheme: VictimsInContactScheme? = null,
  val dateVloInformed: LocalDate? = null,
  val hasArrestIssues: SelectedWithDetails? = null,
  val hasContrabandRisk: SelectedWithDetails? = null,
  val personOnProbation: PersonOnProbation? = null,
  val alternativesToRecallTried: AlternativesToRecallTried? = null,
  val licenceConditionsBreached: LicenceConditionsBreached? = null,
  @JsonProperty("isUnderIntegratedOffenderManagement") val underIntegratedOffenderManagement: UnderIntegratedOffenderManagement? = null,
  val vulnerabilities: Vulnerabilities? = null,
  val convictionDetail: ConvictionDetail? = null
)

data class UnderIntegratedOffenderManagement(
  val selected: String? = null,
  val allOptions: List<TextValueOption>? = null
)

data class PersonOnProbation(
  val name: String? = null,
  val firstName: String? = null,
  val surname: String? = null,
  val middleNames: String? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val dateOfBirth: LocalDate? = null,
  val croNumber: String? = null,
  val mostRecentPrisonerNumber: String? = null,
  val nomsNumber: String? = null,
  val pncNumber: String? = null,
  val mappaCategory: String? = null,
  val mappaLevel: String? = null
)

data class ConvictionDetail(
  val indexOffenceDescription: String? = null,
  val dateOfOriginalOffence: LocalDate? = null,
  val dateOfSentence: LocalDate? = null,
  val lengthOfSentence: Int? = null,
  val lengthOfSentenceUnits: String? = null,
  val sentenceDescription: String? = null,
  val licenceExpiryDate: LocalDate? = null,
  val sentenceExpiryDate: LocalDate? = null,
  val sentenceSecondLength: Int? = null,
  val sentenceSecondLengthUnits: String? = null
)
