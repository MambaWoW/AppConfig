package io.github.mambawow.appconfig.panel.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Application icons for the configuration panel
 * Following Single Responsibility Principle - only provides icon components
 */
object AppIcons {

    val ChevronBackward: ImageVector by lazy {
        Builder(name = "ChevronBackward", defaultWidth = 11.4609.dp,
            defaultHeight = 20.3555.dp, viewportWidth = 11.4609f, viewportHeight =
                20.3555f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(0.0f, 10.1719f)
                curveTo(0.0f, 10.4648f, 0.1055f, 10.7227f, 0.3281f, 10.9453f)
                lineTo(9.6211f, 20.0273f)
                curveTo(9.8203f, 20.2383f, 10.0781f, 20.3438f, 10.3828f, 20.3438f)
                curveTo(10.9922f, 20.3438f, 11.4609f, 19.8867f, 11.4609f, 19.2773f)
                curveTo(11.4609f, 18.9727f, 11.332f, 18.7148f, 11.1445f, 18.5156f)
                lineTo(2.6133f, 10.1719f)
                lineTo(11.1445f, 1.8281f)
                curveTo(11.332f, 1.6289f, 11.4609f, 1.3594f, 11.4609f, 1.0664f)
                curveTo(11.4609f, 0.457f, 10.9922f, 0.0f, 10.3828f, 0.0f)
                curveTo(10.0781f, 0.0f, 9.8203f, 0.1055f, 9.6211f, 0.3047f)
                lineTo(0.3281f, 9.3984f)
                curveTo(0.1055f, 9.6094f, 0.0f, 9.8789f, 0.0f, 10.1719f)
                close()
            }
        }.build()
    }

    val ChevronForward: ImageVector by lazy {
        Builder(name = "ChevronForward", defaultWidth = 11.4609.dp, defaultHeight
        = 20.3555.dp, viewportWidth = 11.4609f, viewportHeight = 20.3555f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(11.4609f, 10.1719f)
                curveTo(11.4609f, 9.8789f, 11.3438f, 9.6094f, 11.1211f, 9.3984f)
                lineTo(1.8398f, 0.3047f)
                curveTo(1.6289f, 0.1055f, 1.3711f, 0.0f, 1.0664f, 0.0f)
                curveTo(0.4688f, 0.0f, 0.0f, 0.457f, 0.0f, 1.0664f)
                curveTo(0.0f, 1.3594f, 0.1172f, 1.6289f, 0.3047f, 1.8281f)
                lineTo(8.8359f, 10.1719f)
                lineTo(0.3047f, 18.5156f)
                curveTo(0.1172f, 18.7148f, 0.0f, 18.9727f, 0.0f, 19.2773f)
                curveTo(0.0f, 19.8867f, 0.4688f, 20.3438f, 1.0664f, 20.3438f)
                curveTo(1.3711f, 20.3438f, 1.6289f, 20.2383f, 1.8398f, 20.0273f)
                lineTo(11.1211f, 10.9453f)
                curveTo(11.3438f, 10.7227f, 11.4609f, 10.4648f, 11.4609f, 10.1719f)
                close()
            }
        }.build()
    }

    @Composable
    fun RadioButtonSelected(
        modifier: Modifier = Modifier,
        color: Color = Color.Blue
    ) {
        Canvas(modifier = modifier) {
            drawCircle(
                color = color,
                radius = size.minDimension / 2f,
                center = Offset(size.width / 2f, size.height / 2f)
            )
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 4f,
                center = Offset(size.width / 2f, size.height / 2f)
            )
        }
    }

    @Composable
    fun RadioButtonUnselected(
        modifier: Modifier = Modifier,
        color: Color = Color.Gray
    ) {
        Canvas(modifier = modifier) {
            drawCircle(
                color = color,
                radius = size.minDimension / 2f,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
} 