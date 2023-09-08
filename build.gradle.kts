plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.11.1"
  kotlin("jvm") version "1.9.10"
  id("org.unbroken-dome.test-sets") version "4.0.0"
  id("jacoco")
  kotlin("plugin.jpa") version "1.9.10"
  id("org.sonarqube") version "4.3.1.3277"
  kotlin("plugin.spring") version "1.9.10"
}

jacoco.toolVersion = "0.8.8"

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

testSets {
  "testSmoke"()
}

allOpen {
  annotations("javax.persistence.Entity")
}

val springDocVersion = "1.7.0"

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-actuator:3.1.3")
  implementation("io.micrometer:micrometer-registry-prometheus:1.11.3")
  implementation("io.opentelemetry:opentelemetry-api:1.29.0")
  implementation("joda-time:joda-time:2.12.5")
  implementation("com.deepoove:poi-tl:1.12.1") {
    // exclude apache.xmlgraphics batik due to vulnerabilities when imported with poi-tl
    exclude("org.apache.xmlgraphics", "batik-codec")
    exclude("org.apache.xmlgraphics", "batik-transcoder")
  }
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  implementation("org.flywaydb:flyway-core:9.22.0")
  implementation("org.postgresql:postgresql:42.6.0")

  implementation("io.sentry:sentry-spring-boot-starter:6.29.0")
  implementation("io.sentry:sentry-logback:6.29.0")

  implementation("org.springdoc:springdoc-openapi-webmvc-core:$springDocVersion")
  implementation("org.springdoc:springdoc-openapi-ui:$springDocVersion")
  implementation("org.springdoc:springdoc-openapi-kotlin:$springDocVersion")
  implementation("org.springdoc:springdoc-openapi-data-rest:$springDocVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")
  implementation("com.vladmihalcea:hibernate-types-52:2.21.1")
  implementation("com.amazonaws:aws-java-sdk-sns")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:1.3.1")
  implementation("org.json:json:20230618")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("org.mock-server:mockserver-netty:5.15.0")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("org.junit.jupiter:junit-jupiter-params")

  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
  testImplementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:9.5.0")

  implementation("io.rest-assured:rest-assured")
  implementation("io.rest-assured:json-path")
  implementation("io.rest-assured:xml-path")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(18))
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
  get() = project.extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().sourceSets.getByName(
    name
  ).kotlin

sourceSets {
  create("functional-test") {
    kotlin.srcDirs("src/functional-test")
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
  }
}

task<Test>("functional-test-light") {
  description = "Runs the functional test, will require start-local-development.sh " +
    "and docker-compose-postgres.yml to be started manually"
  group = "verification"
  testClassesDirs = sourceSets["functional-test"].output.classesDirs
  classpath = sourceSets["functional-test"].runtimeClasspath
  useJUnitPlatform()
}

tasks.withType<Jar>() {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
