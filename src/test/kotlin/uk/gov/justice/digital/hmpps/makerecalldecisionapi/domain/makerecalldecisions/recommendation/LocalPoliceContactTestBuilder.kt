package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun localPoliceContact(
  contactName: String? = randomString(),
  emailAddress: String? = randomString(),
) = LocalPoliceContact(
  contactName = contactName,
  emailAddress = emailAddress,
)
