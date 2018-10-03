package com.github.artfable.gradle.npm.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author artfable
 * 10.05.18
 */
internal class VersionTest {

    @Test
    fun testVersion() {
        val version = Version("1.2.4-rc")

        assertEquals("-rc", version.suffix)
        assertEquals(listOf(1, 2, 4), version.parts)

        val simpleVersion = Version("1.24.44")

        assertNull(simpleVersion.suffix)
        assertEquals(listOf(1, 24, 44), simpleVersion.parts)

        val openEndVersion = Version("1.2.x")

        assertNull(openEndVersion.suffix)
        assertEquals(listOf(1, 2), openEndVersion.parts)
    }

    @Test
    fun testCompareTo() {
        val version1 = Version("1.2.4-rc")
        val version2 = Version("1.2.24-rc")
        val version3 = Version("1.2.4")
        val version4 = Version("1.2.4-beta")
        val version5 = Version("1.4.2")
        val version6 = Version("1.4")

        assertTrue(version1 < version2)
        assertTrue(version1 < version3)
        assertTrue(version1 > version4)
        assertTrue(version5 > version3)
        assertTrue(version5 > version6)
    }

    @Test
    fun testNext() {
        assertEquals(Version("1.2.4"), Version("1.2.4-rc").next())
        assertEquals(Version("1.2.4.4.9"), Version("1.2.4.4.8").next())
    }

    @Test
    fun testApproximateNext() {
        assertEquals(Version("1.3"), Version("1.2.4.5-rc").approximateNext())
        assertEquals(Version("2"), Version("1").approximateNext())
    }

    @Test
    fun testCaretNext() {
        assertEquals(Version("2"), Version("1.2.4-rc").caretNext())
        assertEquals(Version("0.0.5"), Version("0.0.4").caretNext())
        assertEquals(Version("1"), Version("0").caretNext())
    }

    @Test
    fun testMatch() {
        val version1 = Version("1.2.5")
        val version2 = Version("1.4")
        val version3 = Version("1.4.2")

        val interval1 = Interval(version1, version2, true, false)
        val interval2 = Interval(null, version2, true, true)
        val interval3 = Interval(version1, null, false, false)

        assertFalse(interval1.match(version1))
        assertTrue(interval2.match(version1))
        assertTrue(interval3.match(version1))

        assertTrue(interval1.match(version2))
        assertFalse(interval1.match(version3))
        assertFalse(interval2.match(version2))
        assertTrue(interval3.match(version3))
    }

    @Test fun testMatchWithSuffix() {
        val version = Version("1.2.5-rc.10")

        val interval = Interval(Version("1.2.5"), Version("1.3.0"), false, true)
        val interval2 = Interval(Version("1.2.4"), Version("1.2.5"), false, true)

        assertEquals("-rc.10", version.suffix)
        assertFalse(interval.match(version))
        assertTrue(interval2.match(version))
    }
}