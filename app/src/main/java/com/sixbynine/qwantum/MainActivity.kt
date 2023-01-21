package com.sixbynine.qwantum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sixbynine.qwantum.game.QwantumColor
import com.sixbynine.qwantum.game.QwantumDie
import com.sixbynine.qwantum.game.QwantumDieSide
import com.sixbynine.qwantum.ui.theme.QwantumTheme
import java.util.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QwantumTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(254, 248, 238)
                ) {
                    var state by remember { mutableStateOf(AppState()) }
                    Box(Modifier.padding(16.dp)) {
                        when (state.screen) {
                            SelectedScreen.Rolling -> {
                                RollingScreen(
                                    state.rollingState,
                                    onStateChanged = { state = state.copy(rollingState = it) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                ScoringScreenView(
                                    state = state.scoringState,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }


                        if (false) {
                            Image(
                                painterResource(id = R.drawable.ic_switch),
                                contentScale = ContentScale.FillBounds,
                                contentDescription = "Switch",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(48.dp)
                                    .padding(12.dp)
                                    .clickable {
                                        state = state.copy(
                                            screen = when (state.screen) {
                                                SelectedScreen.Rolling -> SelectedScreen.Scoring
                                                SelectedScreen.Scoring -> SelectedScreen.Rolling
                                            }
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class SelectedScreen { Rolling, Scoring }

private fun roll(state: DieBoxState): DieBoxState {
    val rollAll = (!state.isMainDieSelected && state.dice.none { it.isSelected }) ||
            (state.isMainDieSelected && state.dice.all { it.isSelected })

    val rollMain = state.isMainDieSelected || rollAll
    val newMain = if (rollMain) Random().nextInt(6) + 1 else state.mainDieSide

    val newDice = arrayListOf<DieState>()

    state.dice.forEach {
        if (it.isSelected || rollAll) {
            newDice += DieState(it.die, Random().nextInt(6), false)
        } else {
            newDice += it
        }
    }

    if (rollAll) {
        newDice.shuffle()
    }

    return DieBoxState(newDice, newMain)
}

data class DieState(
    val die: QwantumDie,
    val sideIndex: Int,
    val isSelected: Boolean = false
)

data class DieBoxState(
    val dice: List<DieState> = QwantumDie.values().map { DieState(it, 0) },
    val mainDieSide: Int = 1,
    val isMainDieSelected: Boolean = false,
)

data class AppState(
    val screen: SelectedScreen = SelectedScreen.Rolling,
    val rollingState: DieBoxState = DieBoxState(),
    val scoringState: ScoringState = ScoringState(),
)

data class ScoringRow(
    val color: QwantumColor,
    val values: List<Int> = emptyList()
)

data class ScoringState(
    val faults: Int = 0,
    val rows: List<ScoringRow> = listOf(
        ScoringRow(QwantumColor.Purple),
        ScoringRow(QwantumColor.Blue),
        ScoringRow(QwantumColor.Red),
        ScoringRow(QwantumColor.Yellow)
    )
)

@Composable
fun ScoringScreenView(
    state: ScoringState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.border(1.dp, Color.Black)) {
        Row(Modifier.background(Color(191, 218, 162))) {
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                state.rows.forEachIndexed { index, scoringRow ->
                    ScoringRowView(scoringRow)
                    if (index < 3) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.width(48.dp))
            FailBox(state)
            Spacer(modifier = Modifier.width(24.dp))
        }
        ScoringSummaryView(state)
    }
}

@Composable
fun ScoringSummaryView(state: ScoringState) {
    Column {
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.height(48.dp)) {
            Spacer(modifier = Modifier.width(28.dp))
            repeat(6) { index ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, Color.Black)
                ) {
                    val score = getScoreForColumn(state, index)
                    Text(score?.toString() ?: "", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(if (index == 3) 12.dp else 8.dp))
            }

            Spacer(modifier = Modifier.width(60.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, Color.Black)
            ) {
                var score = (0 until 6).mapNotNull { getScoreForColumn(state, it) }.sum()
                repeat(state.faults) {
                    score -= it + 1
                }
                if (score <= 0) score = 0
                Text(score.takeIf { it > 0 }?.toString() ?: "", fontSize = 20.sp)
            }
        }
    }

}

@Composable
fun FailBox(state: ScoringState) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        repeat(5) { index ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(32.dp)) {
                Text("-${index + 1}", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(if (state.faults >= index + 1) Color.Red else Color.White)
                        .border(1.dp, Color.Black)
                )
            }
        }
    }
}

private fun getScoreForColumn(state: ScoringState, columnIndex: Int): Int? {
    val entries = state.rows.mapNotNull { it.values.getOrNull(columnIndex) }
    if (entries.size != 4) return null
    val sortedValues = entries.distinct().sorted()
    return sortedValues.getOrNull(1) ?: sortedValues.first()
}

@Composable
fun ScoringRowView(row: ScoringRow) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
    ) {
        repeat(6) { index ->
            ScoringRowCellView(color = row.color, value = row.values.getOrNull(index))
            if (index == 3) {
                Column(modifier = Modifier.background(row.color.color)) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(4.dp)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun ScoringRowCellView(color: QwantumColor, value: Int?) {
    Box(
        modifier = Modifier
            .background(color.color)
            .padding(4.dp)
            .size(32.dp)
    ) {
        NumberInColorCircle(Color.White, value)
    }
}

@Composable
fun RollingScreen(
    state: DieBoxState,
    modifier: Modifier = Modifier,
    onStateChanged: (DieBoxState) -> Unit = {}
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DieBox(state = state, onStateChanged = onStateChanged)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onStateChanged(roll(state)) }) {
                Text("Roll", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun DieBox(state: DieBoxState, onStateChanged: (DieBoxState) -> Unit = {}) {
    Row(
        Modifier
            .background(Color(254, 248, 238))
            .padding(16.dp)
    ) {
        Die(Color.White, state.mainDieSide, state.isMainDieSelected, onClick = {
            onStateChanged(state.copy(isMainDieSelected = !state.isMainDieSelected))
        })
        Spacer(modifier = Modifier.width(12.dp))
        state.dice.forEachIndexed { index, (die, sideIndex, isSelected) ->
            Die(die.sides[sideIndex], isSelected, onClick = {
                val newDice = state.dice.map {
                    if (it.die == die) {
                        it.copy(isSelected = !it.isSelected)
                    } else {
                        it
                    }
                }
                onStateChanged(state.copy(dice = newDice))
            })
            if (index != state.dice.lastIndex) {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
fun Die(side: QwantumDieSide, isSelected: Boolean, onClick: () -> Unit = {}) {
    Die(side.color.color, side.digit, isSelected, onClick)
}

@Composable
fun Die(dieColor: Color, digit: Int, isSelected: Boolean, onClick: () -> Unit = {}) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .border(0.5.dp, Color.Black, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color(0, 110, 51) else Color.White)
            .clickable(onClick = onClick)
            .padding(8.dp)
            .size(64.dp)
    ) {
        NumberInColorCircle(dieColor, digit)
    }
}

@Composable
fun NumberInColorCircle(circleColor: Color, number: Int?) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(circleColor)
            .fillMaxSize()

    ) {
        val textColor = if (circleColor == Color.White) Color.Black else Color.White
        Text(text = number?.toString().orEmpty(), color = textColor, fontSize = 32.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    QwantumTheme {
        Surface(Modifier.background(Color(254, 248, 238))) {
            DieBox(
                DieBoxState()
            )
        }
    }
}