package uk.gov.justice.digital.hmpps.makerecalldecisionapi.utils

fun String.removeAllCrLf() =
  replace("\r", "")
    .replace("\n", "")
