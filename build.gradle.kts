plugins {
    id("java")
}

group = "com.telefonica.cose.provenance.kafka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {


    // cose-provenance dependencies

    implementation("org.apache.santuario:xmlsec:3.0.3")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
    // https://mvnrepository.com/artifact/com.augustcellars.cose/cose-java
    // https://mvnrepository.com/artifact/org.jdom/jdom2
    implementation("org.jdom:jdom2:2.0.6")
    runtimeOnly("com.augustcellars.cose:cose-java:1.1.0")
    implementation("com.google.code.gson:gson:2.8.9")

    // json management dependencies

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    // canonicalization JCS
    // https://mvnrepository.com/artifact/io.github.erdtman/java-json-canonicalization
    implementation("io.github.erdtman:java-json-canonicalization:1.1")


    // https://mvnrepository.com/artifact/com.upokecenter/cbor
    implementation("com.upokecenter:cbor:4.5.6")


    //kafka dependencies

    // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
    implementation("org.apache.kafka:kafka-clients:3.9.0")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.17")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    implementation("org.slf4j:slf4j-simple:2.0.17")


    implementation("com.telefonica.api:provenance-api:0.0.3")



}

tasks.test {
    useJUnitPlatform()
}