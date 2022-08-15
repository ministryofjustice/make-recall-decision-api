package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternative
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternativeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactSchemeValue
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
          isThisAnEmergencyRecall = true,
          hasVictimsInContactScheme = victimsInContactSchemeData(),
          dateVloInformed = LocalDate.now(),
          lastModifiedBy = "Jack",
          lastModifiedDate = "2022-07-01T15:22:24.567Z",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z",
          personOnProbation = PersonOnProbation(firstName = firstName, surname = surname)
        )
      )
    }

    fun updateRecommendationRequestData(): UpdateRecommendationRequest {
      return UpdateRecommendationRequest(
        status = null,
        recallType = recallTypeData(),
        custodyStatus = custodyStatusData(),
        responseToProbation = "They have not responded well",
        isThisAnEmergencyRecall = true,
        hasVictimsInContactScheme = victimsInContactSchemeData(),
        dateVloInformed = LocalDate.now(),
        alternativesToRecallTried = AlternativesToRecallTried(
          selected = listOf(SelectedAlternative(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, details = "We sent a warning letter on 27th July 2022")),
          allOptions = listOf(TextValueOption(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, text = "Warnings/licence breach letters"))
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
        allOptions = listOf(
          TextValueOption(value = "YES_PRISON", text = "Yes, prison custody"),
          TextValueOption(value = "YES_POLICE", text = "Yes, police custody"),
          TextValueOption(value = "NO", text = "No")
        )
      )
    }

    private fun victimsInContactSchemeData(): VictimsInContactScheme {
      return VictimsInContactScheme(
        selected = VictimsInContactSchemeValue.YES,
        allOptions = listOf(
          TextValueOption(value = "YES", text = "Yes"),
          TextValueOption(value = "NO", text = "No"),
          TextValueOption(value = "NOT_APPLICABLE", text = "N/A")
        )
      )
    }
  }
}
