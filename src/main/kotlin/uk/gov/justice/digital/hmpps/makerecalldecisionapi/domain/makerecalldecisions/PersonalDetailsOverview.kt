package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonFormat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.formatFullName
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.joinToString
import java.time.LocalDate

data class PersonalDetailsOverview(
  val fullName: String?,
  val name: String?,
  val firstName: String?,
  val middleNames: String?,
  val surname: String?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val age: Int?,
  val gender: String?,
  val crn: String?,
  val ethnicity: String?,
  val croNumber: String?,
  val mostRecentPrisonerNumber: String?,
  val pncNumber: String?,
  val nomsNumber: String?,
  val primaryLanguage: String?,
)

fun DeliusClient.PersonalDetailsOverview.toOverview(crn: String) = PersonalDetailsOverview(
  crn = crn,
  fullName = formatFullName(name.forename, name.middleName, name.surname),
  name = joinToString(name.forename, name.surname),
  firstName = name.forename,
  middleNames = name.middleName,
  surname = name.surname,
  dateOfBirth = dateOfBirth,
  age = dateOfBirth.until(LocalDate.now())?.years,
  gender = gender,
  ethnicity = ethnicity ?: "",
  primaryLanguage = primaryLanguage ?: "",
  croNumber = identifiers.croNumber ?: "",
  pncNumber = identifiers.pncNumber ?: "",
  nomsNumber = identifiers.nomsNumber ?: "",
  mostRecentPrisonerNumber = identifiers.bookingNumber ?: "",
)
