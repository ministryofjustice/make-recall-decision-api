package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.springframework.stereotype.Component
import java.lang.ThreadLocal

@Component
object UserContext {
  var authToken = ThreadLocal<String>()

  fun setAuthToken(aToken: String) {
    authToken.set(aToken)
  }

  fun getAuthToken(): String {
    return authToken.get()
  }
}
