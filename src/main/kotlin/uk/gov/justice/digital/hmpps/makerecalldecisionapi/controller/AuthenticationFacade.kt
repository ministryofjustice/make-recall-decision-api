package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade {

  val authentication: Authentication
    get() = SecurityContextHolder.getContext().authentication

  val currentUsername: String?
    get() {
      val userPrincipal = userPrincipal
      return if (userPrincipal is String) {
        userPrincipal
      } else {
        null
      }
    }

  val currentNameOfUser: String?
    get() {
      val credentials = authentication.credentials as Jwt
      return if (credentials.claims is Map<*, *>) {
        credentials.claims["name"] as String?
      } else {
        null
      }
    }

  val currentUserId: String?
    get() {
      val credentials = authentication.credentials as Jwt
      return if (credentials.claims is Map<*, *>) {
        credentials.claims["user_id"] as String?
      } else {
        null
      }
    }

  private val userPrincipal: Any?
    get() {
      val auth = authentication
      return auth.principal
    }
}
