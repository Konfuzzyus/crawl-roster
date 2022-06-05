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
    val kotlin = "1.6.21"
    kotlin("multiplatform") version kotlin
    kotlin("plugin.serialization") version kotlin
    id("org.flywaydb.flyway") version "8.5.10"
    id("com.rohanprabhu.kotlin-dsl-jooq") version "0.4.6"
    application
}

group = "org.codecranachan"
version = "0.1"

object Versions {
    const val kotlinWrappers = "18.1.0-pre.337"
    const val kotlinCoroutines = "1.6.1"
    const val kotlinSerialization = "1.3.3"
    const val kotlinDateTime = "0.3.2"
    const val kotlinRedux = "0.5.5"
    const val ktor = "2.0.1"
    const val jooq = "3.16.6"
    const val h2db = "2.1.212"
    const val flyway = "8.5.10"
    const val logback = "1.2.11"
    const val uuid = "0.4.0"
    const val jose4j = "0.7.12"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
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
        val commonMain by getting {
            dependencies {
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerialization}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinDateTime}")
                implementation("com.benasher44:uuid:${Versions.uuid}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependsOn(flyway)
            dependencies {
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
                implementation("io.ktor:ktor-server-netty:${Versions.ktor}")
                implementation("io.ktor:ktor-server-content-negotiation:${Versions.ktor}")
                implementation("io.ktor:ktor-server-html-builder:${Versions.ktor}")
                implementation("io.ktor:ktor-server-sessions:${Versions.ktor}")
                implementation("io.ktor:ktor-server-auth:${Versions.ktor}")
                implementation("org.bitbucket.b_c:jose4j:${Versions.jose4j}")
                implementation("io.ktor:ktor-client-core:${Versions.ktor}")
                implementation("io.ktor:ktor-client-cio:${Versions.ktor}")
                implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                implementation("org.jooq:jooq:${Versions.jooq}")
                implementation("com.h2database:h2:${Versions.h2db}")
                implementation("org.flywaydb:flyway-core:${Versions.flyway}")
                implementation("ch.qos.logback:logback-classic:${Versions.logback}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("com.willowtreeapps.assertk:assertk:0.25")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
                implementation("io.ktor:ktor-client-core:${Versions.ktor}")
                implementation("io.ktor:ktor-client-js:${Versions.ktor}")
                implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}")
                implementation("org.reduxkotlin:redux-kotlin:${Versions.kotlinRedux}")
                implementation("org.reduxkotlin:redux-kotlin-thunk:${Versions.kotlinRedux}")

                implementation(project.dependencies.enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.340"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-icons")
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
    configuration("jvm", java.sourceSets["main"]) {
        configuration = jooqCodegenConfiguration {
            jdbc {
                username = "sa"
                password = ""
                driver = "org.h2.Driver"
                url = flywayJdbc
            }

            generator {
                target {
                    packageName = "${project.group}.roster.jooq"
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
    doFirst { delete(outputs.files) }
}

tasks.named("compileKotlinJvm") {
    dependsOn(tasks.named("jooq-codegen-jvm"))
}

application {
    mainClass.set("${project.group}.roster.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}