// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.constraintlayout.compose.ConstraintLayout
import cz.lukashoracek.kryptovstupenky.ui.theme.Pink40
import cz.lukashoracek.kryptovstupenky.ui.theme.Purple40
import cz.lukashoracek.kryptovstupenky.ui.theme.PurpleGrey40

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(250, 250, 250),
)

class TicketResult : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ticketValid = intent.getBooleanExtra("ticketValid", false)
        val ticketUID = intent.getByteArrayExtra("ticketUID")
        val userVisibleError = intent.getStringExtra("userVisibleError")

        setContent {
            ActivityComposable(ticketValid, ticketUID, userVisibleError)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Composable
    @Preview
    private fun ActivityComposable(ticketValid: Boolean = false, ticketUID: ByteArray? = null, userVisibleError: String? = "Unknown error") {
        MaterialTheme(
            colorScheme = LightColorScheme,
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val (checkmark, cross, text, button) = createRefs()

                if (ticketValid) {
                    Icon(
                        painterResource(R.drawable.rounded_check_circle_24),
                        "checkmark",
                        modifier = Modifier
                            .constrainAs(checkmark) {
                                top.linkTo(parent.top, margin = 64.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .size(380.dp),
                        tint = Color(76, 175, 80),
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.rounded_block_24),
                        "cross",
                        modifier = Modifier
                            .constrainAs(cross) {
                                top.linkTo(parent.top, margin = 64.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .size(380.dp),
                        tint = Color(220, 0, 0),
                    )
                }

                if (userVisibleError != null) {
                    Text(
                        userVisibleError,
                        modifier = Modifier
                            .constrainAs(text) {
                                top.linkTo(
                                    if (ticketValid) checkmark.bottom else cross.bottom,
                                    margin = 32.dp
                                )
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(32.dp),
                        fontSize = 4.em,
                    )
                } else if (ticketUID != null) {
                    Text(
                        "Ticket ID: 0x" + ticketUID.toHexString(),
                        modifier = Modifier
                            .constrainAs(text) {
                                top.linkTo(
                                    if (ticketValid) checkmark.bottom else cross.bottom,
                                    margin = 32.dp
                                )
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(32.dp),
                        fontSize = 4.em,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    onClick = {
                        finish()
                    },
                    modifier = Modifier
                        .constrainAs(button) {
                            top.linkTo(text.bottom, margin = 32.dp)
                            bottom.linkTo(parent.bottom, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .size(192.dp, 96.dp),
                ) {
                    Text(
                        text = "OK",
                        fontSize = 4.em,
                    )
                }
            }
        }
    }
}