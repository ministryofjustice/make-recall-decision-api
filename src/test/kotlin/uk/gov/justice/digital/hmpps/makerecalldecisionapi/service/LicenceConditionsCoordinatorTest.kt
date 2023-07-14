package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class LicenceConditionsCoordinatorTest : ServiceTestBase() {

  private val licenceConditionsCoordinator = LicenceConditionsCoordinator()

  @Test
  fun `AC 1 - case is not on licence in ND`() {
    // given
    val singleActiveCustodialConvictionNotOnLicence = custodialConviction(isCustodial = true, releasedOnLicence = false).withLicenceConditions(emptyList())

    // and
    val noDeliusActiveLicenceConditions = deliusLicenceConditionsResponse(listOf(singleActiveCustodialConvictionNotOnLicence))
    val anyCvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse()

    // and
    val expected = SelectedLicenceConditions(
      source = "nDelius",
      hasAllConvictionsReleasedOnLicence = false,
      ndeliusLicenceConditions = noDeliusActiveLicenceConditions,
      cvlLicenceConditions = anyCvlActiveLicenceConditions
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(nDeliusLicenceConditions = noDeliusActiveLicenceConditions, cvlLicenceConditions = anyCvlActiveLicenceConditions)

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `AC 2 - CVL version returned when CVL licence status is ACTIVE and CVL licence start date is later`() {
    // given
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14")
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(
      listOf(
        custodialConviction(
          isCustodial = true,
          releasedOnLicence = true,
          licenceStartDate = LocalDate.parse("2020-06-14")
        ).withLicenceConditions(licenceConditions)
      )
    )

    // and
    val expected = SelectedLicenceConditions(
      source = "cvl",
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceConditions = cvlActiveLicenceConditions,
      ndeliusLicenceConditions = deliusLicenceConditions
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(nDeliusLicenceConditions = deliusLicenceConditions, cvlLicenceConditions = cvlActiveLicenceConditions)

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `AC 3 - NDelius version returned when it has a later start date`() {
    // given
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2020-06-14")
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(
      listOf(
        custodialConviction(
          isCustodial = true,
          releasedOnLicence = true,
          licenceStartDate = LocalDate.parse("2022-06-14")
        ).withLicenceConditions(licenceConditions)
      )
    )

    // and
    val expected = SelectedLicenceConditions(
      source = "nDelius",
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceConditions = cvlActiveLicenceConditions,
      ndeliusLicenceConditions = deliusLicenceConditions
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(nDeliusLicenceConditions = deliusLicenceConditions, cvlLicenceConditions = cvlActiveLicenceConditions)

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `AC 3 - NDelius version returned when no active Cvl licence conditions available`() {
    // given
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "NOT_ACTIVE",
      licenceStartDate = LocalDate.parse("2020-06-14")
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(
      listOf(
        custodialConviction(
          isCustodial = true,
          releasedOnLicence = true,
          licenceStartDate = LocalDate.parse("2022-06-14")
        ).withLicenceConditions(licenceConditions)
      )
    )

    // and
    val expected = SelectedLicenceConditions(
      source = "nDelius",
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceConditions = cvlActiveLicenceConditions,
      ndeliusLicenceConditions = deliusLicenceConditions
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(nDeliusLicenceConditions = deliusLicenceConditions, cvlLicenceConditions = cvlActiveLicenceConditions)

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `AC 4 - Return Cvl when both have same licence start dates`() {
    // given
    val cvlActiveLicenceConditions = expectedCvlLicenceConditionsResponse(
      licenceStatus = "ACTIVE",
      licenceStartDate = LocalDate.parse("2022-06-14")
    )
    val deliusLicenceConditions = deliusLicenceConditionsResponse(
      listOf(
        custodialConviction(
          isCustodial = true,
          releasedOnLicence = true,
          licenceStartDate = LocalDate.parse("2022-06-14")
        ).withLicenceConditions(licenceConditions)
      )
    )

    // and
    val expected = SelectedLicenceConditions(
      source = "cvl",
      hasAllConvictionsReleasedOnLicence = true,
      cvlLicenceConditions = cvlActiveLicenceConditions,
      ndeliusLicenceConditions = deliusLicenceConditions
    )

    // when
    val actual = licenceConditionsCoordinator.selectLicenceConditions(nDeliusLicenceConditions = deliusLicenceConditions, cvlLicenceConditions = cvlActiveLicenceConditions)

    // then
    assertThat(actual, equalTo(expected))
  }
}
