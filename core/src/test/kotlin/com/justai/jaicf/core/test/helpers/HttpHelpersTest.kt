package com.justai.jaicf.core.test.helpers

import com.justai.jaicf.helpers.http.withTrailingSlash
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class HttpHelpersTest {

    @Nested
    inner class TrailingSlashesTest {
        private val symbols = ('a'..'z') + ('A'..'Z') + ('0'..'9') + arrayOf('/', '\\', ':', '?', '&', '@')

        private fun randomString(len: Int) =
            (1..len).joinToString("") { symbols.random().toString() }.dropLastWhile { it == '/' }

        private fun nSlashes(len: Int): String =
            (1..len).joinToString("") { "/" }


        @Test
        fun `Test trailing slash without slashes`() = repeat(100) {
            val str = randomString(Random.nextInt(0, 100))

            assertEquals("$str/", str.withTrailingSlash())
            assertEquals(str, str.withTrailingSlash(false))
        }

        @Test
        fun `Test trailing slash with slashes`() = repeat(100) {
            val base = randomString(Random.nextInt(5, 20))
            val str = base + nSlashes(Random.nextInt(1, 5))

            assertEquals("$base/", str.withTrailingSlash())
            assertEquals(base, str.withTrailingSlash(false))
        }
    }
}