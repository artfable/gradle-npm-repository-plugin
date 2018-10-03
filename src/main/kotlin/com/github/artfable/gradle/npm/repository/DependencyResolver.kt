package com.github.artfable.gradle.npm.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import java.net.URL

/**
 * @author artfable
 * 14.05.18
 */
class DependencyResolver(private val objectMapper: ObjectMapper, private val logger: Logger, private val repository: String) {

    private lateinit var resolvedVersions: MutableMap<String, Version>
    private lateinit var others: MutableMap<String, Map<String, List<Interval>>>
    private val cacheNodes: MutableMap<String, JsonNode> = HashMap()

    fun resolveDependencies(dependencies: MutableMap<String, List<Interval>>, excludes: Set<String>): Map<String, String> {
        resolvedVersions = HashMap()
        others = mutableMapOf(Pair("*MAIN*", dependencies))
        processDependencies(dependencies, excludes)
        return resolvedVersions.mapValues { (name, version) ->
            logger.lifecycle("[$name] resolved to version [${version.versionStr}]")
            cacheNodes[name]?.get("versions")?.get(version.versionStr)?.get("dist")?.get("tarball")?.textValue().orEmpty()
        }
    }

    private fun processDependencies(dependencies: Map<String, List<Interval>>, excludes: Set<String>) {
        val childDependencies: MutableSet<String> = HashSet()
        dependencies.forEach { (name, intervals) ->
            resolvedVersions[name]?.let { version ->
                if (intervals.any { it.match(version) }) {
                    return@forEach
                }
                logger.debug("Delete dependencies for [$name] version [${version.versionStr}]")
                resolvedVersions.remove(name)
                others.remove(name)
            }

            val metadata = getDependencyMetadata(name)
            val versionsNode: JsonNode? = metadata["versions"]
            versionsNode ?: throw IllegalArgumentException("No versions for [$name] dependency!")

            for (versionStr in versionsNode.fieldNames().asSequence().asIterable().reversed()) {
                val version = Version(versionStr)
                if (intervals.any { it.match(version) } && matchAllDependencies(name, version)) {
                    resolvedVersions.put(name, version)
                    val childDependencyNode: JsonNode? = versionsNode[versionStr]?.get("dependencies")
                    val decedentDependencies: MutableMap<String, List<Interval>> = HashMap()
                    childDependencyNode?.fields()?.forEach { (decedentName, value) ->
                        if (!excludes.contains(decedentName)) decedentDependencies.put(decedentName, parseVersion(value.textValue()))
                    }
                    childDependencies.add(name)
                    others.put(name, decedentDependencies)
                    break
                }
            }

            if (!resolvedVersions.containsKey(name)) {
                throw IllegalArgumentException("Can't resolve version for [$name]; required ${dependencies[name]}")
            }

            logger.info("Preliminarily version for [$name] resolved as [${resolvedVersions[name]?.versionStr}]")
        }

        childDependencies.forEach { name ->
            others[name] ?: throw IllegalStateException("Lost dependencies for [$name]; Resolved version was [${resolvedVersions[name]?.versionStr}], required ${dependencies[name]}")
            processDependencies(others[name]!!, excludes)
        }
    }

    private fun matchAllDependencies(name: String, version: Version): Boolean {
        for ((_, dependencies) in others) {
            dependencies[name]?.none { it.match(version) }?.let {
                if (it) {
                    return false
                }
            }
        }

        return true
    }

    private fun getDependencyMetadata(name: String): JsonNode {
        cacheNodes[name]?.let {
            return it
        }

        logger.lifecycle("Loading metadata for [$name]...")
        // can't use URLEncoder as symbols other than '/' shouldn't be encoded
        val metadata = objectMapper.readTree(URL(repository + name.replace("/", "%2F")).openStream())
        cacheNodes[name] = metadata
        return metadata
    }
}