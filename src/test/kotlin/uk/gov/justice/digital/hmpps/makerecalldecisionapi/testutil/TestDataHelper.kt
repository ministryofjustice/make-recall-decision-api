package uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil

import java.util.*

class TestDataHelper {

  companion object {
    fun randomString(): String {
      return UUID.randomUUID().toString()
    }
  }
}