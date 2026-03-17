plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("io.spring.dependency-management") version "1.1.7"
    `java-library`
    `maven-publish`
}

group = "com.milabuda"
version = resolveVersion()

fun resolveVersion(): String {
    // 1. In GitHub Actions on tag push: GITHUB_REF_NAME = "0.5.0"
    System.getenv("GITHUB_REF_NAME")
        ?.takeIf { it.isNotBlank() }
        ?.let { return it }

    // 2. Locally on a tagged commit: git describe --tags --exact-match
    runCatching {
        ProcessBuilder("git", "describe", "--tags", "--exact-match")
            .directory(projectDir)
            .start()
            .inputStream.bufferedReader().readLine()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
    }

    // 3. Fallback for local development (untagged commit)
    return "0.0.0-SNAPSHOT"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.1")
    }
}

dependencies {
    // Spring — compileOnly: provided by the consuming Spring Boot app
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux")

    // Spring Cloud AWS DynamoDB — implementation: all apps use snapshot feature
    implementation(libs.spring.cloud.aws.starter.dynamodb)

    // Spring Cloud AWS S3 — compileOnly: only needed when image archive feature is used
    compileOnly(libs.spring.cloud.aws.starter.s3)

    // Coroutines — implementation: used in S3ImageArchiver / S3Mediator
    implementation(libs.kotlinx.coroutines.core)

    // Micrometer — compileOnly: provided by consuming Spring Boot app (Actuator)
    compileOnly("io.micrometer:micrometer-core")
    compileOnly("io.micrometer:micrometer-observation")
    // Jackson — compileOnly: provided by consuming Spring Boot app
    compileOnly(libs.jackson.kotlin)

    // Resilience4j — implementation: used programmatically, not via AOP/annotations
    implementation(libs.resilience4j.retry)

    // Jsoup — HTML document retrieval
    implementation(libs.jsoup)

    // Caffeine — programmatic cache, no Spring Cache/AOP needed
    implementation(libs.caffeine)

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.kotlin.logging)

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation(libs.mockk)
    testImplementation(libs.wiremock.spring.boot)
    // Make compileOnly deps available to tests
    testImplementation(libs.spring.cloud.aws.starter.s3)
    testImplementation("io.micrometer:micrometer-core")
    testImplementation("io.micrometer:micrometer-observation")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MiLabuda/dh-scraping-commons")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN")
                    ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}
