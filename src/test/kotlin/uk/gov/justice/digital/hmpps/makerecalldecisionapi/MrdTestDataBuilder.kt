package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LocalPoliceContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternativeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.StandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.Vulnerabilities
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.time.LocalDate

class MrdTestDataBuilder {
  companion object Helper {

    fun recommendationDataEntityData(
      crn: String?,
      firstName: String = "Jim",
      surname: String = "Long"
    ): RecommendationEntity {
      return RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          status = Status.DRAFT,
          recallType = recallTypeData(),
          custodyStatus = custodyStatusData(),
          responseToProbation = "They have not responded well",
          whatLedToRecall = "Increasingly violent behaviour",
          isThisAnEmergencyRecall = true,
          hasVictimsInContactScheme = victimsInContactSchemeData(),
          dateVloInformed = LocalDate.now(),
          hasArrestIssues = arrestIssues(),
          hasContrabandRisk = contrabandRisk(),
          lastModifiedBy = "Jack",
          lastModifiedDate = "2022-07-01T15:22:24.567Z",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z",
          personOnProbation = PersonOnProbation(firstName = firstName, surname = surname),
          alternativesToRecallTried = alternativesToRecallTried(),
          licenceConditionsBreached = licenceConditionsBreached(),
          underIntegratedOffenderManagement = UnderIntegratedOffenderManagement(
            selected = "YES",
            allOptions = listOf(
              TextValueOption(value = "YES", text = "Yes"), TextValueOption(value = "NO", text = "No"), TextValueOption(value = "NOT_APPLICABLE", text = "N/A")
            )
          ),
          localPoliceContact = localPoliceContact()
        )
      )
    }

    fun updateRecommendationRequestData(existingRecommendation: RecommendationEntity): RecommendationModel {
      return RecommendationModel(
        crn = existingRecommendation.data.crn,
        createdBy = existingRecommendation.data.createdBy,
        createdDate = existingRecommendation.data.createdDate,
        personOnProbation = existingRecommendation.data.personOnProbation,
        status = Status.DRAFT,
        recallType = recallTypeData(),
        custodyStatus = custodyStatusData(),
        responseToProbation = "They have not responded well",
        whatLedToRecall = "Increasingly violent behaviour",
        isThisAnEmergencyRecall = true,
        hasVictimsInContactScheme = victimsInContactSchemeData(),
        dateVloInformed = LocalDate.now(),
        alternativesToRecallTried = alternativesToRecallTried(),
        hasArrestIssues = arrestIssues(),
        hasContrabandRisk = contrabandRisk(),
        licenceConditionsBreached = licenceConditionsBreached(),
        underIntegratedOffenderManagement = integratedOffenderManagement(),
        localPoliceContact = localPoliceContact(),
        vulnerabilities = vulnerabilities()
      )
    }

    private fun vulnerabilities(): Vulnerabilities {
      return Vulnerabilities(
        selected = listOf(ValueWithDetails(value = VulnerabilityOptions.RISK_OF_SUICIDE_OR_SELF_HARM.name, details = "Risk of suicide")),
        allOptions = listOf(
          TextValueOption(value = VulnerabilityOptions.RISK_OF_SUICIDE_OR_SELF_HARM.name, text = "Risk of suicide or self harm"),
          TextValueOption(value = VulnerabilityOptions.RELATIONSHIP_BREAKDOWN.name, text = "Relationship breakdown")
        )
      )
    }

    private fun recallTypeData(): RecallType {
      return RecallType(
        selected = RecallTypeSelectedValue(value = RecallTypeValue.FIXED_TERM, details = "My details"),
        allOptions = listOf(
          TextValueOption(value = "NO_RECALL", text = "No recall"),
          TextValueOption(value = "FIXED_TERM", text = "Fixed term"),
          TextValueOption(value = "STANDARD", text = "Standard")
        )
      )
    }

    private fun custodyStatusData(): CustodyStatus {
      return CustodyStatus(
        selected = CustodyStatusValue.YES_PRISON,
        details = "Bromsgrove Police Station\r\nLondon",
        allOptions = listOf(
          TextValueOption(value = "YES_PRISON", text = "Yes, prison custody"),
          TextValueOption(value = "YES_POLICE", text = "Yes, police custody"),
          TextValueOption(value = "NO", text = "No")
        )
      )
    }

    private fun victimsInContactSchemeData(): VictimsInContactScheme {
      return VictimsInContactScheme(
        selected = YesNoNotApplicableOptions.YES,
        allOptions = listOf(
          TextValueOption(value = "YES", text = "Yes"),
          TextValueOption(value = "NO", text = "No"),
          TextValueOption(value = "NOT_APPLICABLE", text = "N/A")
        )
      )
    }

    private fun alternativesToRecallTried(): AlternativesToRecallTried {
      return AlternativesToRecallTried(
        selected = listOf(ValueWithDetails(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, details = "We sent a warning letter on 27th July 2022")),
        allOptions = listOf(TextValueOption(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, text = "Warnings/licence breach letters"))
      )
    }

    private fun arrestIssues(): SelectedWithDetails {
      return SelectedWithDetails(selected = true, details = "Arrest issue details")
    }

    private fun contrabandRisk(): SelectedWithDetails {
      return SelectedWithDetails(selected = true, details = "Contraband risk details")
    }

    private fun licenceConditionsBreached(): LicenceConditionsBreached {
      return LicenceConditionsBreached(
        standardLicenceConditions = StandardLicenceConditions(
          selected = listOf(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name),
          allOptions = listOf(TextValueOption(value = SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name, text = "They had good behaviour"))
        ),
        additionalLicenceConditions = AdditionalLicenceConditions(
          selected = listOf("NST14"),
          allOptions = listOf(
            AdditionalLicenceConditionOption(
              subCatCode = "NST14",
              mainCatCode = "NLC5",
              title = "Additional title", details = "Additional details", note = "Additional note"
            )
          )
        )
      )
    }

    private fun localPoliceContact(): LocalPoliceContact {
      return LocalPoliceContact(contactName = "Thomas Magnum", phoneNumber = "555-0100", faxNumber = "555-0199", emailAddress = "thomas.magnum@gmail.com")
    }

    private fun integratedOffenderManagement(): UnderIntegratedOffenderManagement {
      return UnderIntegratedOffenderManagement(
        selected = "YES",
        allOptions = listOf(TextValueOption(value = "YES", text = "Yes"), TextValueOption(value = "NO", text = "No"), TextValueOption(value = "NOT_APPLICABLE", text = "N/A"))
      )
    }
  }
}
