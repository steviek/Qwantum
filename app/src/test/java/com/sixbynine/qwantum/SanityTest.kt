package com.sixbynine.qwantum

import com.sixbynine.qwantum.game.QwantumColor
import com.sixbynine.qwantum.game.QwantumDie
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SanityTest {
    @Test
    fun `game is sane`() {
        val colors = QwantumColor.values().map { it.name + " " + getTotal(it) }
        fail(colors.toString())
    }

    private fun getTotal(color: QwantumColor): Int {
        return QwantumDie.values()
            .sumOf { it.sides.filter { it.color == color }.sumOf { it.digit } }
    }
}