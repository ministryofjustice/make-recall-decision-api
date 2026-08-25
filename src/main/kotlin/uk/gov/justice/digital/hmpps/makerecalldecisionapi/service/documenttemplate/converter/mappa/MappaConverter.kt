package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate.converter.mappa

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants

@Service
internal class MappaConverter {

  fun formatMappaCategory(mappa: Mappa?): String = if (mappa?.category == null) {
    MrdTextConstants.NOT_APPLICABLE
  } else {
    "Category${MrdTextConstants.WHITE_SPACE}${(mappa.category)}"
  }

  fun formatMappaLevel(mappa: Mappa?): String = if (mappa?.level == null) {
    MrdTextConstants.NOT_APPLICABLE
  } else {
    "Level${MrdTextConstants.WHITE_SPACE}${(mappa.level)}"
  }
}
