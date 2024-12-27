package com.github.janmalch.shed.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.janmalch.shed.R
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
    var isExpanded by rememberSaveable { mutableStateOf(false) }
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
                .run {
                    if (item.stackTrace == null) this
                    else clickable { isExpanded = !isExpanded }
                }
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.tag ?: "",
                        fontSize = 12.sp,
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                    )
                    if (item.stackTrace != null) {
                        val rotation by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            label = "chevron_rotation_${item.id}"
                        )
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.toggle_stack_trace),
                            modifier = Modifier
                                .size(20.dp)
                                .alpha(0.7f)
                                .rotate(rotation),
                        )
                    }
                }
                Text(
                    text = LocalUiDateTimeFormatter.current.format(item.timestamp),
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                )
            }
            Text(text = item.message)

            if (item.stackTrace != null) {
                AnimatedVisibility(visible = isExpanded, label = "${item.id}_expansion") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = item.stackTrace ?: "",
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
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
        HorizontalDivider()
    }
}

