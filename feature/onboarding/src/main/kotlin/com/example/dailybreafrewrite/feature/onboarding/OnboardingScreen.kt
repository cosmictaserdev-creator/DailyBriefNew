package com.example.dailybreafrewrite.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460),
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Welcome to",
                color = Color(0xFF8E8E93),
                fontSize = 18.sp,
            )
            Text(
                text = "DailyBrief",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pick your interests to personalize your feed",
                color = Color(0xFF8E8E93),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.allInterests, key = { it.name }) { interest ->
                    val isSelected = state.selectedInterests.contains(interest.name)
                    InterestCard(
                        interest = interest,
                        isSelected = isSelected,
                        onClick = { viewModel.onAction(OnboardingAction.ToggleInterest(interest.name)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCompleted,
                enabled = state.selectedInterests.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A84FF),
                    disabledContainerColor = Color(0xFF2C2C2E),
                ),
            ) {
                Text(
                    text = if (state.selectedInterests.isNotEmpty())
                        "Get Started"
                    else
                        "Select at least one interest",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InterestCard(
    interest: Interest,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "interestScale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFF0A84FF).copy(alpha = 0.2f)
                else Color(0xFF1C1C1E)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = interest.emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = interest.name,
                color = if (isSelected) Color.White else Color(0xFF8E8E93),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(animationSpec = spring(stiffness = 300f)) + fadeIn(),
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
