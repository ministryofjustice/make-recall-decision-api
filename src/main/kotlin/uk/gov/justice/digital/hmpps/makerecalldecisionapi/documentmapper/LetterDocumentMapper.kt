package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateTimeToDateWithSlashes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.text.SimpleDateFormat
import java.time.ZonedDateTime

abstract class LetterDocumentMapper : RecommendationDataToDocumentMapper() {

  fun mapRecommendationDataToLetterDocumentData(
    recommendation: RecommendationResponse,
    paragraph1: String?,
    paragraph2: String?,
    paragraph3: String?,
  ): DocumentData {
    val name = formatFullName(
      recommendation.personOnProbation?.firstName,
      null,
      recommendation.personOnProbation?.surname,
    )

    return DocumentData(
      salutation = buildSalutationName(name),
      letterTitle = buildLetterTitle(),
      letterDate = convertLocalDateTimeToDateWithSlashes(recommendation.lastDntrLetterDownloadDateTime),
      signedByParagraph = buildSignedByParagraph(),
      letterAddress = getLetterAddressDetails(recommendation.personOnProbation?.addresses, name),
      section1 = paragraph1,
      section2 = paragraph2,
      section3 = paragraph3,
    )
  }

  fun generateLongDateAndTime(dateTimeString: String?): String {
    val formattedDateTime = if (dateTimeString == null) {
      MrdTextConstants.EMPTY_STRING
    } else {
      val dateTime = ZonedDateTime.parse(dateTimeString)
      val month = getAbbreviatedFromDateTime(dateTime, "MMMM")
      val dayOfWeek = getAbbreviatedFromDateTime(dateTime, "EEEE")
      val year = getAbbreviatedFromDateTime(dateTime, "yyyy")
      val dayOfMonth = getAbbreviatedFromDateTime(dateTime, "d")
      val hour = getAbbreviatedFromDateTime(dateTime, "h")
      val minute = getAbbreviatedFromDateTime(dateTime, "mm")
      val ampm = getAbbreviatedFromDateTime(dateTime, "a")

      "$dayOfWeek $dayOfMonth${getDayOfMonthSuffix(Integer.valueOf(dayOfMonth))} $month $year at $hour:$minute${ampm?.lowercase()}"
    }
    return formattedDateTime
  }

  fun getDayOfMonthSuffix(n: Int): String? = if (n in 11..13) {
    "th"
  } else {
    when (n % 10) {
      1 -> "st"
      2 -> "nd"
      3 -> "rd"
      else -> "th"
    }
  }

  private fun getAbbreviatedFromDateTime(dateTime: ZonedDateTime, field: String): String? {
    val output = SimpleDateFormat(field)
    return output.format(dateTime.toInstant().toEpochMilli())
  }

  fun nextAppointmentBy(recommendation: RecommendationResponse): String? {
    val selected = recommendation.nextAppointment?.howWillAppointmentHappen?.selected
    return recommendation.nextAppointment?.howWillAppointmentHappen?.allOptions
      ?.filter { it.value == selected?.name }
      ?.map { it.text?.lowercase() }
      ?.first()
  }

  private fun getLetterAddressDetails(addresses: List<Address>?, name: String?): String {
    val mainAddresses = addresses?.filter { !it.noFixedAbode }

    return if (mainAddresses?.isNotEmpty() == true && mainAddresses.size == 1) {
      val addressConcat = mainAddresses.map {
        it.separatorFormattedAddress("\n", true, name)
      }
      addressConcat[0]
    } else {
      ""
    }
  }

  private fun buildSalutationName(popName: String?): String = "Dear $popName,"

  abstract fun buildLetterTitle(): String

  private fun buildSignedByParagraph(): String? = "Yours sincerely,\n\n\nProbation Practitioner/Senior Probation Officer/Head of PDU"
}
