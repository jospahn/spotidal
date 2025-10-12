plugins {
    id("java")
}

group = "com.musicmigration"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.7")
    implementation("se.michaelthelin.spotify:spotify-web-api-java:9.3.0")
    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("ch.qos.logback:logback-classic:1.5.19")
    implementation("org.duckdb:duckdb_jdbc:1.4.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}