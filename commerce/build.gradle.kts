import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
	id("org.springframework.boot") version "3.1.4"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2022.0.4"

dependencies {
	implementation("org.jetbrains.exposed:exposed-core:0.44.1")
	implementation("org.jetbrains.exposed:exposed-crypt:0.44.1")
	implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
	implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
	implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")
	implementation("org.jetbrains.exposed:exposed-json:0.44.1")
	implementation("org.jetbrains.exposed:exposed-money:0.44.1")
	implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.44.1")

	implementation("com.mysql:mysql-connector-j:8.2.0")

	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}