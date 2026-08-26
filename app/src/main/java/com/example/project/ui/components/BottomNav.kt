package com.example.project.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class NavItem(
    val route: String,
    @DrawableRes val iconRes: Int,
    val contentDescription: String
)

@Composable
fun BottomNavigation(
    items: List<NavItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark =
        MaterialTheme.colorScheme.background.luminance() < 0.5f

    val containerBgColor =
        if (isDark) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.White
        }

    val activeTabBgColor =
        if (isDark) {
            Color.White.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.primary.copy(
                alpha = 0.15f
            )
        }

    val activeIconTint =
        MaterialTheme.colorScheme.primary

    val inactiveIconTint =
        if (isDark) {
            Color.White.copy(alpha = 0.60f)
        } else {
            Color.Black.copy(alpha = 0.60f)
        }

    val borderColor =
        if (isDark) {
            Color.White.copy(alpha = 0.12f)
        } else {
            Color.Black.copy(alpha = 0.08f)
        }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        val totalWidth =
            maxWidth - 16.dp

        val tabWidth =
            totalWidth / items.size

        val indicatorOffset by animateDpAsState(
            targetValue =
                tabWidth * selectedIndex,
            animationSpec =
                tween(
                    durationMillis = 240,
                    easing = FastOutSlowInEasing
                ),
            label = "indicator_slide"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation =
                        if (isDark) {
                            12.dp
                        } else {
                            20.dp
                        },
                    shape =
                        RoundedCornerShape(32.dp),
                    clip = false,
                    ambientColor =
                        Color.Black.copy(
                            alpha =
                                if (isDark) {
                                    0.50f
                                } else {
                                    0.35f
                                }
                        ),
                    spotColor =
                        Color.Black.copy(
                            alpha =
                                if (isDark) {
                                    0.40f
                                } else {
                                    0.25f
                                }
                        )
                ),
            shape =
                RoundedCornerShape(32.dp),
            color =
                containerBgColor,
            border =
                BorderStroke(
                    1.dp,
                    borderColor
                )
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 6.dp
                        )
            ) {
                Surface(
                    modifier =
                        Modifier
                            .offset(
                                x = indicatorOffset
                            )
                            .width(tabWidth)
                            .fillMaxHeight()
                            .padding(
                                horizontal = 4.dp,
                                vertical = 2.dp
                            )
                            .shadow(
                                elevation = 6.dp,
                                shape =
                                    RoundedCornerShape(
                                        24.dp
                                    ),
                                clip = false,
                                ambientColor =
                                    Color.Black.copy(
                                        alpha =
                                            if (isDark) {
                                                0.35f
                                            } else {
                                                0.25f
                                            }
                                    ),
                                spotColor =
                                    Color.Black.copy(
                                        alpha =
                                            if (isDark) {
                                                0.25f
                                            } else {
                                                0.18f
                                            }
                                    )
                            ),
                    shape =
                        RoundedCornerShape(24.dp),
                    color =
                        activeTabBgColor
                ) {}

                Row(
                    modifier =
                        Modifier.fillMaxSize(),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->

                        val isSelected =
                            index == selectedIndex

                        val iconScale by
                        animateFloatAsState(
                            targetValue =
                                if (isSelected) {
                                    1.15f
                                } else {
                                    1.0f
                                },
                            animationSpec =
                                tween(
                                    durationMillis = 180,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            label = "icon_scale"
                        )

                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(
                                        RoundedCornerShape(
                                            24.dp
                                        )
                                    )
                                    .clickable(
                                        interactionSource =
                                            remember {
                                                MutableInteractionSource()
                                            },
                                        indication = null
                                    ) {
                                        onTabSelected(index)
                                    },
                            contentAlignment =
                                Alignment.Center
                        ) {
                            Icon(
                                painter =
                                    painterResource(
                                        id =
                                            item.iconRes
                                    ),
                                contentDescription =
                                    item.contentDescription,
                                tint =
                                    if (isSelected) {
                                        activeIconTint
                                    } else {
                                        inactiveIconTint
                                    },
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX =
                                                iconScale
                                            scaleY =
                                                iconScale
                                        }
                            )
                        }
                    }
                }
            }
        }
    }
}