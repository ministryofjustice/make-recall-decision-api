package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.jpa.repository

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.ServerSocket


object PostgresContainer {
  val instance: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }
  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? = if (checkPostgresRunning().not()) {
    PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withExposedPorts(5432)
      withDatabaseName("make_recall_decision")
      withUsername("admin")
      withPassword("admin_password")
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)
      start()
    }
  } else {
    null
  }

  private fun checkPostgresRunning(): Boolean = try {
    val serverSocket = ServerSocket(5432)
    serverSocket.localPort == 0
  } catch (e: IOException) {
    true
  }
}