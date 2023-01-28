package com.vkochenkov.fingerbeat

import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vkochenkov.fingerbeat.InMemoryCache.pattern
import java.util.concurrent.CancellationException

@Composable
fun ScreenBody() {
    val context = LocalContext.current
    var isPlay by rememberSaveable { mutableStateOf(false) }
    var isRecord by rememberSaveable { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() } // for ripple

    var lastTimeClick by rememberSaveable {
        mutableStateOf(System.currentTimeMillis())
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Gray)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                enabled = !isPlay,
                onClick = {
                    isRecord = !isRecord
                    if (isRecord) {
                        // clean
                        pattern = mutableListOf()
                        lastTimeClick = System.currentTimeMillis()
                    } else {
                        // do nothing
                    }
                }
            ) {
                Text(
                    text = if (isRecord) {
                        stringResource(R.string.stop_record)
                    } else {
                        stringResource(R.string.start_record)
                    }
                )
            }

            Button(
                enabled = !isRecord,
                onClick = {
                    isPlay = !isPlay
                    val vibrator = context.getSystemService(ComponentActivity.VIBRATOR_SERVICE) as Vibrator
                    if (isPlay && pattern.isNotEmpty()) {
                        vibrator.vibrate(pattern.toLongArray(), 0)
                    } else {
                        vibrator.cancel()
                    }

                }
            ) {
                Text(
                    text = if (isPlay) {
                        stringResource(R.string.stop_play)
                    } else {
                        stringResource(R.string.start_play)
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .indication(interactionSource, LocalIndication.current) // for ripple
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val pressedTime = System.currentTimeMillis()
                            val press = PressInteraction.Press(offset)
                            val released = try {
                                interactionSource.emit(press)
                                tryAwaitRelease()
                            } catch (c: CancellationException) {
                                false
                            }
                            if (released) {
                                interactionSource.emit(PressInteraction.Release(press))
                                val difPause = System.currentTimeMillis() - lastTimeClick
                                val difAction = System.currentTimeMillis() - pressedTime
                                pattern.add(difPause)
                                pattern.add(difAction)
                                lastTimeClick = System.currentTimeMillis()
                            } else {
                                // cancelled
                            }
                        },
                        onTap = {
                            // onTap
                        },
                        onDoubleTap = {
                            // onDoubleTap
                        },
                        onLongPress = {
                            // onLongPress
                        }
                    )
                }
                .fillMaxSize()
                .background(color = Color.LightGray)
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                color = Color.Black,
                text = stringResource(R.string.about)
            )
        }
    }
}


@Preview
@Composable
fun Preview() {
    ScreenBody()
}


