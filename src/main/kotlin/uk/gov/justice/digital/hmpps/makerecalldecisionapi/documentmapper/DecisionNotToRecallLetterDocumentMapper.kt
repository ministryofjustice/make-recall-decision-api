package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DecisionNotToRecallLetter
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants

class DecisionNotToRecallLetterDocumentMapper : LetterDocumentMapper() {

  fun mapRecommendationDataToDocumentData(
    recommendation: RecommendationEntity
  ): DocumentData {
    return mapRecommendationDataToLetterDocumentData(recommendation, buildFirstBlockOfLetterContent(recommendation), buildNextAppointmentDetails(recommendation), buildClosingParagraphOfLetter(recommendation))
  }

  private fun buildFirstBlockOfLetterContent(recommendation: RecommendationEntity): String? {
    return DecisionNotToRecallLetter.INTRODUCTION_PARAGRAPH.content + " " +
      getDisplayTextFromWhyConsideredRecall(recommendation.data.whyConsideredRecall) + ".\n\n" +
      DecisionNotToRecallLetter.BREACH_DISCUSSED_PARAGRAPH.content + "\n\n" +
      recommendation.data.reasonsForNoRecall?.licenceBreach + "\n\n" +
      recommendation.data.reasonsForNoRecall?.noRecallRationale + "\n\n" +
      recommendation.data.reasonsForNoRecall?.popProgressMade + "\n\n" +
      recommendation.data.reasonsForNoRecall?.futureExpectations + "\n\n" +
      DecisionNotToRecallLetter.CLARIFICATION_PARAGRAPH.content + "\n\n" +
      DecisionNotToRecallLetter.NEXT_APPOINTMENT_PARAGRAPH.content + nextAppointmentBy(recommendation) + DecisionNotToRecallLetter.ON.content
  }

  private fun buildNextAppointmentDetails(recommendation: RecommendationEntity): String? {
    return generateLongDateAndTime(recommendation.data.nextAppointment?.dateTimeOfAppointment) + "\n"
  }

  private fun buildClosingParagraphOfLetter(recommendation: RecommendationEntity): String? {
    return DecisionNotToRecallLetter.CLOSING_PARAGRAPH.content + recommendation.data.nextAppointment?.probationPhoneNumber + "\n"
  }

  override fun buildLetterTitle(): String {
    return DecisionNotToRecallLetter.LETTER_TITLE.content
  }

  private fun getDisplayTextFromWhyConsideredRecall(whyConsideredRecall: WhyConsideredRecall?): String {
    return whyConsideredRecall?.allOptions
      ?.associate { it.value to it.text?.lowercase() }
      ?.get(whyConsideredRecall.selected?.name) ?: MrdTextConstants.EMPTY_STRING
  }
}
