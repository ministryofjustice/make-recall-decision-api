package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter
import org.springframework.util.MultiValueMap


class UserAwareEntityConverter : OAuth2ClientCredentialsGrantRequestEntityConverter() {
  /**
   * This method relies on static fields managed by the Spring framework which can't be easily mocked and tested, it
   * also relies on implicit guarantees about the values returned by request.getBody(). In an effort to mitigate some
   * of this risk the class has been coded defensively with fine grained exception handling.
   */
  fun enhanceWithUsername(grantRequest: OAuth2ClientCredentialsGrantRequest?, username: String?): RequestEntity<*> {
    if (grantRequest == null) {
   //   throw UnableToGetTokenOnBehalfOfUserException(String.format("Unexpected condition - cannot add username '%s' to formParameters as incoming request is null", username))
    }
    val request = super.convert(grantRequest)
    var formParameters: MultiValueMap<String, Any?>? = null
    if (request == null) {
   //   throw UnableToGetTokenOnBehalfOfUserException(String.format("Unexpected condition - cannot add username '%s' to formParameters as request returned by super is null", username))
    }
    if (request.type == null || request.type == MultiValueMap::class.java) {
    //  throw UnableToGetTokenOnBehalfOfUserException(String.format("Unexpected condition - cannot add username '%s' to formParameters as request type is %s", username, request.type))
    }
    try {
      formParameters = request.body as MultiValueMap<String, Any?>
      formParameters.add("username", username)
    } catch (exception: ClassCastException) {
      val message = String.format("Unexpected condition - exception thrown when adding username '%s' to formParameters", username)
     // throw UnableToGetTokenOnBehalfOfUserException(message, exception)
    }
    return RequestEntity(formParameters, request.headers, HttpMethod.POST, request.url)
  }
}