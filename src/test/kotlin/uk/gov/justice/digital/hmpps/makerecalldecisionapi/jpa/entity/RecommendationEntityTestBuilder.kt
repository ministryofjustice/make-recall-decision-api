package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RoshSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.BookRecallToPpud
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.BookingMemento
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConsiderationRationale
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CvlLicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.HasBeenReviewed
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LocalPoliceContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecision
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NextAppointment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NomisIndexOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PpudOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PractitionerForPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PrisonOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ReasonsForNoRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallConsidered
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RoshData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SentenceGroup
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilitiesRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhoCompletedPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.alternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.bookRecallToPpud
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.bookingMemento
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.considerationRationale
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.convictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.custodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.cvlLicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.hasBeenReviewed
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.indeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.indeterminateSentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.licenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.localPoliceContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.managerRecallDecision
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.nextAppointment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.nomisIndexOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.personOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ppudOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.practitionerForPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.previousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.previousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.prisonOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.reasonsForNoRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.recallConsidered
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.recallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.roshData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.selectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.underIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.victimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.vulnerabilitiesRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.whoCompletedPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.whyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.roshSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate
import java.time.LocalDateTime

fun recommendationEntity(
  id: Long = randomInt().toLong(),
  data: RecommendationModel = recommendationModel(),
  deleted: Boolean = randomBoolean(),
) = RecommendationEntity(
  id = id,
  data = data,
  deleted = deleted,
)

