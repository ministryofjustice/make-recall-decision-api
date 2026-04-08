import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.6.0"
  kotlin("jvm") version "2.3.10"
  id("org.unbroken-dome.test-sets") version "4.1.0"
  id("jacoco")
  kotlin("plugin.jpa") version "2.3.10"
  id("org.sonarqube") version "6.2.0.5505"
  kotlin("plugin.spring") version "2.3.10"
  kotlin("plugin.serialization") version "2.3.10"
}

jacoco.toolVersion = "0.8.11"

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

testSets {
  "testSmoke"()
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.4.7")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("io.micrometer:micrometer-registry-prometheus:1.15.1")
  implementation("io.opentelemetry:opentelemetry-api:1.51.0")
  implementation("joda-time:joda-time:2.14.0")
  // At the time of writing, there are no versions of poi-tl beyond 1.12.2, hence the overridden implementations below
  implementation("com.deepoove:poi-tl:1.12.2") {
    // exclude apache.xmlgraphics batik due to vulnerabilities when imported with poi-tl
    exclude("org.apache.xmlgraphics", "batik-codec")
    exclude("org.apache.xmlgraphics", "batik-transcoder")
    implementation("org.apache.commons:commons-compress:1.27.1") // Address CVE-2024-25710 and CVE-2024-26308 present in v1.21
    implementation("org.apache.poi:poi-ooxml:5.4.1") // Address CVE-2025-31672 present in 5.2.2
  }
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  // Temporary fix to address CVE-2025-68161 until we upgrade to spring-boot 4 or a 3.5.x with the fix is released
  implementation("org.apache.logging.log4j:log4j-api:2.25.3")

  implementation("org.flywaydb:flyway-core:11.1.1")
  implementation("org.flywaydb:flyway-database-postgresql:11.1.1")
  implementation("org.postgresql:postgresql:42.7.7")

  implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.38.0")
  implementation("io.sentry:sentry-logback:7.20.0")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
  // Temporary fix to address CVE-2026-0540, CVE-2025-15599, should be removable once
  // springdoc-openapi-starter-webmvc-ui above pulls later version of swagger-ui
  implementation("org.webjars:swagger-ui:5.32.1")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
  implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.10.1")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.6")
  implementation("org.json:json:20250517")

  implementation("com.google.code.gson:gson:2.13.2")

  // shedlock is currently unused, but is expected to be used recurringly with roll-outs
  // requiring recommendations to be soft deleted due to incompatibilities with new functionality
  implementation("net.javacrumbs.shedlock:shedlock-spring:6.10.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.10.0")

  // The following pinned netty dependencies are to address CVE-2026-33871 and CVE-2026-33870. Spring Boot 3.5.13
  // addresses this, but it is currently only a few days old, so will wait for a bit more before releasing a new HMPPS
  // plug-in version with it and pulling it here
  implementation("io.netty:netty-buffer:4.2.12.Final")
  implementation("io.netty:netty-codec:4.2.12.Final")
  implementation("io.netty:netty-codec-dns:4.2.12.Final")
  implementation("io.netty:netty-codec-http:4.2.12.Final")
  implementation("io.netty:netty-codec-http2:4.2.12.Final")
  implementation("io.netty:netty-codec-socks:4.2.12.Final")
  implementation("io.netty:netty-common:4.2.12.Final")
  implementation("io.netty:netty-handler:4.2.12.Final")
  implementation("io.netty:netty-handler-proxy:4.2.12.Final")
  implementation("io.netty:netty-resolver:4.2.12.Final")
  implementation("io.netty:netty-resolver-dns:4.2.12.Final")
  implementation("io.netty:netty-resolver-dns-classes-macos:4.2.12.Final")
  implementation("io.netty:netty-resolver-dns-native-macos:4.2.12.Final")
  implementation("io.netty:netty-resolver-dns-native-macos:4.2.12.Final")
  implementation("io.netty:netty-transport:4.2.12.Final")
  implementation("io.netty:netty-transport-classes-epoll:4.2.12.Final")
  implementation("io.netty:netty-transport-native-epoll:4.2.12.Final")
  implementation("io.netty:netty-transport-native-unix-common:4.2.12.Final")

  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("org.mock-server:mockserver-netty:5.15.0")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("io.jsonwebtoken:jjwt:0.13.0")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
  testImplementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:10.0.0")

  testImplementation("io.rest-assured:rest-assured")
  testImplementation("io.rest-assured:json-path")
  testImplementation("io.rest-assured:xml-path")

  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
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

tasks.test {
  jvmArgs("-XX:+EnableDynamicAgentLoading")
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
  }
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
