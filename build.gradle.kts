import kotlinx.benchmark.gradle.BenchmarkConfiguration

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.aoc.inputs.downloader)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

val benchmarksSourceSet = sourceSets.create("benchmarks")

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xmulti-dollar-interpolation", "-Xnon-local-break-continue")
    }
    target {
        compilations["benchmarks"].associateWith(compilations["main"])
    }
}

val benchmarksDirectorySubPath = File.separator + benchmarksSourceSet.name + File.separator

ktlint {
    version = "1.4.1"
    filter {
        exclude { benchmarksDirectorySubPath in it.file.absolutePath }
    }
}

val benchmarksImplementation by configurations

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    benchmarksImplementation(libs.kotlinx.benchmark.runtime)
}

downloadAocInputs {
    sessionCookie { projectDir.resolve("secrets/session-cookie.txt").readText() }
}

benchmark {
    targets.register("benchmarks")
    configurations {
        benchmarksSourceSet.kotlin.asSequence()
            .filter { it.extension == "kt" }
            .forEach { file -> registerBenchmarkTasks(file.nameWithoutExtension) }
    }
}

fun NamedDomainObjectContainerScope<BenchmarkConfiguration>.registerBenchmarkTasks(fileName: String) {
    val trimmedName = fileName.replaceFirstChar { it.lowercaseChar() }.substringBefore("Benchmark")
    val includePattern = ".*$fileName.*"

    register("${trimmedName}Standard") {
        include(includePattern)
        warmups = 5
        outputTimeUnit = "ms"
        mode = "avgt"
        iterations = 5
        iterationTime = 5
        iterationTimeUnit = "sec"
    }
    register("${trimmedName}Fast") {
        include(includePattern)
        warmups = 20
        outputTimeUnit = "ms"
        iterations = 5
        iterationTime = 500
        iterationTimeUnit = "ms"
        mode = "avgt"
    }
}
