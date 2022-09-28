package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToDateWithSlashes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZonedDateTime

abstract class LetterDocumentMapper : RecommendationDataToDocumentMapper() {

  fun mapRecommendationDataToLetterDocumentData(recommendation: RecommendationEntity, paragraph1: String?, paragraph2: String?, paragraph3: String?): DocumentData {
    val name = formatFullName(
      recommendation.data.personOnProbation?.firstName,
      null,
      recommendation.data.personOnProbation?.surname
    )

    return DocumentData(
      salutation = buildSalutationName(name),
      letterTitle = buildLetterTitle(),
      letterDate = convertLocalDateToDateWithSlashes(LocalDate.now()),
      signedByParagraph = buildSignedByParagraph(),
      letterAddress = getLetterAddressDetails(recommendation.data.personOnProbation?.addresses, name),
      section1 = paragraph1,
      section2 = paragraph2,
      section3 = paragraph3
    )
  }

  fun generateLongDateAndTime(dateTimeString: String?): String {
    val formattedDateTime = if (dateTimeString == null) {
      MrdTextConstants.EMPTY_STRING
    } else {
      val dateTime = ZonedDateTime.parse(dateTimeString)
      val month = getAbbreviatedFromDateTime(dateTime, "MMMM")
      val dayOfWeek = getAbbreviatedFromDateTime(dateTime, "EEEE")
      val year = getAbbreviatedFromDateTime(dateTime, "YYYY")
      val dayOfMonth = getAbbreviatedFromDateTime(dateTime, "dd")
      val hour = getAbbreviatedFromDateTime(dateTime, "h")
      val minute = getAbbreviatedFromDateTime(dateTime, "mm")
      val ampm = getAbbreviatedFromDateTime(dateTime, "a")

      // FIXME: add ordinal number to the day part of the formatted date
      "$dayOfWeek $dayOfMonth $month $year at $hour:$minute${ampm?.lowercase()}"
    }
    return formattedDateTime
  }

  private fun getAbbreviatedFromDateTime(dateTime: ZonedDateTime, field: String): String? {
    val output = SimpleDateFormat(field)
    return output.format(dateTime.toInstant().toEpochMilli())
  }

  fun nextAppointmentBy(recommendation: RecommendationEntity): String? {
    val selected = recommendation.data.nextAppointment?.howWillAppointmentHappen?.selected
    return recommendation.data.nextAppointment?.howWillAppointmentHappen?.allOptions
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
    } else ""
  }

  private fun buildSalutationName(popName: String?): String {
    return "Dear $popName,"
  }

  abstract fun buildLetterTitle(): String

  private fun buildSignedByParagraph(): String? {
    return "Yours sincerely,\n\n\nProbation Practitioner/Senior Probation Officer/Head of PDU"
  }
}
