import com.rohanprabhu.gradle.plugins.kdjooq.database
import com.rohanprabhu.gradle.plugins.kdjooq.generator
import com.rohanprabhu.gradle.plugins.kdjooq.jdbc
import com.rohanprabhu.gradle.plugins.kdjooq.jooqCodegenConfiguration
import com.rohanprabhu.gradle.plugins.kdjooq.target
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
    val kotlin = "1.7.0"
    kotlin("multiplatform") version kotlin
    kotlin("plugin.serialization") version kotlin
    id("org.flywaydb.flyway") version "8.5.10"
    id("com.rohanprabhu.kotlin-dsl-jooq") version "0.4.6"
    application
}

group = "org.codecranachan"
version = "2022.10.4"

object Versions {
    const val kotlinReact = "18.2.0-pre.347"
    const val kotlinMui = "5.8.3-pre.345"
    const val kotlinEmotion = "11.9.3-pre.347"
    const val kotlinCoroutines = "1.6.3"
    const val kotlinSerialization = "1.3.3"
    const val kotlinDateTime = "0.4.0"
    const val kotlinRedux = "0.5.5"
    const val ktor = "2.0.2"
    const val jooq = "3.17.0"
    const val h2db = "2.1.214"
    const val flyway = "8.5.13"
    const val logback = "1.2.11"
    const val uuid = "0.4.0"
    const val jose4j = "0.7.12"
    const val discord4j = "3.3.0-SNAPSHOT"
    const val reactor = "3.4.12"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
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
                implementation("io.projectreactor:reactor-core:${Versions.reactor}")
                implementation("com.discord4j:discord4j-core:${Versions.discord4j}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
                implementation("io.ktor:ktor-serialization-kotlinx-cbor:${Versions.ktor}")
                implementation("io.ktor:ktor-server-netty:${Versions.ktor}")
                implementation("io.ktor:ktor-server-content-negotiation:${Versions.ktor}")
                implementation("io.ktor:ktor-server-html-builder:${Versions.ktor}")
                implementation("io.ktor:ktor-server-sessions:${Versions.ktor}")
                implementation("io.ktor:ktor-server-auth:${Versions.ktor}")
                implementation("io.ktor:ktor-server-forwarded-header:${Versions.ktor}")
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
                implementation("io.ktor:ktor-client-core-js:${Versions.ktor}")
                implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Versions.kotlinCoroutines}")
                implementation("org.reduxkotlin:redux-kotlin-js:${Versions.kotlinRedux}")
                implementation("org.reduxkotlin:redux-kotlin-thunk-js:${Versions.kotlinRedux}")

                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:${Versions.kotlinReact}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:${Versions.kotlinReact}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:${Versions.kotlinEmotion}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui:${Versions.kotlinMui}")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-icons:${Versions.kotlinMui}")
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