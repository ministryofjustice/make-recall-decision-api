package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

class MrdTestDataBuilder {
  companion object Helper {

    fun recommendationDataEntityData(crn: String?, firstName: String = "Jim", surname: String = "Long"): RecommendationEntity {
      return RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          status = Status.DRAFT,
          recallType = RecallType(
            selected = RecallTypeSelectedValue(value = RecallTypeValue.FIXED_TERM, details = "My details"),
            allOptions = listOf(
              TextValueOption(value = "NO_RECALL", text = "No recall"),
              TextValueOption(value = "FIXED_TERM", text = "Fixed term"),
              TextValueOption(value = "STANDARD", text = "Standard")
            )
          ),
          custodyStatus = CustodyStatus(
            selected = CustodyStatusValue.YES_PRISON,
            allOptions = listOf(
              TextValueOption(value = "YES_PRISON", text = "Yes, prison custody"),
              TextValueOption(value = "YES_POLICE", text = "Yes, police custody"),
              TextValueOption(value = "NO", text = "No")
            )
          ),
          responseToProbation = "They have not responded well",
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
        recallType = RecallType(
          selected = RecallTypeSelectedValue(value = RecallTypeValue.NO_RECALL, details = "details"),
          allOptions = listOf(
            TextValueOption(value = "NO_RECALL", text = "No recall"),
            TextValueOption(value = "FIXED_TERM", text = "Fixed term"),
            TextValueOption(value = "STANDARD", text = "Standard")
          )
        ),
        custodyStatus = CustodyStatus(
          selected = CustodyStatusValue.YES_PRISON,
          allOptions = listOf(
            TextValueOption(value = "YES_PRISON", text = "Yes, prison custody"),
            TextValueOption(value = "YES_POLICE", text = "Yes, police custody"),
            TextValueOption(value = "NO", text = "No")
          )
        ),
        responseToProbation = "They have not responded well"
      )
    }
  }
}
