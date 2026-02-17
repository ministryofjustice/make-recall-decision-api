package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskBreakdown
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskOfSeriousHarm
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RoshSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.BookRecallToPpud
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CvlLicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.HasBeenReviewed
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.HowWillAppointmentHappen
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceTypeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionSection
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LocalPoliceContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecision
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecisionTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecisionTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NextAppointment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NextAppointmentValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NomisIndexOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.OfferedOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PpudOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PpudOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PpudSentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PpudSentenceLength
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PractitionerForPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ReasonsForNoRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallConsidered
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RoshData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RoshDataScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternativeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.StandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.Term
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilitiesRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhoCompletedPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecallValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.time.LocalDate
import java.time.LocalDateTime

class MrdTestDataBuilder {
  companion object Helper {

    fun recommendationDataEntityData(
      crn: String?,
      firstName: String = "Joe",
      surname: String = "Bloggs",
      status: Status = Status.DRAFT,
      recallTypeValue: RecallTypeValue? = RecallTypeValue.FIXED_TERM,
      lastModifiedDate: String? = "2022-07-01T15:22:24.567Z",
    ): RecommendationEntity = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        status = status,
        recallConsideredList = recallConsideredData(),
        recallType = recallTypeData(recallTypeValue),
        custodyStatus = custodyStatusData(),
        responseToProbation = "They have not responded well",
        triggerLeadingToRecall = "Just a bad morning, I guess",
        whatLedToRecall = "Increasingly violent behaviour",
        isThisAnEmergencyRecall = true,
        isIndeterminateSentence = true,
        isExtendedSentence = true,
        activeCustodialConvictionCount = 1,
        hasVictimsInContactScheme = victimsInContactSchemeData(),
        indeterminateSentenceType = indeterminateSentenceType(),
        dateVloInformed = LocalDate.now(),
        hasArrestIssues = arrestIssues(),
        hasContrabandRisk = contrabandRisk(),
        lastModifiedBy = "Jack",
        decisionDateTime = LocalDateTime.now(),
        lastModifiedByUserName = "jack",
        lastModifiedDate = lastModifiedDate,
        createdBy = "Jack",
        createdDate = "2022-07-01T15:22:24.567Z",
        personOnProbation = PersonOnProbation(
          firstName = firstName,
          surname = surname,
          mappa = Mappa(level = 1, category = 1, lastUpdatedDate = null),
          primaryLanguage = "English",
          dateOfBirth = LocalDate.parse("1982-10-24"),
          addresses = listOf(
            Address(
              line1 = "Line 1 address",
              line2 = "Line 2 address",
              town = "Town address",
              postcode = "TS1 1ST",
              noFixedAbode = false,
            ),
          ),
        ),
        alternativesToRecallTried = alternativesToRecallTried(),
        licenceConditionsBreached = licenceConditionsBreached(),
        underIntegratedOffenderManagement = UnderIntegratedOffenderManagement(
          selected = "YES",
          allOptions = listOf(
            TextValueOption(value = "YES", text = "Yes"),
            TextValueOption(value = "NO", text = "No"),
            TextValueOption(value = "NOT_APPLICABLE", text = "N/A"),
          ),
        ),
        localPoliceContact = localPoliceContact(),
        convictionDetail = convictionDetail(),
        region = "London",
        localDeliveryUnit = "LDU London",
        userNamePartACompletedBy = "John Doe",
        userEmailPartACompletedBy = "John.Doe@test.com",
        lastPartADownloadDateTime = null,
        fixedTermAdditionalLicenceConditions = SelectedWithDetails(
          selected = true,
          "This is an additional licence condition",
        ),
        mainAddressWherePersonCanBeFound = isMainAddressWherePersonCanBeFound(),
        whyConsideredRecall = whyConsideredRecall(),
        reasonsForNoRecall = reasonForNoRecall(),
        nextAppointment = nextAppointment(),
        indexOffenceDetails = "Offence details",
        previousReleases = previousReleases(),
        roshSummary = roshSummary(),
        whoCompletedPartA = WhoCompletedPartA(
          name = "Mr Jenkins",
          email = "jenkins@email.com",
          telephone = "1234567",
          region = "London",
          localDeliveryUnit = "A123",
          isPersonProbationPractitionerForOffender = false,
        ),
        practitionerForPartA = PractitionerForPartA(
          name = "Mr Jenkins 1",
          email = "jenkins1@email.com",
          telephone = "12345678",
          region = "London2",
          localDeliveryUnit = "A1234",
        ),
        nomisIndexOffence = NomisIndexOffence(
          selected = 123,
          allOptions = listOf(
            OfferedOffence(
              offenderChargeId = 123,
              offenceCode = "A123X",
              offenceStatute = "A123",
              offenceDescription = "",
              sentenceDate = LocalDate.now(),
              sentenceStartDate = LocalDate.now(),
              sentenceEndDate = LocalDate.now(),
              bookingId = 123,
              terms = listOf(Term(4, 4, 4, 4)),
            ),
          ),
        ),
        bookRecallToPpud = BookRecallToPpud(
          decisionDateTime = LocalDateTime.now(),
          mappaLevel = "Level 1",
          policeForce = "Kent Police",
          probationArea = "Kent",
          receivedDateTime = LocalDateTime.now(),
          sentenceDate = LocalDate.now(),
          minute = "some text",
        ),
        ppudOffender = PpudOffender(
          id = "12345678",
          croOtherNumber = "1234",
          dateOfBirth = "1990-09-14",
          familyName = "",
          firstNames = "",
          gender = "Male",
          immigrationStatus = "Naturalized",
          establishment = "HMP Brixton",
          nomsId = "",
          prisonerCategory = "Incarcerated",
          prisonNumber = "1234",
          sentences = listOf(
            PpudSentence(
              id = "1234123412341234",
              offenceDescription = "some description",
              dateOfSentence = "2006-01-01",
              sentenceExpiryDate = "2006-08-13",
              custodyType = "Extended",
              mappaLevel = "Level 1",
              licenceExpiryDate = "2006-08-14",
              tariffExpiryDate = "2006-08-14",
              offence = PpudOffence(
                indexOffence = "123",
                dateOfIndexOffence = "2007-08-14",
              ),
              releaseDate = "2027-08-14",
              releases = emptyList(),
              sentenceLength = PpudSentenceLength(1, 1, 1),
              sentencingCourt = "Richmond",
            ),
          ),
          status = "Active",
          youngOffender = "No",
          ethnicity = "White",
        ),
      ),
    )

    fun updateRecommendationWithManagerRecallDecisionRequestData(
      existingRecommendation: RecommendationEntity,
      sendSpoRationaleToDelius: String,
    ): RecommendationModel = RecommendationModel(
      spoRecallType = "RECALL",
      spoRecallRationale = "Recall",
      sendSpoRationaleToDelius = sendSpoRationaleToDelius != "false",
      crn = existingRecommendation.data.crn,
      createdBy = existingRecommendation.data.createdBy,
      createdDate = existingRecommendation.data.createdDate,
      status = Status.DRAFT,
      managerRecallDecision = ManagerRecallDecision(
        isSentToDelius = true,
        selected = ManagerRecallDecisionTypeSelectedValue(
          value = ManagerRecallDecisionTypeValue.RECALL,
          details = "Recall",
        ),
        allOptions = null,
        createdBy = "Bill",
        createdDate = "2022-07-26T09:48:27.443Z",
      ),
    )

    fun updateRecommendationWithDeleteRecallDecisionRequestData(
      existingRecommendation: RecommendationEntity,
      sendSpoDeleteRationaleToDelius: String,
    ): RecommendationModel = RecommendationModel(
      spoRecallType = "RECALL",
      spoRecallRationale = "Recall",
      sendSpoDeleteRationaleToDelius = sendSpoDeleteRationaleToDelius != "false",
      crn = existingRecommendation.data.crn,
      createdBy = existingRecommendation.data.createdBy,
      createdDate = existingRecommendation.data.createdDate,
      status = Status.DRAFT,
      spoDeleteRecommendationRationale = "Deleted due to error in Part A",
    )

    fun updateRecommendationRequestData(existingRecommendation: RecommendationEntity): RecommendationModel = RecommendationModel(
      crn = existingRecommendation.data.crn,
      createdBy = existingRecommendation.data.createdBy,
      createdDate = existingRecommendation.data.createdDate,
      personOnProbation = personOnProbation(existingRecommendation.data.personOnProbation),
      status = Status.DRAFT,
      recallType = recallTypeData(RecallTypeValue.FIXED_TERM),
      custodyStatus = custodyStatusData(),
      responseToProbation = "They have not responded well",
      whatLedToRecall = "Increasingly violent behaviour",
      isThisAnEmergencyRecall = true,
      isIndeterminateSentence = true,
      isExtendedSentence = false,
      activeCustodialConvictionCount = 1,
      hasVictimsInContactScheme = victimsInContactSchemeData(),
      indeterminateSentenceType = indeterminateSentenceType(),
      dateVloInformed = LocalDate.now(),
      alternativesToRecallTried = alternativesToRecallTried(),
      hasArrestIssues = arrestIssues(),
      hasContrabandRisk = contrabandRisk(),
      licenceConditionsBreached = licenceConditionsBreached(),
      underIntegratedOffenderManagement = integratedOffenderManagement(),
      indexOffenceDetails = "Offence details",
      localPoliceContact = localPoliceContact(),
      vulnerabilities = vulnerabilities(),
      convictionDetail = convictionDetail(),
      fixedTermAdditionalLicenceConditions = SelectedWithDetails(
        selected = true,
        "This is an additional licence condition",
      ),
      indeterminateOrExtendedSentenceDetails = indeterminateOrExtendedSentenceDetails(),
      mainAddressWherePersonCanBeFound = isMainAddressWherePersonCanBeFound(),
      whyConsideredRecall = whyConsideredRecall(),
      reasonsForNoRecall = reasonForNoRecall(),
      nextAppointment = nextAppointment(),
      hasBeenReviewed = reviewedPages(
        personOnProbationReviewed = true,
        convictionDetailReviewed = true,
        mappa = true,
      ),
      offenceAnalysis = "This is the offence analysis",
      previousReleases = previousReleases(),
      previousRecalls = previousRecalls(),
      recallConsideredList = recallConsideredData(),
      currentRoshForPartA = roshDataForPartA(),
      isUnder18 = false,
      isSentence12MonthsOrOver = false,
      isMappaLevelAbove1 = false,
      hasBeenConvictedOfSeriousOffence = false,
      isSentence48MonthsOrOver = false,
      isMappaCategory4 = false,
      isMappaLevel2Or3 = false,
      isRecalledOnNewChargedOffence = false,
      isServingFTSentenceForTerroristOffence = false,
      hasBeenChargedWithTerroristOrStateThreatOffence = false,
    )

    private fun personOnProbation(personOnProbation: PersonOnProbation?): PersonOnProbation? = personOnProbation?.copy(
      mappa = Mappa(
        level = 2,
        lastUpdatedDate = null,
        category = 2,
        hasBeenReviewed = true,
      ),
      hasBeenReviewed = true,
    )

    private fun recallConsideredData(): List<RecallConsidered> = listOf(
      RecallConsidered(
        id = 1,
        createdDate = "2022-07-26T09:48:27.443Z",
        userName = "Bill",
        recallConsideredDetail = "I have concerns about their behaviour",
        userId = "bill",
      ),
    )

    private fun vulnerabilities(): VulnerabilitiesRecommendation = VulnerabilitiesRecommendation(
      selected = listOf(
        ValueWithDetails(
          value = VulnerabilityOptions.RISK_OF_SUICIDE_OR_SELF_HARM.name,
          details = "Risk of suicide",
        ),
      ),
      allOptions = listOf(
        TextValueOption(
          value = VulnerabilityOptions.RISK_OF_SUICIDE_OR_SELF_HARM.name,
          text = "Risk of suicide or self harm",
        ),
        TextValueOption(value = VulnerabilityOptions.RELATIONSHIP_BREAKDOWN.name, text = "Relationship breakdown"),
      ),
    )

    private fun indeterminateOrExtendedSentenceDetails(): IndeterminateOrExtendedSentenceDetails = IndeterminateOrExtendedSentenceDetails(
      selected = listOf(
        ValueWithDetails(
          value = IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE.name,
          details = "behaviour similar to index offence",
        ),
      ),
      allOptions = listOf(
        TextValueOption(
          value = IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE.name,
          text = "behaviour similar to index offence",
        ),
        TextValueOption(
          value = IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE.name,
          text = "behaviour leading to sexual or violent behaviour",
        ),
        TextValueOption(
          value = IndeterminateOrExtendedSentenceDetailsOptions.OUT_OF_TOUCH.name,
          text = "out of touch",
        ),
      ),
    )

    private fun convictionDetail(): ConvictionDetail = ConvictionDetail(
      indexOffenceDescription = "This is the index offence",
      dateOfOriginalOffence = LocalDate.parse("2022-09-01"),
      dateOfSentence = LocalDate.parse("2022-09-02"),
      lengthOfSentence = 6,
      lengthOfSentenceUnits = "days",
      sentenceDescription = "CJA - Extended Sentence",
      licenceExpiryDate = LocalDate.parse("2022-09-03"),
      sentenceExpiryDate = LocalDate.parse("2022-09-04"),
      sentenceSecondLength = 12,
      sentenceSecondLengthUnits = "months",
    )

    private fun recallTypeData(recallTypeValue: RecallTypeValue?): RecallType? = if (recallTypeValue != null) {
      RecallType(
        selected = RecallTypeSelectedValue(value = recallTypeValue, details = "My details"),
        allOptions = listOf(
          TextValueOption(value = "NO_RECALL", text = "No recall"),
          TextValueOption(value = "FIXED_TERM", text = "Fixed term"),
          TextValueOption(value = "STANDARD", text = "Standard"),
        ),
      )
    } else {
      null
    }

    private fun custodyStatusData(): CustodyStatus = CustodyStatus(
      selected = CustodyStatusValue.YES_PRISON,
      details = "Bromsgrove Police Station\r\nLondon",
      allOptions = listOf(
        TextValueOption(value = "YES_PRISON", text = "Yes, prison custody"),
        TextValueOption(value = "YES_POLICE", text = "Yes, police custody"),
        TextValueOption(value = "NO", text = "No"),
      ),
    )

    private fun whyConsideredRecall(): WhyConsideredRecall = WhyConsideredRecall(
      selected = WhyConsideredRecallValue.RISK_INCREASED,
      allOptions = listOf(
        TextValueOption(value = "RISK_INCREASED", text = "Your risk is assessed as increased"),
        TextValueOption(value = "CONTACT_STOPPED", text = "Contact with your probation practitioner has broken down"),
        TextValueOption(
          value = "RISK_INCREASED_AND_CONTACT_STOPPED",
          text = "Your risk is assessed as increased and contact with your probation practitioner has broken down",
        ),
      ),
    )

    private fun victimsInContactSchemeData(): VictimsInContactScheme = VictimsInContactScheme(
      selected = YesNoNotApplicableOptions.YES,
      allOptions = listOf(
        TextValueOption(value = "YES", text = "Yes"),
        TextValueOption(value = "NO", text = "No"),
        TextValueOption(value = "NOT_APPLICABLE", text = "N/A"),
      ),
    )

    private fun indeterminateSentenceType(): IndeterminateSentenceType = IndeterminateSentenceType(
      selected = IndeterminateSentenceTypeOptions.LIFE,
      allOptions = listOf(
        TextValueOption(value = "LIFE", text = "Life sentence"),
        TextValueOption(value = "IPP", text = "Imprisonment for Public Protection (IPP) sentence"),
        TextValueOption(value = "DPP", text = "Detention for Public Protection (DPP) sentence"),
        TextValueOption(value = "NO", text = "No"),
      ),
    )

    private fun alternativesToRecallTried(): AlternativesToRecallTried = AlternativesToRecallTried(
      selected = listOf(
        ValueWithDetails(
          value = SelectedAlternativeOptions.WARNINGS_LETTER.name,
          details = "We sent a warning letter on 27th July 2022",
        ),
      ),
      allOptions = listOf(
        TextValueOption(
          value = SelectedAlternativeOptions.WARNINGS_LETTER.name,
          text = "Warnings/licence breach letters",
        ),
      ),
    )

    private fun arrestIssues(): SelectedWithDetails = SelectedWithDetails(selected = true, details = "Arrest issue details")

    private fun contrabandRisk(): SelectedWithDetails = SelectedWithDetails(selected = true, details = "Contraband risk details")

    private fun isMainAddressWherePersonCanBeFound(): SelectedWithDetails = SelectedWithDetails(selected = false, details = "123 Oak Avenue, Birmingham, B23 1AV")

    private fun licenceConditionsBreached(): LicenceConditionsBreached = LicenceConditionsBreached(
      standardLicenceConditions = StandardLicenceConditions(
        selected = listOf(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name),
        allOptions = listOf(
          TextValueOption(
            value = SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name,
            text = "They had good behaviour",
          ),
        ),
      ),
      additionalLicenceConditions = AdditionalLicenceConditions(
        selected = listOf("NST14"),
        allOptions = listOf(
          AdditionalLicenceConditionOption(
            subCatCode = "NST14",
            mainCatCode = "NLC5",
            title = "Additional title",
            details = "Additional details",
            note = "Additional note",
          ),
        ),
      ),
    )

    private const val ADDITIONAL_LICENCE_CODE = "123e4567-e89b-12d3-a456-426614174000"
    private const val BESPOKE_LICENCE_CODE = "223e4567-e89b-12d3-a456-426614174000"

    /**
     * Builds a set of licence conditions that have been breached
     *
     * @param useTransformedSelected
     *  -  If false (default), returns the uuid for the breached conditions
     *  -  If true, returns the description of the breached conditions
     */
    fun buildCvlLicenceConditionsBreached(
      useTransformedSelected: Boolean = false,
    ): CvlLicenceConditionsBreached {
      val selectedStandard = if (useTransformedSelected) {
        listOf("They had good behaviour")
      } else {
        listOf(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.cvlCode)
      }

      val selectedAdditional = if (useTransformedSelected) {
        listOf("Some Additional Licence Condition")
      } else {
        listOf(ADDITIONAL_LICENCE_CODE)
      }

      val selectedBespoke = if (useTransformedSelected) {
        listOf("Some Bespoke Licence Condition")
      } else {
        listOf(BESPOKE_LICENCE_CODE)
      }

      val standardLicenceConditionOption = LicenceConditionOption(
        code = SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.cvlCode,
        text = "They had good behaviour",
      )

      val additionalLicenceConditionOption = LicenceConditionOption(
        code = ADDITIONAL_LICENCE_CODE,
        text = "Some Additional Licence Condition",
      )

      val bespokeLicenceConditionOption = LicenceConditionOption(
        code = BESPOKE_LICENCE_CODE,
        text = "Some Bespoke Licence Condition",
      )

      return CvlLicenceConditionsBreached(
        standardLicenceConditions = LicenceConditionSection(
          selected = selectedStandard,
          allOptions = listOf(standardLicenceConditionOption),
        ),
        additionalLicenceConditions = LicenceConditionSection(
          selected = selectedAdditional,
          allOptions = listOf(additionalLicenceConditionOption),
        ),
        bespokeLicenceConditions = LicenceConditionSection(
          selected = selectedBespoke,
          allOptions = listOf(bespokeLicenceConditionOption),
        ),
      )
    }

    private fun localPoliceContact(): LocalPoliceContact = LocalPoliceContact(
      contactName = "John Doe",
      phoneNumber = "01234567890",
      faxNumber = "09876543210",
      emailAddress = "john.doe@gmail.com",
    )

    private fun integratedOffenderManagement(): UnderIntegratedOffenderManagement = UnderIntegratedOffenderManagement(
      selected = "YES",
      allOptions = listOf(
        TextValueOption(value = "YES", text = "Yes"),
        TextValueOption(value = "NO", text = "No"),
        TextValueOption(value = "NOT_APPLICABLE", text = "N/A"),
      ),
    )

    private fun nextAppointment(): NextAppointment = NextAppointment(
      HowWillAppointmentHappen(
        selected = NextAppointmentValue.TELEPHONE,
        allOptions = listOf(
          TextValueOption(text = "Telephone", value = "TELEPHONE"),
          TextValueOption(text = "Video call", value = "VIDEO_CALL"),
          TextValueOption(text = "Office visit", value = "OFFICE_VISIT"),
          TextValueOption(text = "Home visit", value = "HOME_VISIT"),
        ),
      ),
      dateTimeOfAppointment = "2022-04-24T20:39:00.000Z",
      probationPhoneNumber = "01238282838",
    )

    private fun previousReleases(): PreviousReleases = PreviousReleases(
      lastReleaseDate = LocalDate.parse("2022-09-02"),
      lastReleasingPrisonOrCustodialEstablishment = "HMP Holloway",
      hasBeenReleasedPreviously = true,
      previousReleaseDates = listOf(LocalDate.parse("2020-02-01")),
    )

    private fun previousRecalls(): PreviousRecalls = PreviousRecalls(
      lastRecallDate = LocalDate.parse("2022-08-02"),
      hasBeenRecalledPreviously = true,
      previousRecallDates = listOf(LocalDate.parse("2021-06-01")),
    )

    private fun reviewedPages(
      personOnProbationReviewed: Boolean,
      convictionDetailReviewed: Boolean,
      mappa: Boolean,
    ): HasBeenReviewed = HasBeenReviewed(
      personOnProbation = personOnProbationReviewed,
      convictionDetail = convictionDetailReviewed,
      mappa = mappa,
    )

    private fun reasonForNoRecall(): ReasonsForNoRecall = ReasonsForNoRecall(
      licenceBreach = "Reason for breaching licence",
      noRecallRationale = "Rationale for no recall",
      popProgressMade = "Progress made so far detail",
      futureExpectations = "Future expectations detail",
    )

    private fun roshDataForPartA(): RoshData = RoshData(
      riskToChildren = RoshDataScore.VERY_HIGH,
      riskToPublic = RoshDataScore.HIGH,
      riskToKnownAdult = RoshDataScore.MEDIUM,
      riskToStaff = RoshDataScore.LOW,
      riskToPrisoners = RoshDataScore.NOT_APPLICABLE,
    )

    private fun roshSummary(): RoshSummary = RoshSummary(
      riskOfSeriousHarm = RiskOfSeriousHarm(
        overallRisk = "HIGH",
        risks = listOf(
          RiskBreakdown(
            riskTo = "Children",
            community = "HIGH",
            custody = "LOW",
          ),
          RiskBreakdown(
            riskTo = "Public",
            community = "MEDIUM",
            custody = "MEDIUM",
          ),
          RiskBreakdown(
            riskTo = "Known Adult",
            community = "MEDIUM",
            custody = "MEDIUM",
          ),
          RiskBreakdown(
            riskTo = "Staff",
            community = "LOW",
            custody = "LOW",
          ),
          RiskBreakdown(
            riskTo = "Prisoners",
            community = "",
            custody = "VERY_HIGH",
          ),
        ),
      ),
      lastUpdatedDate = "2023-01-12T20:39:00.000Z",
    )
  }
}
