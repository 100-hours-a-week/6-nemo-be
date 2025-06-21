plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.sentry.jvm.gradle") version "5.6.0"
    id("jacoco")
}

group = "kr.ai.nemo"
version = "2.0.6-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val jjwtVersion = "0.11.5"
val awsSpringCloud = "3.0.2"
val awsSdkVersion = "1.12.700"
val swaggerVersion = "2.7.0"
val sentryVersion = "8.12.0"
val retryVersion = "1.3.4"

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:$awsSpringCloud")
    implementation("com.amazonaws:aws-java-sdk-s3:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$swaggerVersion")
    implementation("io.sentry:sentry-spring-boot-starter-jakarta:$sentryVersion")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.retry:spring-retry")

    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    runtimeOnly("com.mysql:mysql-connector-j")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

sentry {
    // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    includeSourceContext = true

    org = "glenn-bn"
    projectName = "java-spring-boot"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.test {
    ignoreFailures = true  // 테스트 실패 있어도 실패로 간주하지 않음
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(false)
        html.required.set(true)
        csv.required.set(false)


        sourceDirectories.setFrom(files("src/main/java"))

        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it) {
                        // 커버리지에서 제외할 디렉터리/파일
                        exclude(
                            "**/dto/**",
                            "**/config/**",
                            "**/NemoApplication*",
                            "**/*exception*",
                            "**/*Request*",
                            "**/*request*",
                            "**/*Response*",
                            "**/*response*",
                            "**/*util*"
                        )
                    }
                }
            )
        )
    }
}
