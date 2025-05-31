package io.github.mambawow.appconfig.panel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mambawow.appconfig.panel.ui.icons.AppIcons.ChevronBackward
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.Divider
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.SystemBlue
import io.github.mambawow.appconfig.panel.ui.theme.AppTextStyles
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType

/**
 * @author Frank
 * @created 5/29/25
 */
@Composable
fun ActionButton(
    actionText: String,
    onClick: () -> Unit
) {
    if (LocalThemeType.current == ThemeType.Material) {
        TextButton(onClick = onClick) {
            Text(
                text = actionText,
                style = AppTextStyles.BodySmall.copy(
                    fontSize = 16.sp
                )
            )
        }
    } else {
        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        Row(
            Modifier
                .graphicsLayer {
                    alpha = if (pressed) 0.33f else 1f
                }.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ).padding(end = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = actionText,
                style = AppTextStyles.BodyNormal.copy(fontSize = 16.sp, color = SystemBlue)
            )
        }
    }
}

@Composable
fun NavigationButton(
    onClick: () -> Unit
) {
    if (LocalThemeType.current == ThemeType.Material) {
        IconButton(onClick = onClick) {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp).size(16.dp),
                imageVector = ChevronBackward,
                contentDescription = "Back"
            )
        }
    } else {
        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        Row(
            Modifier
                .graphicsLayer {
                    alpha = if (pressed) 0.33f else 1f
                }.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(start = 16.dp, end = 4.dp).size(15.dp),
                imageVector = ChevronBackward,
                contentDescription = "Back",
                tint = SystemBlue
            )

            Text(
                text = "Back",
                style = AppTextStyles.BodyNormal.copy(fontSize = 17.sp, color = SystemBlue)
            )
        }
    }
}


@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = Divider
) {
    Spacer(
        modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(color = color)
    )
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = Divider
) {
    Spacer(
        modifier
            .fillMaxHeight()
            .width(0.5.dp)
            .background(color = color)
    )
}