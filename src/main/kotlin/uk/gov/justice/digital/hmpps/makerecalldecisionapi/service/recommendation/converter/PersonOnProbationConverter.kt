package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.converter

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.formatFullName
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbationDto

@Service
class PersonOnProbationConverter {

  /**
   * Converts a PersonOnProbation to a PersonOnProbationDto.
   */
  fun convert(personOnProbation: PersonOnProbation): PersonOnProbationDto {
    val firstName = personOnProbation.firstName
    val middleNames = personOnProbation.middleNames
    val surname = personOnProbation.surname
    return PersonOnProbationDto(
      fullName = formatFullName(firstName, middleNames, surname),
      name = personOnProbation.name,
      firstName = firstName,
      surname = surname,
      middleNames = middleNames,
      gender = personOnProbation.gender,
      ethnicity = personOnProbation.ethnicity,
      dateOfBirth = personOnProbation.dateOfBirth,
      croNumber = personOnProbation.croNumber,
      mostRecentPrisonerNumber = personOnProbation.mostRecentPrisonerNumber,
      nomsNumber = personOnProbation.nomsNumber,
      pncNumber = personOnProbation.pncNumber,
      mappa = personOnProbation.mappa,
      addresses = personOnProbation.addresses,
      primaryLanguage = personOnProbation.primaryLanguage,
      hasBeenReviewed = personOnProbation.hasBeenReviewed,
    )
  }
}
