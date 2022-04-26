plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.1.3"
  kotlin("plugin.spring") version "1.6.20"
  kotlin("plugin.serialization") version "1.4.31"
}

repositories {
  mavenCentral()
}

configurations {
  testImplementation {
    exclude(group = "org.junit.vintage")
    exclude(group = "junit")
  }
}

dependencyCheck {
  suppressionFiles.add("$rootDir/owasp.suppression.xml")
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator:2.6.5")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  "5.7.3".let { sentryVersion ->
    implementation("io.sentry:sentry-spring-boot-starter:$sentryVersion")
    implementation("io.sentry:sentry-logback:$sentryVersion")
  }

  implementation("io.opentelemetry:opentelemetry-api:1.12.0")
  implementation("org.springframework.security:spring-security-oauth2-client")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.6")

  implementation("dev.forkhandles:result4k:2.0.0.0")

  "5.7.0".let { junitVersion ->
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
  }

  testImplementation("io.mockk:mockk:1.12.3")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.0.31")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
    vendor.set(JvmVendorSpec.matching("AdoptOpenJDK"))
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}
