package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate.converter.mappa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NOT_APPLICABLE

class MappaConverterTest {

  @Nested
  @DisplayName("formatMappaCategory method")
  inner class MappaCategory {

    @Test
    fun `returns NA string when mappa is null`() {
      val result = MappaConverter().formatMappaCategory(null)
      assertThat(result).isEqualTo(NOT_APPLICABLE)
    }

    @Test
    fun `returns NA string when mappa category is null`() {
      val result = MappaConverter().formatMappaCategory(mappa(category = null))
      assertThat(result).isEqualTo(NOT_APPLICABLE)
    }

    @Test
    fun `returns category when mappa category is not null`() {
      val mappa = mappa()
      val result = MappaConverter().formatMappaCategory(mappa)
      assertThat(result).isEqualTo("Category ${mappa.category}")
    }
  }

  @Nested
  @DisplayName("formatMappaLevel method")
  inner class MappaLevel {

    @Test
    fun `returns NA string when mappa is null`() {
      val result = MappaConverter().formatMappaLevel(null)
      assertThat(result).isEqualTo(NOT_APPLICABLE)
    }

    @Test
    fun `returns NA string when mappa level is null`() {
      val result = MappaConverter().formatMappaLevel(mappa(level = null))
      assertThat(result).isEqualTo(NOT_APPLICABLE)
    }

    @Test
    fun `returns level when mappa level is not null`() {
      val mappa = mappa()
      val result = MappaConverter().formatMappaLevel(mappa)
      assertThat(result).isEqualTo("Level ${mappa.level}")
    }
  }
}
