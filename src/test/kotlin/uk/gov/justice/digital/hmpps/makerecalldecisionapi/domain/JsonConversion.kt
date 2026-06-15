package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.mockserver.model.JsonBody.json

private val customMapper = ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  .registerModule(JavaTimeModule())
  .registerKotlinModule()

internal fun toJsonString(value: Any?): String = customMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value)

internal fun toJsonBody(value: Any) = json(toJsonString(value))
