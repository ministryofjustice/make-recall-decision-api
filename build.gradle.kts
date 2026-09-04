import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.6"
  kotlin("jvm") version "2.4.10"
  id("org.unbroken-dome.test-sets") version "4.1.0"
  kotlin("plugin.jpa") version "2.4.10"
  kotlin("plugin.spring") version "2.4.10"
  kotlin("plugin.serialization") version "2.4.10"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
  testRuntimeClasspath {
    // MockServer 5.15.0 uses json-unit-core 2.36.0 for JSON body matching.
    // hmpps-subject-access-request-test-support pulls in json-unit-assertj:5.x which would
    // upgrade json-unit-core to 5.x, breaking MockServer's JSON matching.
    // Force json-unit-core back to the version MockServer was built against.
    resolutionStrategy.force("net.javacrumbs.json-unit:json-unit-core:6.2.0")
  }
}

dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

testSets {
  "testSmoke"()
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.0")
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")

  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("joda-time:joda-time:2.14.3")
  // At the time of writing, there are no versions of poi-tl beyond 1.12.2, hence the overridden implementations below
  implementation("com.deepoove:poi-tl:1.12.2") {
    // exclude apache.xmlgraphics batik due to vulnerabilities when imported with poi-tl
    exclude("org.apache.xmlgraphics", "batik-codec")
    exclude("org.apache.xmlgraphics", "batik-transcoder")
    implementation("org.apache.commons:commons-compress:1.28.0")
    implementation("org.apache.poi:poi-ooxml:5.5.1")
  }
  implementation("org.springframework.boot:spring-boot-jackson2")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.21.6") {
    because("Address CVE-2026-19032 & CVE-2026-68497")
  }
  implementation("tools.jackson.core:jackson-databind:3.1.6") {
    because("Address CVE-2026-19032 & CVE-2026-68497")
  }
  implementation("ch.qos.logback:logback-classic:1.6.3") {
    because("Address CVE-2026-19880")
  }
  implementation("ch.qos.logback:logback-core:1.6.3") {
    because("Address CVE-2026-19880")
  }

  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("org.postgresql:postgresql:42.7.13")

  implementation("io.sentry:sentry-spring-boot-4:8.52.0")
  implementation("io.sentry:sentry-logback:8.52.0")

  // OpenAPI dependencies
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
  implementation("io.hypersistence:hypersistence-utils-hibernate-71:3.15.4")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.1")
  implementation("org.json:json:20260719")

  implementation("com.google.code.gson:gson:2.14.0")

  implementation("io.flipt:flipt-client-java:1.3.3")

  // shedlock is currently unused, but is expected to be used recurringly with roll-outs
  // requiring recommendations to be soft deleted due to incompatibilities with new functionality
  implementation("net.javacrumbs.shedlock:shedlock-spring:6.10.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.10.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-webtestclient")
  testImplementation("org.springframework:spring-test")

  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("org.mock-server:mockserver-netty:5.15.0")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("io.jsonwebtoken:jjwt:0.13.0")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
  testImplementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:10.0.0")

  testImplementation(platform("io.rest-assured:rest-assured-bom:6.0.1"))
  testImplementation("io.rest-assured:rest-assured")
  testImplementation("io.rest-assured:json-path")
  testImplementation("io.rest-assured:xml-path")

  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-subject-access-request-test-support:2.8.0")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-subject-access-request-lib:2.7.0")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:3.0.1")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-test-autoconfigure:3.0.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:6.2.0")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget = JvmTarget.fromTarget("21")
    }
  }
}

// TODO test removing this - it was introduced as part of the upgrade to Spring Boot 3,
//      but I'm not sure what tool exactly is the one dynamically loading an agent (mockito?).
//      See https://stackoverflow.com/a/79171147 for more info.
tasks.test {
  jvmArgs("-XX:+EnableDynamicAgentLoading")
}

val SourceSet.kotlin: SourceDirectorySet
  get() = project.extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().sourceSets.getByName(
    name,
  ).kotlin

sourceSets {
  create("functional-test") {
    kotlin.srcDirs("src/functional-test")
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
  }
}

tasks.register<Test>("functional-test-light") {
  description = "Runs the functional test, will require start-local-development.sh " +
    "and docker compose-postgres.yml to be started manually"
  group = "verification"
  testClassesDirs = sourceSets["functional-test"].output.classesDirs
  classpath = sourceSets["functional-test"].runtimeClasspath
  useJUnitPlatform()
}

tasks.withType<Jar> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
