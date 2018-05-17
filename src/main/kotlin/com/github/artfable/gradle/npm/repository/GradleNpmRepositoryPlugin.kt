package com.github.artfable.gradle.npm.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * @author artfable
 * 06.05.18
 */
class GradleNpmRepositoryPlugin: Plugin<Project> {
//    private val logger: Logger = LoggerFactory.getLogger(GradleNpmRepositoryPlugin::class.java)
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun apply(project: Project) {
        val config = project.extensions.create("npm", GradleNpmRepositoryExtension::class.java)
        val task = project.task("npmLoad")
        val dependencies: MutableMap<String, List<Interval>> = HashMap()

        task.doFirst {
//            config.packageJSONFile ?: throw IllegalArgumentException("package.json file isn't specified")
            config.output ?: throw IllegalArgumentException("Output directory isn't specified")

//            val packageJSONFile = File(config.packageJSONFile)
            val output = File(config.output)

            if (!output.isDirectory) {
                throw IllegalArgumentException("Provided incorrect output dir")
            }

            config.packageJSONFile?.let { packageJSONFileName ->

                if (config.dependencies.isNotEmpty()) throw IllegalArgumentException("packageJSONFile couldn't be provided if dependencies are provided")

                val packageJSONFile = File(packageJSONFileName)
                val jsonNode = objectMapper.readTree(packageJSONFile)
                val dependencyNode: JsonNode? = jsonNode["dependencies"]

                if (dependencyNode == null) {
                    task.logger.warn("Warning! No dependencies were found")
                    return@doFirst
                }

                dependencyNode.fields().forEach { (name, value) ->
                    dependencies.put(name, parseVersion(value.textValue()))
                }
            }

            config.dependencies.forEach { (name, value) ->
                dependencies.put(name, parseVersion(value))
            }

            if (!config.repository.endsWith("/")) {
                config.repository += "/"
            }

            val dependencyResolver: DependencyResolver = DependencyResolver(objectMapper, task.logger, config.repository)
            dependencyResolver.resolveDependencies(dependencies, config.excludes).forEach { (name, url) ->
                task.logger.lifecycle("Download [$name]: $url")
                val tempDir = Files.createTempDirectory(UUID.randomUUID().toString()).toFile()
                val directory = File(output.absolutePath + File.separatorChar + name)
                val tar = Files.createTempFile(name.replace(File.separator, ""), UUID.randomUUID().toString() + ".tgz").toFile()
                tar.writeBytes(URL(url).readBytes())
                directory.deleteRecursively()
                directory.mkdirs()
                project.copy {
                    it.from(project.tarTree(tar)).into(tempDir)
                }
                project.copy {
                    it.from(tempDir.path + File.separatorChar + "package").into(directory)
                }
                tempDir.delete()
                tar.delete()
            }
        }
    }
}

open class GradleNpmRepositoryExtension() {
    var packageJSONFile: String? = null
    var output: String? = null
    var repository: String = "https://registry.npmjs.org/"
    var excludes: Set<String> = HashSet()
    var dependencies: Map<String, String> = HashMap()
}
