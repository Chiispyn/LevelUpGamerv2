package com.levelupgamer.levelup.ui.auth

import org.junit.Test

class RutFinderTest {
    @Test
    fun findValidRut() {
        for (i in 10000000..99999999) {
            val rut = i.toString()
            if (isValidRut(rut + "0")) {
                java.io.File("valid_rut.txt").writeText(rut + "0")
                throw RuntimeException("FOUND")
            }
            if (isValidRut(rut + "1")) {
                java.io.File("valid_rut.txt").writeText(rut + "1")
                throw RuntimeException("FOUND")
            }
            if (isValidRut(rut + "K")) {
                java.io.File("valid_rut.txt").writeText(rut + "K")
                throw RuntimeException("FOUND")
            }
             if (isValidRut(rut + "9")) {
                java.io.File("valid_rut.txt").writeText(rut + "9")
                throw RuntimeException("FOUND")
            }
        }
    }

    private fun isValidRut(rut: String): Boolean {
        if (rut.length !in 8..9) return false
        try {
            var rutAux = rut.uppercase().replace(".", "").replace("-", "")
            val dv = rutAux.last()
            val numbers = rutAux.substring(0, rutAux.length - 1).toInt()
            var m = 0
            var s = 1
            var t = numbers
            while (t != 0) {
                s = (s + t % 10 * (9 - m++ % 6)) % 11
                t /= 10
            }
            val calculatedDv = if (s != 0) (s + 47).toChar() else 'K'
            return dv == calculatedDv
        } catch (e: Exception) {
            return false
        }
    }
}
