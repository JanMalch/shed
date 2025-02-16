package io.github.janmalch.shed.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.shed.R
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LevelFilterChip(
    selected: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var expanded by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    FilterChip(
        onClick = {
            focusManager.clearFocus(force = true)
            expanded = true
        },
        label = { Text(text = stringResource(R.string.priority), maxLines = 1) },
        selected = selected.size != ALL_PRIORITIES.size,
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
    )

    if (expanded) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { expanded = false },
            dragHandle = null,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                Modifier.padding(
                    start = 12.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = 8.dp,
                ),
            ) {
                IconButton(
                    onClick = {
                        scope
                            .launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    expanded = false
                                }
                            }
                    }
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close))
                }
                Text(text = stringResource(R.string.priority), style = MaterialTheme.typography.titleMedium)
            }
            ALL_PRIORITIES.forEach { priority ->
                var isSelected by remember(selected) { mutableStateOf(priority in selected) }
                val mis = remember { MutableInteractionSource() }
                val onClick: () -> Unit = {
                    if (isSelected) {
                        isSelected = false
                        onSelectionChange(selected - priority)
                    } else {
                        isSelected = true
                        onSelectionChange(selected + priority)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                    Modifier.fillMaxWidth()
                        .clickable(
                            interactionSource = mis,
                            indication = null,
                            onClick = onClick
                        )
                        .padding(horizontal = 12.dp),
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        interactionSource = mis
                    )
                    Text(
                        when (priority) {
                            Log.VERBOSE -> stringResource(R.string.priority_verbose)
                            Log.DEBUG -> stringResource(R.string.priority_debug)
                            Log.INFO -> stringResource(R.string.priority_info)
                            Log.WARN -> stringResource(R.string.priority_warn)
                            Log.ERROR -> stringResource(R.string.priority_error)
                            Log.ASSERT -> stringResource(R.string.priority_assert)
                            else -> ""
                        })
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}