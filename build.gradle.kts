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


    implementation("org.apache.santuario:xmlsec:3.0.3")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
    // https://mvnrepository.com/artifact/com.augustcellars.cose/cose-java
    // https://mvnrepository.com/artifact/org.jdom/jdom2
    implementation("org.jdom:jdom2:2.0.6")
    runtimeOnly("com.augustcellars.cose:cose-java:1.1.0")
    implementation("com.google.code.gson:gson:2.8.9")

    // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
    implementation("org.apache.kafka:kafka-clients:3.9.0")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.17")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    implementation("org.slf4j:slf4j-simple:2.0.17")


    // To canonicalize in JCS schema JSON
    //implementation(files("libs/provenance-api-0.0.2.jar"))

    implementation("com.telefonica.api:provenance-api:0.0.2")



}

tasks.test {
    useJUnitPlatform()
}