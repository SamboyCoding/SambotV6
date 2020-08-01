import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.70"
    id("com.github.johnrengelman.shadow") version("5.1.0")
}

group = "me.samboycoding"
version = "1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    //Jda
    implementation("net.dv8tion:JDA:4.2.0_168")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")

    //Reactive Jda
    implementation("club.minnced:jda-reactor:1.2.0")

    //ORM
    implementation("me.liuwj.ktorm:ktorm-core:3.0.0")
    implementation("mysql:mysql-connector-java:8.0.17")

    //GSON
    implementation("com.google.code.gson:gson:2.8.6")

    //Reflections
    implementation("org.reflections:reflections:0.9.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}