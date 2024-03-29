package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.ConvictionWithLicenceConditions
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class LicenceConditionsCoordinatorTest : ServiceTestBase() {

  private val licenceConditionsCoordinator = LicenceConditionsCoordinator()

  @Test
  fun `case is not on licence in ND`() {
    // given
    val notOnLicenceCode = "BLA" // anything except 'B'

    // and
    val singleActiveCustodialConvictionNotOnLicence =
      activeConviction(isCustodial = true, custodialStatusCode = notOnLicenceCode).withLicenceConditions(emptyList())

    // and
    val noDeliusActiveLicenceConditions =
      deliusLicenceConditionsResponse(listOf(singleActiveCustodialConvictionNotOnLicence))
    val anyCvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse()

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = false,
      ndeliusActiveCustodialConvictions = listOf(singleActiveCustodialConvictionNotOnLicence),
      cvlLicenceCondition = null,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = noDeliusActiveLicenceConditions,
      cvlLicenceConditions = anyCvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `no active convictions`() {
    // given
    val noDeliusActiveLicenceConditions = deliusLicenceConditionsResponse(emptyList())
    val anyCvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse()

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = false,
      ndeliusActiveCustodialConvictions = emptyList(),
      cvlLicenceCondition = null,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = noDeliusActiveLicenceConditions,
      cvlLicenceConditions = anyCvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `no CVL licence start dates`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = null,
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(
      listOf(
        activeConviction(
          isCustodial = true,
          custodialStatusCode = onLicenceStatusCode,
        ).withLicenceConditions(licenceConditions),
      ),
    )

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = listOf(
        activeConviction(
          isCustodial = true,
          custodialStatusCode = onLicenceStatusCode,
        ).withLicenceConditions(licenceConditions),
      ),
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `ND licences with both 'on licence' and 'not on licence' custodialStatusCodes present`() {
    // given
    val onLicenceStatusCode = "B"
    val notOnLicenceStatusCode = "BLA" // anything except 'B'

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = true,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2020-06-14"))),
      activeConviction(
        isCustodial = true,
        custodialStatusCode = notOnLicenceStatusCode,
      ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("3020-06-14"))),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = false,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = activeConvictions,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `ND licences with both 'custodial' and 'non-custodial' convictions present`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val custodialActiveConviction = activeConviction(
      isCustodial = true,
      custodialStatusCode = onLicenceStatusCode,
    ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("3020-06-14")))
    val nonCustodialActiveConviction = activeConviction(
      isCustodial = false,
      custodialStatusCode = onLicenceStatusCode,
    ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2020-06-14")))
    val activeConvictions = listOf(nonCustodialActiveConviction, custodialActiveConviction)
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = listOf(custodialActiveConviction),
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `ND licences with both 'custodial' and 'non-custodial' convictions present and null on non-custodial conviction custodialStatusCode`() {
    // given
    val nullCustodialOnLicenceStatusCode = null
    val custodialStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val custodialActiveConviction = activeConviction(
      isCustodial = true,
      custodialStatusCode = custodialStatusCode,
    ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("3020-06-14")))
    val nonCustodialActiveConviction = activeConviction(
      isCustodial = false,
      custodialStatusCode = nullCustodialOnLicenceStatusCode,
    ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2020-06-14")))
    val activeConvictions = listOf(nonCustodialActiveConviction, custodialActiveConviction)
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = listOf(custodialActiveConviction),
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `ND licences with both 'custodial' and 'non-custodial' convictions present and null on custodial conviction custodialStatusCode`() {
    // given
    val nullCustodialOnLicenceStatusCode = null
    val custodialStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val custodialActiveConviction = activeConviction(
      isCustodial = true,
      custodialStatusCode = nullCustodialOnLicenceStatusCode,
    ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("3020-06-14")))
    val nonCustodialActiveConviction = activeConviction(
      isCustodial = false,
      custodialStatusCode = custodialStatusCode,
    ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2020-06-14")))
    val activeConvictions = listOf(nonCustodialActiveConviction, custodialActiveConviction)
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = false,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = listOf(custodialActiveConviction),
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `ND licences with both 'custodial' and 'non-custodial' convictions present and null on both conviction custodialStatusCodes`() {
    // given
    val nullCustiodialOnLicenceStatusCode = null
    val custodialStatusCode = null

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val custodialActiveConviction = activeConviction(
      isCustodial = true,
      custodialStatusCode = custodialStatusCode,
    ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("3020-06-14")))
    val nonCustodialActiveConviction = activeConviction(
      isCustodial = false,
      custodialStatusCode = nullCustiodialOnLicenceStatusCode,
    ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2020-06-14")))
    val activeConvictions = listOf(nonCustodialActiveConviction, custodialActiveConviction)
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = false,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = listOf(custodialActiveConviction),
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `no ND start dates`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = true,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(licenceConditions),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = activeConvictions,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `missing sentence data in ND`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val activeConvictionsWithMissingSentenceData = listOf(
      ConvictionWithLicenceConditions(
        sentence = null,
        mainOffence = DeliusClient.Offence(null, "", ""),
        licenceConditions = emptyList(),
        additionalOffences = emptyList(),
      ),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(
      listOf(
        activeConviction(
          isCustodial = true,
          custodialStatusCode = onLicenceStatusCode,
        ).withLicenceConditions(licenceConditions),
      ),
    ).copy(activeConvictions = activeConvictionsWithMissingSentenceData)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = false,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = emptyList(),
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `no custodial ND convictions`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = false,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(licenceConditions),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = false,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = emptyList(),
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `ND version returned when it has a later start date`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2020-06-14"),
    )
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = true,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2022-06-14"))),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = activeConvictions,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `no active Cvl licence conditions available`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "NOT_ACTIVE",
      licenceStartDate = LocalDate.parse("2020-06-14"),
    )
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = true,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(licenceConditions),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = activeConvictions,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `ND and CVL have same licence start dates`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14"),
    )
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = true,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2022-06-14"))),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = cvlActiveLicenceConditions.first(),
      ndeliusActiveCustodialConvictions = activeConvictions,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `CVL has a later licence start date`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2028-06-14"),
    )
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = true,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2026-06-14"))),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = cvlActiveLicenceConditions.first(),
      ndeliusActiveCustodialConvictions = activeConvictions,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `No CVL Data and ND response missing licence conditions`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = true,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(emptyList()),
    )
    val deliusResponseWithoutLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = null,
      ndeliusActiveCustodialConvictions = activeConvictions,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusResponseWithoutLicenceConditions,
      cvlLicenceConditions = emptyList(),
    )

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `Two CVL licences present with different start dates, one earlier and one later than the ND dates`() {
    // given
    val onLicenceStatusCode = "B"

    // and
    val cvlActiveLicenceConditions = expectedMultipleCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate1 = LocalDate.parse("2028-06-14"),
      licenceStartDate2 = LocalDate.parse("1980-06-14"),
    )
    val activeConvictions = listOf(
      activeConviction(
        isCustodial = true,
        custodialStatusCode = onLicenceStatusCode,
      ).withLicenceConditions(deliusLicenceConditions(LocalDate.parse("2026-06-14"))),
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(activeConvictions)

    // and
    val expected = SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceCondition = cvlActiveLicenceConditions.first(),
      ndeliusActiveCustodialConvictions = activeConvictions,
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(
      nDeliusLicenceConditions = deliusLicenceConditions,
      cvlLicenceConditions = cvlActiveLicenceConditions,
    )

    // then
    assertThat(actual, equalTo(expected))
  }
}
