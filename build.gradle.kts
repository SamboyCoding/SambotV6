import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
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
    implementation("net.dv8tion:JDA:4.0.0_46")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha0")

    //Reactive Jda
    implementation("club.minnced:jda-reactor:0.2.7")

    //ORM
    implementation("me.liuwj.ktorm:ktorm-core:2.5")
    implementation("mysql:mysql-connector-java:8.0.17")

    //GSON
    implementation("com.google.code.gson:gson:2.8.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}