fun recommendationModel(
  crn: String? = randomString(),
  sensitive: Boolean? = randomBoolean(),
  ppudRecordPresent: Boolean = randomBoolean(),
  recallConsideredList: List<RecallConsidered>? = listOf(recallConsidered()),
  recallType: RecallType? = recallType(),
  sendSpoRationaleToDelius: Boolean? = randomBoolean(),
  managerRecallDecision: ManagerRecallDecision? = managerRecallDecision(),
  considerationRationale: ConsiderationRationale? = considerationRationale(),
  custodyStatus: CustodyStatus? = custodyStatus(),
  localPoliceContact: LocalPoliceContact? = localPoliceContact(),
  responseToProbation: String? = randomString(),
  // deprecated
  thoughtsLeadingToRecall: String? = randomString(),
  triggerLeadingToRecall: String? = randomString(),
  whatLedToRecall: String? = randomString(),
  sentenceGroup: SentenceGroup? = randomEnum<SentenceGroup>(),
  isThisAnEmergencyRecall: Boolean? = randomBoolean(),
  isIndeterminateSentence: Boolean? = randomBoolean(),
  isExtendedSentence: Boolean? = randomBoolean(),
  activeCustodialConvictionCount: Number? = randomInt(),
  hasVictimsInContactScheme: VictimsInContactScheme? = victimsInContactScheme(),
  indeterminateSentenceType: IndeterminateSentenceType? = indeterminateSentenceType(),
  dateVloInformed: LocalDate? = randomLocalDate(),
  hasArrestIssues: SelectedWithDetails? = selectedWithDetails(),
  hasContrabandRisk: SelectedWithDetails? = selectedWithDetails(),
  status: Status? = randomEnum<Status>(),
  region: String? = randomString(),
  localDeliveryUnit: String? = randomString(),
  userNameDntrLetterCompletedBy: String? = randomString(),
  lastDntrLetterADownloadDateTime: LocalDateTime? = randomLocalDateTime(),
  reviewPractitionersConcerns: Boolean? = randomBoolean(),
  odmName: String? = randomString(),
  spoRecallType: String? = randomString(),
  spoRecallRationale: String? = randomString(),
  spoDeleteRecommendationRationale: String? = randomString(),
  sendSpoDeleteRationaleToDelius: Boolean? = randomBoolean(),
  // deprecated
  spoCancelRecommendationRationale: String? = randomString(),
  reviewOffenderProfile: Boolean? = randomBoolean(),
  explainTheDecision: Boolean? = randomBoolean(),
  lastModifiedBy: String? = randomString(),
  lastModifiedByUserName: String? = randomString(),
  lastModifiedDate: String? = randomString(),
  createdBy: String? = randomString(),
  createdByUserFullName: String? = randomString(),
  createdDate: String? = randomString(),
  personOnProbation: PersonOnProbation? = personOnProbation(),
  convictionDetail: ConvictionDetail? = convictionDetail(),
  alternativesToRecallTried: AlternativesToRecallTried? = alternativesToRecallTried(),
  licenceConditionsBreached: LicenceConditionsBreached? = licenceConditionsBreached(),
  cvlLicenceConditionsBreached: CvlLicenceConditionsBreached? = cvlLicenceConditionsBreached(),
  additionalLicenceConditionsText: String? = randomString(),
  vulnerabilities: VulnerabilitiesRecommendation? = vulnerabilitiesRecommendation(),
  underIntegratedOffenderManagement: UnderIntegratedOffenderManagement? = underIntegratedOffenderManagement(),
  indexOffenceDetails: String? = randomString(),
  offenceDataFromLatestCompleteAssessment: Boolean? = randomBoolean(),
  offencesMatch: Boolean? = randomBoolean(),
  offenceAnalysis: String? = randomString(),
  fixedTermAdditionalLicenceConditions: SelectedWithDetails? = selectedWithDetails(),
  indeterminateOrExtendedSentenceDetails: IndeterminateOrExtendedSentenceDetails? = indeterminateOrExtendedSentenceDetails(),
  mainAddressWherePersonCanBeFound: SelectedWithDetails? = selectedWithDetails(),
  whyConsideredRecall: WhyConsideredRecall? = whyConsideredRecall(),
  reasonsForNoRecall: ReasonsForNoRecall? = reasonsForNoRecall(),
  nextAppointment: NextAppointment? = nextAppointment(),
  hasBeenReviewed: HasBeenReviewed? = hasBeenReviewed(),
  previousReleases: PreviousReleases? = previousReleases(),
  previousRecalls: PreviousRecalls? = previousRecalls(),
  recommendationStartedDomainEventSent: Boolean? = randomBoolean(),
  currentRoshForPartA: RoshData? = roshData(),
  roshSummary: RoshSummary? = roshSummary(),
  countersignSpoTelephone: String? = randomString(),
  countersignSpoExposition: String? = randomString(),
  countersignAcoExposition: String? = randomString(),
  countersignAcoTelephone: String? = randomString(),
  whoCompletedPartA: WhoCompletedPartA? = whoCompletedPartA(),
  practitionerForPartA: PractitionerForPartA? = practitionerForPartA(),
  revocationOrderRecipients: List<String>? = listOf(randomString()),
  decisionDateTime: LocalDateTime? = randomLocalDateTime(),
  ppcsQueryEmails: List<String>? = listOf(randomString()),
  prisonOffender: PrisonOffender? = prisonOffender(),
  prisonApiLocationDescription: String? = randomString(),
  releaseUnderECSL: Boolean? = randomBoolean(),
  dateOfRelease: LocalDate? = randomLocalDate(),
  conditionalReleaseDate: LocalDate? = randomLocalDate(),
  nomisIndexOffence: NomisIndexOffence? = nomisIndexOffence(),
  bookRecallToPpud: BookRecallToPpud? = bookRecallToPpud(),
  ppudOffender: PpudOffender? = ppudOffender(),
  bookingMemento: BookingMemento? = bookingMemento(),
  // deprecated
  isOver18: Boolean? = randomBoolean(),
  isUnder18: Boolean? = randomBoolean(),
  isMappaLevelAbove1: Boolean? = randomBoolean(),
  // deprecated
  isSentenceUnder12Months: Boolean? = randomBoolean(),
  isSentence12MonthsOrOver: Boolean? = randomBoolean(),
  hasBeenConvictedOfSeriousOffence: Boolean? = randomBoolean(),
  isSentence48MonthsOrOver: Boolean? = randomBoolean(),
  isMappaCategory4: Boolean? = randomBoolean(),
  isMappaLevel2Or3: Boolean? = randomBoolean(),
  isRecalledOnNewChargedOffence: Boolean? = randomBoolean(),
  isServingFTSentenceForTerroristOffence: Boolean? = randomBoolean(),
  hasBeenChargedWithTerroristOrStateThreatOffence: Boolean? = randomBoolean(),
  // deprecated
  userNamePartACompletedBy: String? = randomString(),
  // deprecated
  userEmailPartACompletedBy: String? = randomString(),
  // deprecated
  lastPartADownloadDateTime: LocalDateTime? = randomLocalDateTime(),
  // deprecated
  countersignSpoDateTime: LocalDateTime? = randomLocalDateTime(),
  // deprecated
  countersignSpoName: String? = randomString(),
  // deprecated
  acoCounterSignEmail: String? = randomString(),
  // deprecated
  spoCounterSignEmail: String? = randomString(),
  // deprecated
  countersignAcoName: String? = randomString(),
  // deprecated
  countersignAcoDateTime: LocalDateTime? = randomLocalDateTime(),
  // deprecated
  deleted: Boolean = randomBoolean(),
) = RecommendationModel(
  crn = crn,
  sensitive = sensitive,
  ppudRecordPresent = ppudRecordPresent,
  recallConsideredList = recallConsideredList,
  recallType = recallType,
  sendSpoRationaleToDelius = sendSpoRationaleToDelius,
  managerRecallDecision = managerRecallDecision,
  considerationRationale = considerationRationale,
  custodyStatus = custodyStatus,
  localPoliceContact = localPoliceContact,
  responseToProbation = responseToProbation,
  thoughtsLeadingToRecall = thoughtsLeadingToRecall,
  triggerLeadingToRecall = triggerLeadingToRecall,
  whatLedToRecall = whatLedToRecall,
  sentenceGroup = sentenceGroup,
  isThisAnEmergencyRecall = isThisAnEmergencyRecall,
  isIndeterminateSentence = isIndeterminateSentence,
  isExtendedSentence = isExtendedSentence,
  activeCustodialConvictionCount = activeCustodialConvictionCount,
  hasVictimsInContactScheme = hasVictimsInContactScheme,
  indeterminateSentenceType = indeterminateSentenceType,
  dateVloInformed = dateVloInformed,
  hasArrestIssues = hasArrestIssues,
  hasContrabandRisk = hasContrabandRisk,
  status = status,
  region = region,
  localDeliveryUnit = localDeliveryUnit,
  userNameDntrLetterCompletedBy = userNameDntrLetterCompletedBy,
  lastDntrLetterADownloadDateTime = lastDntrLetterADownloadDateTime,
  reviewPractitionersConcerns = reviewPractitionersConcerns,
  odmName = odmName,
  spoRecallType = spoRecallType,
  spoRecallRationale = spoRecallRationale,
  spoDeleteRecommendationRationale = spoDeleteRecommendationRationale,
  sendSpoDeleteRationaleToDelius = sendSpoDeleteRationaleToDelius,
  spoCancelRecommendationRationale = spoCancelRecommendationRationale,
  reviewOffenderProfile = reviewOffenderProfile,
  explainTheDecision = explainTheDecision,
  lastModifiedBy = lastModifiedBy,
  lastModifiedByUserName = lastModifiedByUserName,
  lastModifiedDate = lastModifiedDate,
  createdBy = createdBy,
  createdByUserFullName = createdByUserFullName,
  createdDate = createdDate,
  personOnProbation = personOnProbation,
  convictionDetail = convictionDetail,
  alternativesToRecallTried = alternativesToRecallTried,
  licenceConditionsBreached = licenceConditionsBreached,
  cvlLicenceConditionsBreached = cvlLicenceConditionsBreached,
  additionalLicenceConditionsText = additionalLicenceConditionsText,
  vulnerabilities = vulnerabilities,
  underIntegratedOffenderManagement = underIntegratedOffenderManagement,
  indexOffenceDetails = indexOffenceDetails,
  offenceDataFromLatestCompleteAssessment = offenceDataFromLatestCompleteAssessment,
  offencesMatch = offencesMatch,
  offenceAnalysis = offenceAnalysis,
  fixedTermAdditionalLicenceConditions = fixedTermAdditionalLicenceConditions,
  indeterminateOrExtendedSentenceDetails = indeterminateOrExtendedSentenceDetails,
  mainAddressWherePersonCanBeFound = mainAddressWherePersonCanBeFound,
  whyConsideredRecall = whyConsideredRecall,
  reasonsForNoRecall = reasonsForNoRecall,
  nextAppointment = nextAppointment,
  hasBeenReviewed = hasBeenReviewed,
  previousReleases = previousReleases,
  previousRecalls = previousRecalls,
  recommendationStartedDomainEventSent = recommendationStartedDomainEventSent,
  currentRoshForPartA = currentRoshForPartA,
  roshSummary = roshSummary,
  countersignSpoTelephone = countersignSpoTelephone,
  countersignSpoExposition = countersignSpoExposition,
  countersignAcoExposition = countersignAcoExposition,
  countersignAcoTelephone = countersignAcoTelephone,
  whoCompletedPartA = whoCompletedPartA,
  practitionerForPartA = practitionerForPartA,
  revocationOrderRecipients = revocationOrderRecipients,
  decisionDateTime = decisionDateTime,
  ppcsQueryEmails = ppcsQueryEmails,
  prisonOffender = prisonOffender,
  prisonApiLocationDescription = prisonApiLocationDescription,
  releaseUnderECSL = releaseUnderECSL,
  dateOfRelease = dateOfRelease,
  conditionalReleaseDate = conditionalReleaseDate,
  nomisIndexOffence = nomisIndexOffence,
  bookRecallToPpud = bookRecallToPpud,
  ppudOffender = ppudOffender,
  bookingMemento = bookingMemento,
  isOver18 = isOver18,
  isUnder18 = isUnder18,
  isMappaLevelAbove1 = isMappaLevelAbove1,
  isSentenceUnder12Months = isSentenceUnder12Months,
  isSentence12MonthsOrOver = isSentence12MonthsOrOver,
  hasBeenConvictedOfSeriousOffence = hasBeenConvictedOfSeriousOffence,
  isSentence48MonthsOrOver = isSentence48MonthsOrOver,
  isMappaCategory4 = isMappaCategory4,
  isMappaLevel2Or3 = isMappaLevel2Or3,
  isRecalledOnNewChargedOffence = isRecalledOnNewChargedOffence,
  isServingFTSentenceForTerroristOffence = isServingFTSentenceForTerroristOffence,
  hasBeenChargedWithTerroristOrStateThreatOffence = hasBeenChargedWithTerroristOrStateThreatOffence,
  userNamePartACompletedBy = userNamePartACompletedBy,
  userEmailPartACompletedBy = userEmailPartACompletedBy,
  lastPartADownloadDateTime = lastPartADownloadDateTime,
  countersignSpoDateTime = countersignSpoDateTime,
  countersignSpoName = countersignSpoName,
  acoCounterSignEmail = acoCounterSignEmail,
  spoCounterSignEmail = spoCounterSignEmail,
  countersignAcoName = countersignAcoName,
  countersignAcoDateTime = countersignAcoDateTime,
  deleted = deleted,
)

fun textValueOption(
  value: String? = randomString(),
  text: String? = randomString(),
) = TextValueOption(
  value = value,
  text = text,
)
