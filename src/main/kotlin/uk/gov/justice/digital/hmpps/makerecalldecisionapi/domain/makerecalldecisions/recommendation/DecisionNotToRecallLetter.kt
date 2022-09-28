package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

enum class DecisionNotToRecallLetter(val content: String) {
  LETTER_TITLE("DECISION NOT TO RECALL"),
  INTRODUCTION_PARAGRAPH("I am writing to you because you have breached your licence conditions in such a way that"),
  BREACH_DISCUSSED_PARAGRAPH("This breach has been discussed with a Probation manager and a decision has been made that you will not be recalled to prison. This letter explains this decision. If you have any questions, please contact me."),
  CLARIFICATION_PARAGRAPH("I hope our conversation and/or this letter has helped to clarify what is required of you going forward and that we can continue to work together to enable you to successfully complete your licence period."),
  NEXT_APPOINTMENT_PARAGRAPH("Your next appointment is by "),
  ON(" on:"),
  CLOSING_PARAGRAPH("You must please contact me immediately if you are not able to keep this appointment. Should you wish to discuss anything before then, please contact me by the following telephone number: "),
}
