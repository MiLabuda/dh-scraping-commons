plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("io.spring.dependency-management") version "1.1.7"
    `java-library`
    `maven-publish`
}

group = "com.milabuda"
version = "0.4.0"

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

    // Resilience4j — implementation: used programmatically, not via AOP/annotations
    implementation(libs.resilience4j.retry)

    // Caffeine — programmatic cache, no Spring Cache/AOP needed
    implementation(libs.caffeine)

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.kotlin.logging)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
