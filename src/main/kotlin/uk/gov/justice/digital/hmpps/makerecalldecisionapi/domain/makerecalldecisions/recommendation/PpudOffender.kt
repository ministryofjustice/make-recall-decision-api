package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreType
import java.io.Serializable

data class PpudOffender(
  val id: String,
  val croOtherNumber: String,
  val dateOfBirth: String,
  val ethnicity: String,
  val familyName: String,
  val firstNames: String,
  val gender: String,
  val immigrationStatus: String,
  // TODO MRD-2693 make establishment field mandatory
  val establishment: String?,
  val nomsId: String,
  val prisonerCategory: String,
  val prisonNumber: String,
  val sentences: List<PpudSentence>,
  val status: String,
  val youngOffender: String,
) : Serializable

data class PpudSentence(
  val id: String?,
  // deprecated
  val offenceDescription: String?,
  val sentenceExpiryDate: String?,
  val dateOfSentence: String,
  val custodyType: String,
  val mappaLevel: String?,
  val licenceExpiryDate: String?,
  val tariffExpiryDate: String?,
  val offence: PpudOffence?,
  val releaseDate: String?,
  // deprecated
  @JsonIgnore
  val releases: List<PpudRelease>?,
  val sentenceLength: PpudSentenceLength?,
  val sentencingCourt: String?,
) : Serializable

data class PpudOffence(
  val indexOffence: String?,
  val dateOfIndexOffence: String?,
  val indexOffenceComment: String?,
) : Serializable

// deprecated
@JsonIgnoreType
data class PpudRelease(
  val category: String?,
  val dateOfRelease: String?,
  val releasedFrom: String?,
  val releasedUnder: String?,
  val releaseType: String?,
) : Serializable

data class PpudSentenceLength(
  val partYears: Int?,
  val partMonths: Int?,
  val partDays: Int?,
) : Serializable
