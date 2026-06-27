package com.example.dailybreafrewrite.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailybreafrewrite.core.domain.model.DateRange
import com.example.dailybreafrewrite.core.domain.model.FilterPreferences
import com.example.dailybreafrewrite.core.domain.model.SortBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    current: FilterPreferences,
    onApply: (FilterPreferences) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(current) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Sort By",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            SortBy.entries.forEach { sort ->
                val isSelected = draft.sortBy == sort
                FilterOptionRow(
                    label = sort.displayName,
                    isSelected = isSelected,
                    onClick = { draft = draft.copy(sortBy = sort) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Date Range",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            DateRange.entries.forEach { range ->
                val isSelected = draft.dateRange == range
                FilterOptionRow(
                    label = range.displayName,
                    isSelected = isSelected,
                    onClick = { draft = draft.copy(dateRange = range) },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(
                    onClick = {
                        draft = FilterPreferences()
                        onApply(FilterPreferences())
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset", color = Color(0xFF8E8E93))
                }
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Apply", color = Color(0xFF0A84FF))
                }
            }
        }
    }
}

@Composable
private fun FilterOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF0A84FF).copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.7f),
        label = "filterBg",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFFE5E5EA),
            fontSize = 16.sp,
        )
    }
}
