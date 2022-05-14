import com.rohanprabhu.gradle.plugins.kdjooq.*
import org.flywaydb.gradle.task.FlywayMigrateTask

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.h2database:h2:2.1.212")
    }
}

plugins {
    kotlin("multiplatform") version "1.6.21"
    id("org.flywaydb.flyway") version "8.5.10"
    id("com.rohanprabhu.kotlin-dsl-jooq") version "0.4.6"
    application
}

group = "me.konfuzzyus"
version = "1.0-SNAPSHOT"

object Versions {
    const val kotlin = "1.6.10"
    const val jooq = "3.16.6"
    const val h2db = "2.1.212"
    const val flyway = "8.5.10"
    const val logback = "1.2.11"
}
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val flyway by creating {
        }
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependsOn(flyway)
            dependencies {
                implementation("io.ktor:ktor-server-netty:1.6.7")
                implementation("io.ktor:ktor-html-builder:1.6.7")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
                implementation("org.jooq:jooq:${Versions.jooq}")
                implementation("com.h2database:h2:${Versions.h2db}")
                implementation("org.flywaydb:flyway-core:${Versions.flyway}")
                implementation("ch.qos.logback:logback-classic:${Versions.logback}")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.290-kotlin-${Versions.kotlin}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.290-kotlin-${Versions.kotlin}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-css:17.0.2-pre.290-kotlin-${Versions.kotlin}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.2.1-pre.290-kotlin-${Versions.kotlin}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-redux:4.1.2-pre.290-kotlin-${Versions.kotlin}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.6-pre.290-kotlin-${Versions.kotlin}")
            }
        }
        val jsTest by getting
    }
}

val flywayGeneratedDir = "${project.buildDir}/generated/flyway"
val flywayJdbc = "jdbc:h2:file:${flywayGeneratedDir}/database"
val jooqGeneratedDir = "${project.buildDir}/generated/jooq"

flyway {
    url = flywayJdbc
    user = "sa"
    password = ""
    schemas = arrayOf("ROSTER")
    locations = project.kotlin.sourceSets["flyway"].resources.srcDirs.map { "filesystem:$it" }.toTypedArray()
    createSchemas = true
}

tasks.flywayMigrate {
    project.kotlin.sourceSets["flyway"].resources.srcDirs.forEach(inputs::dir)
    outputs.dir(flywayGeneratedDir)
    doFirst { delete(outputs.files) }
}

jooqGenerator {
    jooqVersion = "3.16.6"
    configuration("jvm" , java.sourceSets["main"]) {
        configuration = jooqCodegenConfiguration {
            jdbc {
                username = "sa"
                password = ""
                driver = "org.h2.Driver"
                url = flywayJdbc
            }

            generator {
                target {
                    packageName = "${project.group}.${project.name}.jooq"
                    directory = jooqGeneratedDir
                }

                database {
                    name = "org.jooq.meta.h2.H2Database"
                    inputSchema = "ROSTER"
                }
            }
        }
    }
    dependencies {
        jooqGeneratorRuntime("com.h2database:h2:${Versions.h2db}")
    }
}

tasks.named("jooq-codegen-jvm") {
    val fw = tasks.named<FlywayMigrateTask>("flywayMigrate")
    inputs.files(fw.get().outputs.files)
    dependsOn(fw)
}

tasks.named("jvmMainClasses") {
    dependsOn(tasks.named("jooq-codegen-jvm"))
}

application {
    mainClass.set("me.konfuzzyus.crawlroster.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}