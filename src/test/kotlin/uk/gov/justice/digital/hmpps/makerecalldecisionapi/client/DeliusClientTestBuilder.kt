package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactHistory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactHistory.Contact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactHistory.Contact.DocumentReference
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactHistory.Contact.Type
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactHistory.ContactSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactTypeSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Name
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.PersonalDetailsOverview
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.PersonalDetailsOverview.Identifiers
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomZonedDateTime
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Helper functions for generating instances of DeliusCLient data classes with
 * their fields pre-filled with random values. Intended for use in unit tests.
 */

internal fun name(
  forename: String = randomString(),
  middleName: String? = randomString(),
  surname: String = randomString(),
) = Name(
  forename,
  middleName,
  surname,
)

internal fun personalDetailsOverview(
  name: Name = name(),
  identifiers: Identifiers = identifiers(),
  dateOfBirth: LocalDate = randomLocalDate(),
  gender: String = randomString(),
  ethnicity: String? = randomString(),
  primaryLanguage: String? = randomString(),
) = PersonalDetailsOverview(
  name,
  identifiers,
  dateOfBirth,
  gender,
  ethnicity,
  primaryLanguage,
)

internal fun identifiers(
  crn: String = randomString(),
  pncNumber: String? = randomString(),
  croNumber: String? = randomString(),
  nomsNumber: String? = randomString(),
  bookingNumber: String? = randomString(),
) = Identifiers(
  crn,
  pncNumber,
  croNumber,
  nomsNumber,
  bookingNumber,
)

internal fun contactHistory(
  personalDetails: PersonalDetailsOverview = personalDetailsOverview(),
  contacts: List<Contact> = listOf(contact()),
  summary: ContactSummary = contactSummary(),
) = ContactHistory(
  personalDetails,
  contacts,
  summary,
)

internal fun ContactHistory.toJsonString(): String = ResourceLoader.CustomMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

internal fun contact(
  description: String? = randomString(),
  documents: List<DocumentReference> = listOf(documentReference()),
  enforcementAction: String? = randomString(),
  notes: String? = randomString(),
  outcome: String? = randomString(),
  sensitive: Boolean? = randomBoolean(),
  startDateTime: ZonedDateTime = randomZonedDateTime(),
  type: Type = type(),
) = Contact(
  description,
  documents,
  enforcementAction,
  notes,
  outcome,
  sensitive,
  startDateTime,
  type,
)

internal fun type(
  code: String = randomString(),
  description: String = randomString(),
  systemGenerated: Boolean = randomBoolean(),
) = Type(
  code,
  description,
  systemGenerated,
)

internal fun documentReference(
  id: String = randomString(),
  name: String = randomString(),
  lastUpdated: ZonedDateTime = randomZonedDateTime(),
) = DocumentReference(
  id,
  name,
  lastUpdated,
)

internal fun contactSummary(
  types: List<ContactTypeSummary> = listOf(contactTypeSummary()),
  hits: Int = randomInt(),
  total: Int = randomInt(),
) = ContactSummary(
  types,
  hits,
  total,
)

internal fun contactTypeSummary(
  code: String = randomString(),
  description: String = randomString(),
  total: Int = randomInt(),
) = ContactTypeSummary(
  code,
  description,
  total,
)
