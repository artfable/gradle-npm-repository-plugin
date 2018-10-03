package com.github.artfable.gradle.npm.repository

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

/**
 * @author artfable
 * 09.05.18
 */
fun parseVersion(versionCondition: String): List<Interval> {
    val versions: MutableList<Interval> = LinkedList()
    var current: Interval = Interval()
    var range: Boolean = false

    versionCondition.trim().split(" ").forEach{ part ->
        if ("||" == part) {
            versions.add(current)
            current = Interval()
            return@forEach
        }

        if (part.isEmpty() || "*" == part) {
            current = Interval()
            return@forEach
        }

        if ("-" == part) {
            range = true
            return@forEach
        }

        val groupValues = Regex("^((?:[><]?=?|~|\\^)?v?)(\\d.*)").find(part)?.groupValues
        val condition = groupValues?.get(1)?.dropLastWhile { it == 'v' }
        val versionValue = groupValues?.get(2)
        when {
            condition == null -> {}
            condition.isEmpty() || condition == "=" -> {
                if (range) {
                    range = false
                    current.startOpen = false
                    current.endOpen = true
                    current.end = Version(versionValue!!).next()
                } else {
                    val version = Version(versionValue!!)
                    current = Interval(version, if (version.suffix != null) version else version.next(), false, version.suffix == null)
                }
            }
            condition == "~" -> {
                val version = Version(versionValue!!)
                current.start = version
                current.startOpen = false
                current.endOpen = true
                current.end = version.approximateNext()
            }
            condition == "^" -> {
                val version = Version(versionValue!!)
                current.start = version
                current.startOpen = false
                current.endOpen = true
                current.end = version.caretNext()
            }
            condition.startsWith(">") -> {
                current.start = Version(versionValue!!)
                current.startOpen = !condition.endsWith("=")
            }
            condition.startsWith("<") -> {
                current.end = Version(versionValue!!)
                current.endOpen = !condition.endsWith("=")
            }
        }
    }

    versions.add(current)
    return versions
}

data class Version(val versionStr: String): Comparable<Version> {
    private val mutableParts: MutableList<Int> = ArrayList()
    val parts: List<Int> get() = mutableParts.toList()
    val suffix: String?
    init {
        val suffixStart = versionStr.indexOf('-')
        suffix = if (suffixStart != -1) versionStr.substring(suffixStart) else null
        versionStr.removeSuffix(suffix ?: "").split(".").forEach {
            it.toIntOrNull()?.let {
                mutableParts.add(it)
            }
        }
    }

    override fun compareTo(other: Version): Int {
        var result: Int = 0
        val smallestSize = min(mutableParts.size, other.mutableParts.size)

        for (i in 0 until smallestSize) {
            result = mutableParts[i].compareTo(other.mutableParts[i])
            if (result != 0) break
        }

        if (result == 0) {
            result = mutableParts.size.compareTo(other.mutableParts.size)
        }

        if (result == 0) {
            if (suffix === other.suffix) return 0
            if (suffix === null) return 1
            if (other.suffix === null) return -1
            return suffix.compareTo(other.suffix)
        }

        return result
    }

    fun next(): Version {
        suffix?.let {
            return Version(versionStr.removeSuffix(suffix))
        }
        return Version(mutableParts.dropLast(1).joinToString(separator = ".", postfix = ".") + (mutableParts.last() + 1))
    }

    fun approximateNext(): Version {
        return Version(if (parts.size < 2) (parts[0] + 1).toString() else parts[0].toString() + "." + (parts[1] + 1))
    }

    fun caretNext(): Version {
        val elementToIncrease = parts.indexOfFirst { it > 0 }.coerceAtLeast(0)

        return Version(parts.subList(0, elementToIncrease).plus(parts[elementToIncrease] + 1).joinToString("."))
    }
}

data class Interval(var start: Version? = null,
                    var end: Version? = null,
                    var startOpen: Boolean = false,
                    var endOpen: Boolean = false) {

    fun match(version: Version): Boolean {
        return (start == null || start!! < version || (!startOpen && start == version))
                && (end == null || end!! > version || (!endOpen && end == version))
    }
}
