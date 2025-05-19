package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DecisionNotToRecallLetter
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants

@Component
class DecisionNotToRecallLetterDocumentMapper : LetterDocumentMapper() {

  fun mapRecommendationDataToDocumentData(
    recommendation: RecommendationResponse,
  ): DocumentData = mapRecommendationDataToLetterDocumentData(
    recommendation,
    buildFirstBlockOfLetterContent(recommendation),
    buildNextAppointmentDetails(recommendation),
    buildClosingParagraphOfLetter(recommendation),
  )

  private fun buildFirstBlockOfLetterContent(recommendation: RecommendationResponse): String? = DecisionNotToRecallLetter.INTRODUCTION_PARAGRAPH.content + " " +
    getDisplayTextFromWhyConsideredRecall(recommendation.whyConsideredRecall) + ".\n\n" +
    DecisionNotToRecallLetter.BREACH_DISCUSSED_PARAGRAPH.content + "\n\n" +
    recommendation.reasonsForNoRecall?.licenceBreach + "\n\n" +
    recommendation.reasonsForNoRecall?.noRecallRationale + "\n\n" +
    recommendation.reasonsForNoRecall?.popProgressMade + "\n\n" +
    recommendation.reasonsForNoRecall?.popThoughts + "\n\n" +
    recommendation.reasonsForNoRecall?.futureExpectations + "\n\n" +
    DecisionNotToRecallLetter.CLARIFICATION_PARAGRAPH.content + "\n\n" +
    DecisionNotToRecallLetter.NEXT_APPOINTMENT_PARAGRAPH.content + nextAppointmentBy(recommendation) + DecisionNotToRecallLetter.ON.content

  private fun buildNextAppointmentDetails(recommendation: RecommendationResponse): String? = generateLongDateAndTime(recommendation.nextAppointment?.dateTimeOfAppointment) + "\n"

  private fun buildClosingParagraphOfLetter(recommendation: RecommendationResponse): String? = DecisionNotToRecallLetter.CLOSING_PARAGRAPH.content + recommendation.nextAppointment?.probationPhoneNumber + "\n"

  override fun buildLetterTitle(): String = DecisionNotToRecallLetter.LETTER_TITLE.content

  private fun getDisplayTextFromWhyConsideredRecall(whyConsideredRecall: WhyConsideredRecall?): String = whyConsideredRecall?.allOptions
    ?.associate { it.value to it.text?.lowercase() }
    ?.get(whyConsideredRecall.selected?.name) ?: MrdTextConstants.EMPTY_STRING
}
