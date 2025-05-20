package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PrisonOffender
import java.time.LocalDate

class OffenderTest {

  @Test
  fun `toPrisonOffender conversion - standard`() {
    val offenderInput = Offender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      physicalAttributes = PhysicalAttributes("Male", "White"),
      identifiers = listOf(Identifier("CRO", "croVal"), Identifier("PNC", "pncVal")),
      sentenceDetail = SentenceDetail(LocalDate.parse("2000-03-15")),
    )

    var prisonOffenderOutput = PrisonOffender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      ethnicity = "White",
      gender = "Male",
      cro = "croVal",
      pnc = "pncVal",
    )

    val result = offenderInput.toPrisonOffender()
    assertThat(result).isEqualTo(prisonOffenderOutput)
  }

  @Test
  fun `toPrisonOffender conversion -  physicalAttributes null`() {
    val offenderInput = Offender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      physicalAttributes = null,
      identifiers = listOf(Identifier("CRO", "croVal"), Identifier("PNC", "pncVal")),
      sentenceDetail = SentenceDetail(LocalDate.parse("2000-03-15")),
    )

    var prisonOffenderOutput = PrisonOffender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      ethnicity = null,
      gender = null,
      cro = "croVal",
      pnc = "pncVal",
    )

    val result = offenderInput.toPrisonOffender()
    assertThat(result).isEqualTo(prisonOffenderOutput)
  }

  @Test
  fun `toPrisonOffender conversion -  identifiers null`() {
    val offenderInput = Offender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      physicalAttributes = null,
      identifiers = null,
      sentenceDetail = SentenceDetail(LocalDate.parse("2000-03-15")),
    )

    var prisonOffenderOutput = PrisonOffender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      ethnicity = null,
      gender = null,
      cro = null,
      pnc = null,
    )

    val result = offenderInput.toPrisonOffender()
    assertThat(result).isEqualTo(prisonOffenderOutput)
  }

  @Test
  fun `toPrisonOffender conversion -  identifiers not null but CRO not present`() {
    val offenderInput = Offender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      physicalAttributes = null,
      identifiers = listOf(Identifier("NOTCRO", "croVal"), Identifier("PNC", "pncVal")),
      sentenceDetail = SentenceDetail(LocalDate.parse("2000-03-15")),
    )

    var prisonOffenderOutput = PrisonOffender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      ethnicity = null,
      gender = null,
      cro = null,
      pnc = "pncVal",
    )

    val result = offenderInput.toPrisonOffender()
    assertThat(result).isEqualTo(prisonOffenderOutput)
  }

  @Test
  fun `toPrisonOffender conversion -  identifiers not null but PNC not present`() {
    val offenderInput = Offender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      physicalAttributes = null,
      identifiers = listOf(Identifier("CRO", "croVal"), Identifier("notPNC", "pncVal")),
      sentenceDetail = SentenceDetail(LocalDate.parse("2000-03-15")),
    )

    var prisonOffenderOutput = PrisonOffender(
      image = "123",
      status = "ACTIVE IN",
      lastName = "Bloggs",
      bookingNo = "7878783",
      firstName = "Joe",
      middleName = "J",
      dateOfBirth = LocalDate.parse("1970-03-15"),
      agencyId = "BRX",
      facialImageId = 321,
      locationDescription = "Outside - released from Leeds",
      ethnicity = null,
      gender = null,
      cro = "croVal",
      pnc = null,
    )

    val result = offenderInput.toPrisonOffender()
    assertThat(result).isEqualTo(prisonOffenderOutput)
  }
}
