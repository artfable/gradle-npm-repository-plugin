package com.github.artfable.gradle.npm.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author artfable
 * 09.05.18
 */
internal class VersionKtTest {

    @Test
    fun testParseVersion() {
        val versionValue = "1.2.4"
        val versionNextValue = "1.2.5"
        val version = Version(versionValue)
        val versionNext = Version(versionNextValue)
        val version2nd = Version(versionValue + 1)
        assertEquals(Interval(version, null, false, false), parseVersion(">=v$versionValue")[0], ">=")
        assertEquals(Interval(version, versionNext, false, true), parseVersion("=$versionValue")[0], "=")
        assertEquals(Interval(version, versionNext, false, true), parseVersion(versionValue)[0], "=")
        assertEquals(Interval(version, null, true, false), parseVersion(">v$versionValue")[0], ">")
        assertEquals(Interval(null, version, false, false), parseVersion("<=v$versionValue")[0], "<=")
        assertEquals(Interval(null, version, false, true), parseVersion("<$versionValue")[0], "<")

        assertEquals(Interval(version, version2nd, true, true), parseVersion("<${versionValue + 1} >$versionValue")[0], "> <")
        assertEquals(Interval(version, version2nd, true, true), parseVersion(">$versionValue <${versionValue + 1}")[0], "> <")
        assertEquals(Interval(version, version2nd, true, false), parseVersion(">$versionValue <=${versionValue + 1}")[0], "> <=")

        assertEquals(Interval(null, null, false, false), parseVersion("*")[0], "*")
        assertEquals(Interval(version, versionNext, false, true), parseVersion("$versionValue - $versionValue")[0], "-")

        assertEquals(Interval(version, version.caretNext(), false, true), parseVersion("^$versionValue")[0], "^")
        assertEquals(Interval(version, version.approximateNext(), false, true), parseVersion("~$versionValue")[0], "~")
    }

    @Test
    fun testParseVersion_groups() {
        val versionValue = "1.2.4"
        val version = Version(versionValue)
        val versionNext = Version("1.2.5")
        val versions = parseVersion(">=$versionValue || $versionValue - $versionValue || ~$versionValue || <${versionValue + 1} >$versionValue || <$versionValue || *")

        assertEquals(6, versions.size)
        assertIterableEquals(listOf(Interval(version, null, false, false),
                Interval(version, versionNext, false, true),
                Interval(version, Version("1.3"), false, true),
                Interval(version, Version(versionValue + 1), true, true),
                Interval(null, version, false, true),
                Interval()), versions)
    }
}