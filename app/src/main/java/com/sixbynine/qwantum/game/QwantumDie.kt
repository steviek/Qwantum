package com.sixbynine.qwantum.game

import com.sixbynine.qwantum.game.QwantumColor.Blue
import com.sixbynine.qwantum.game.QwantumColor.Purple
import com.sixbynine.qwantum.game.QwantumColor.Red
import com.sixbynine.qwantum.game.QwantumColor.Yellow

data class QwantumDieSide(val color: QwantumColor, val digit: Int) {
    init {
        require(digit in 1..6)
    }
}

enum class QwantumDie(val sides: List<QwantumDieSide>) {
    One(Red to 1, Red to 4, Yellow to 5, Blue to 5, Blue to 1, Purple to 6),
    Two(Red to 6, Red to 2, Yellow to 1, Blue to 5, Yellow to 3, Purple to 4),
    Three(Blue to 4, Red to 3, Red to 1, Yellow to 4, Yellow to 2, Purple to 3),
    Four(Blue to 2, Blue to 4, Purple to 1, Red to 5, Yellow to 6, Purple to 3),
    Five(Purple to 2, Purple to 5, Red to 3, Blue to 2, Blue to 6, Yellow to 4),
    Six(Yellow to 1, Yellow to 5, Red to 6, Purple to 6, Purple to 2, Blue to 3)
    ;

    constructor(vararg sides: Pair<QwantumColor, Int>):
            this(sides.map { QwantumDieSide(it.first, it.second) })

    init {
        require(sides.size == 6)
    }
}
