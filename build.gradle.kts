plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.3.0"
  kotlin("jvm") version "1.7.0"
  id("jacoco")
  kotlin("plugin.jpa") version "1.7.0"
  id("org.sonarqube") version "3.4.0.2513"
  kotlin("plugin.spring") version "1.7.0"
}

jacoco.toolVersion = "0.8.8"

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

allOpen {
  annotations("javax.persistence.Entity")
}

val springDocVersion = "1.6.9"

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-actuator:2.7.1")
  implementation("io.micrometer:micrometer-registry-prometheus:1.9.1")
  implementation("io.opentelemetry:opentelemetry-api:1.15.0")

  implementation("org.flywaydb:flyway-core:8.5.13")
  implementation("org.postgresql:postgresql:42.4.0")

  implementation("io.sentry:sentry-spring-boot-starter:6.1.4")
  implementation("io.sentry:sentry-logback:6.1.4")

  implementation("org.springdoc:springdoc-openapi-webmvc-core:$springDocVersion")
  implementation("org.springdoc:springdoc-openapi-ui:$springDocVersion")
  implementation("org.springdoc:springdoc-openapi-kotlin:$springDocVersion")
  implementation("org.springdoc:springdoc-openapi-data-rest:$springDocVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.3.0")

  testImplementation("org.mock-server:mockserver-netty:5.13.2")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
  testImplementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:7.0.0")
  testImplementation("com.h2database:h2:2.1.214")

  testImplementation("io.rest-assured:rest-assured:5.0.1")
  implementation("io.rest-assured:json-path:5.0.1")
  implementation("io.rest-assured:xml-path:5.1.1")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(18))
    vendor.set(JvmVendorSpec.matching("AdoptOpenJDK"))
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "18"
    }
  }
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
  }
}

val SourceSet.kotlin: SourceDirectorySet
  get() = project.extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().sourceSets.getByName(name).kotlin

sourceSets {
  create("functional-test") {
    kotlin.srcDirs("src/functional-test")
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
  }
}
val dockerUp = task<Exec>("dockerUp") {
  dependsOn("assemble")
  commandLine("docker-compose", "-f", "docker-compose-functional-test.yml", "up", "-d")
}

val waitForIt = task<Exec>("waitForIt") {
  dependsOn(dockerUp)
  commandLine("./scripts/wait-for-it.sh", "127.0.0.1:8081", "--strict", "-t", "600")
}

val dockerDown = task<Exec>("dockerDown") {
  commandLine("./scripts/clean-up-docker.sh")
}

task<Test>("functional-test") {
  description = "Runs the functional test"
  group = "verification"
  testClassesDirs = sourceSets["functional-test"].output.classesDirs
  classpath = sourceSets["functional-test"].runtimeClasspath
  useJUnitPlatform()
  dependsOn(waitForIt)
  finalizedBy(dockerDown)
}

tasks.withType<Jar>() {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
