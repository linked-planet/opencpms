import net.pwall.json.schema.codegen.*
import net.pwall.json.schema.parser.Parser

plugins {
    kotlin("jvm")
}

buildscript {
    dependencies {
        classpath("net.pwall.json:json-kotlin-schema-codegen:0.63")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib"))
}

// Project structure
val generatedKotlinSourceDir = "$buildDir/generated-sources/kotlin"
kotlin {
    sourceSets {
        main {
            kotlin.srcDir("$buildDir/generated-sources/kotlin")
        }
    }
}

// Generate classes for all the stuff
val sourceDir = "$projectDir/src/main/json/"
val outputDir = "$buildDir/generated-sources/kotlin"
val packageName = "$group.${name.replace("-", ".")}"

val generateTask = tasks.register<GenerateTask>("generate") {
    this.sourceDirectory = sourceDir
    this.outputDirectory = outputDir
    this.outputPackageName = packageName
}

abstract class GenerateTask : DefaultTask() {

    @Input
    var sourceDirectory: String = ""

    @Input
    var outputDirectory: String = ""

    @Input
    var outputPackageName: String = ""

    @TaskAction
    fun test() {
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = outputDirectory
        codeGenerator.basePackageName = outputPackageName
        codeGenerator.targetLanguage = TargetLanguage.KOTLIN
        codeGenerator.nestedClassNameOption = CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY

        val parser = Parser()
        val inputClasses = File(sourceDirectory)
            .walkTopDown()
            .toList()
            .filter {
                val suffix = it.name.substringAfterLast("/").substringAfterLast(".", "")
                suffix == "json"
            }
            .map {
                val schema = parser.parse(it)
                val classname = it.name.substringAfterLast("/").substringBeforeLast(".")
                schema to classname
            }

        codeGenerator.generateClasses(inputClasses)
    }
}
