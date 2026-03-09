package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SentenceGroup
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel

/**
 * Determines whether the sentence is indeterminate, encompassing both pre-FTR56 behaviour
 * (when there was an isIndeterminateSentence field) and post-FTR56 behaviour (when there
 * is a sentenceGroup field).
 */
fun RecommendationModel.calculateIsIndeterminateSentence(): Boolean? = if (sentenceGroup != null) sentenceGroup == SentenceGroup.INDETERMINATE else isIndeterminateSentence

/**
 * Determines whether the sentence is extended, encompassing both pre-FTR56 behaviour
 * (when there was an isExtendedSentence field) and post-FTR56 behaviour (when there
 * is a sentenceGroup field).
 */
fun RecommendationModel.calculateIsExtendedSentence(): Boolean? = if (sentenceGroup != null) sentenceGroup == SentenceGroup.EXTENDED else isExtendedSentence

/**
 * Determines whether the sentence is indeterminate, encompassing both pre-FTR56 behaviour
 * (when there was an isIndeterminateSentence field) and post-FTR56 behaviour (when there
 * is a sentenceGroup field).
 */
fun RecommendationResponse.calculateIsIndeterminateSentence(): Boolean? = if (sentenceGroup != null) sentenceGroup == SentenceGroup.INDETERMINATE else isIndeterminateSentence

/**
 * Determines whether the sentence is extended, encompassing both pre-FTR56 behaviour
 * (when there was an isExtendedSentence field) and post-FTR56 behaviour (when there
 * is a sentenceGroup field).
 */
fun RecommendationResponse.calculateIsExtendedSentence(): Boolean? = if (sentenceGroup != null) sentenceGroup == SentenceGroup.EXTENDED else isExtendedSentence
