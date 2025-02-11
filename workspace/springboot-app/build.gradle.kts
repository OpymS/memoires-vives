plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "fr.memoires-vives"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    this.options.encoding = "UTF-8"
}

tasks.register<Copy>("copyImages") {
    from("src/main/resources/static/images/public")
    into("build/resources/main/static/images/public")
}

tasks.bootJar {
	dependsOn("copyImages")
    archiveFileName.set("memoires-vives.jar")
}

tasks.jar {
	dependsOn("copyImages")
    manifest {
        attributes(
            "Main-Class" to "fr.memoires-vives.SpringbootAppApplication"
        )
    }
}

tasks.named("resolveMainClassName") {
    dependsOn("copyImages")
}

tasks.named("compileTestJava") {
    dependsOn("copyImages") // Assure-toi que les images sont copiées avant de compiler les tests
}
