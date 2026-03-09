package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean

fun hasBeenReviewed(
  personOnProbation: Boolean = randomBoolean(),
  convictionDetail: Boolean = randomBoolean(),
  mappa: Boolean = randomBoolean(),
) = HasBeenReviewed(
  personOnProbation = personOnProbation,
  convictionDetail = convictionDetail,
  mappa = mappa,
)
