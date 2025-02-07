package com.github.janmalch.shed.ui

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.janmalch.shed.database.LogEntity


private fun priorityLabel(priority: Int): String = when (priority) {
    Log.VERBOSE -> "VERBOSE"
    Log.DEBUG -> "DEBUG"
    Log.INFO -> "INFO"
    Log.WARN -> "WARN"
    Log.ERROR -> "ERROR"
    Log.ASSERT -> "ASSERT"
    else -> "?"
}

private fun priorityColor(priority: Int): Color = when (priority) {
    Log.VERBOSE -> Color(0xFF607d8b)
    Log.DEBUG -> Color(0xFF00897b)
    Log.INFO -> Color(0xFF1e88e5)
    Log.WARN -> Color(0xFFffc107)
    Log.ERROR -> Color(0xFFe53935)
    Log.ASSERT -> Color(0xFFb71c1c)
    else -> Color.White
}

@Composable
internal fun LogEntry(
    item: LogEntity,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val borderWidth = with(LocalDensity.current) { 4.dp.toPx() }
        val borderColor = priorityColor(item.priority)
        Column(
            modifier = Modifier
                .testTag("log_entry_${item.id}")
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        borderColor,
                        center = Offset(x = 0f, y = borderWidth * 2),
                        radius = borderWidth
                    )
                    drawRect(
                        borderColor,
                        topLeft = Offset(x = 0f, y = borderWidth * 2),
                        size = Size(width = borderWidth, height = size.height - borderWidth * 4)
                    )
                    drawCircle(
                        borderColor,
                        center = Offset(x = 0f, y = size.height - borderWidth * 2),
                        radius = borderWidth
                    )
                }
                .padding(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = item.tag ?: "",
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                )
                Text(
                    text = LocalUiDateTimeFormatter.current.format(item.timestamp),
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                )
            }

            if (item.message.length > 100) {
                val abbreviated = remember(item.message) { item.message.take(100) + "…" }
                var isCollapsed by rememberSaveable { mutableStateOf(true) }
                Text(
                    text = if (isCollapsed) abbreviated else item.message,
                    modifier = Modifier
                        .animateContentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isCollapsed = !isCollapsed }
                )
            } else {
                Text(text = item.message)
            }

            if (item.stackTrace != null) {
                Spacer(Modifier.height(8.dp))

                val abbreviated =
                    remember(item.stackTrace) { item.stackTrace.substringBefore('\n') }
                var isCollapsed by rememberSaveable { mutableStateOf(true) }

                Text(
                    text = if (isCollapsed) abbreviated else item.stackTrace,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                        .animateContentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isCollapsed = !isCollapsed }
                )
            }
        }
        HorizontalDivider()
    }
}

