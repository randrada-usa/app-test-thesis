package com.example.fruitgrade.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BurstMode
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fruitgrade.R
import com.example.fruitgrade.ui.theme.FreshGreen
import com.example.fruitgrade.ui.theme.FreshGreenDark
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Data classes
// ---------------------------------------------------------------------------

/** Represents a fruit variety the user can select on the home screen. */
data class FruitVariety(
    val name: String,
    val scientificName: String,
    val borderColor: Color,
    val backgroundColor: Color
)

private val fruitVarieties = listOf(
    FruitVariety(
        name = "Carabao Mango",
        scientificName = "Mangifera Indica var. carabao",
        borderColor = Color(0xFFF9A825),
        backgroundColor = Color(0xFFFFF8E1)
    ),
    FruitVariety(
        name = "Lakatan Banana",
        scientificName = "Musa acuminata cv. Lakatan",
        borderColor = Color(0xFF66BB6A),
        backgroundColor = Color(0xFFE8F5E9)
    ),
    FruitVariety(
        name = "Red Lady Papaya",
        scientificName = "Carica papaya cv. Red Lady",
        borderColor = Color(0xFFEF9A9A),
        backgroundColor = Color(0xFFFCE4EC)
    )
)

// ---------------------------------------------------------------------------
// Model & Mode option data
// ---------------------------------------------------------------------------

private data class ModelOption(
    val fileName: String,
    val label: String,
    val subtitle: String,
    val icon: ImageVector
)

private data class ModeOption(
    val key: String,
    val label: String,
    val subtitle: String,
    val icon: ImageVector
)

private val modelOptions = listOf(
    ModelOption("small_model.tflite", "Small", "Faster · ~1 MB", Icons.Default.Speed),
    ModelOption("large_model.tflite", "Large", "More accurate · ~3 MB", Icons.Default.Memory)
)

private val modeOptions = listOf(
    ModeOption("solo", "Solo", "Single fruit", Icons.Outlined.CenterFocusStrong),
    ModeOption("batch", "Batch", "Up to 5 images", Icons.Outlined.BurstMode)
)

// ---------------------------------------------------------------------------
// HomeScreen
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(
    onScan: (modelName: String, mode: String) -> Unit,
    onHistory: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Stagger-in animation for cards
    var visibleCards by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in fruitVarieties.indices) {
            delay(120L)
            visibleCards = i + 1
        }
    }

    // Track which fruit was tapped → opens model-selection dialog
    var selectedFruit by remember { mutableStateOf<FruitVariety?>(null) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            HomeBottomNavigation(
                selectedRoute = "home",
                onItemSelected = { route ->
                    when (route) {
                        "history" -> onHistory()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Fruit Quality",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = FreshGreenDark,
                    fontSize = 30.sp,
                    lineHeight = 36.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ripeness Classification",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = FreshGreenDark,
                    fontSize = 30.sp,
                    lineHeight = 36.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select fruit variety to begin assessment",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            fruitVarieties.forEachIndexed { index, fruit ->
                AnimatedVisibility(
                    visible = index < visibleCards,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                ) {
                    FruitVarietyCard(
                        fruit = fruit,
                        onClick = { selectedFruit = fruit }
                    )
                }

                if (index < fruitVarieties.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TechBadgesRow()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // -------- Model / Mode selection dialog --------
    selectedFruit?.let { fruit ->
        ModelSelectionDialog(
            fruit = fruit,
            onDismiss = { selectedFruit = null },
            onConfirm = { modelName, mode ->
                selectedFruit = null
                onScan(modelName, mode)
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Modern Model / Mode selection dialog
// ---------------------------------------------------------------------------

@Composable
private fun ModelSelectionDialog(
    fruit: FruitVariety,
    onDismiss: () -> Unit,
    onConfirm: (modelName: String, mode: String) -> Unit
) {
    var selectedModelIndex by remember { mutableIntStateOf(0) }
    var selectedModeIndex by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // ---- Header: fruit name + close ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fruit.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1B1F)
                            )
                        )
                        Text(
                            text = fruit.scientificName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF9E9E9E)
                            )
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---- "Configure scan" label ----
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = FreshGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Configure scan",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = FreshGreen,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---- Model selection ----
                Text(
                    text = "Model",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242),
                        letterSpacing = 0.3.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    modelOptions.forEachIndexed { index, option ->
                        SelectableOptionCard(
                            modifier = Modifier.weight(1f),
                            label = option.label,
                            subtitle = option.subtitle,
                            icon = option.icon,
                            isSelected = selectedModelIndex == index,
                            accentColor = FreshGreen,
                            onClick = { selectedModelIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---- Mode selection ----
                Text(
                    text = "Mode",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242),
                        letterSpacing = 0.3.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    modeOptions.forEachIndexed { index, option ->
                        SelectableOptionCard(
                            modifier = Modifier.weight(1f),
                            label = option.label,
                            subtitle = option.subtitle,
                            icon = option.icon,
                            isSelected = selectedModeIndex == index,
                            accentColor = FreshGreen,
                            onClick = { selectedModeIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ---- Start button ----
                Button(
                    onClick = {
                        onConfirm(
                            modelOptions[selectedModelIndex].fileName,
                            modeOptions[selectedModeIndex].key
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "Start Assessment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Selectable option card (used for both Model and Mode)
// ---------------------------------------------------------------------------

@Composable
private fun SelectableOptionCard(
    modifier: Modifier = Modifier,
    label: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "borderWidth"
    )
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.08f) else Color(0xFFFAFAFA)
    val borderColor = if (isSelected) accentColor else Color(0xFFE0E0E0)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon with check badge
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.15f)
                            else Color(0xFFEEEEEE)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) accentColor else Color(0xFF9E9E9E),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Animated check badge
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF1B1B1F) else Color(0xFF616161)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) accentColor else Color(0xFFBDBDBD),
                    fontSize = 10.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Fruit-variety card
// ---------------------------------------------------------------------------

@Composable
private fun FruitVarietyCard(
    fruit: FruitVariety,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .scale(scale)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = fruit.borderColor.copy(alpha = 0.25f),
                spotColor = fruit.borderColor.copy(alpha = 0.25f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(fruit.backgroundColor.copy(alpha = 0.45f))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fruit.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF212121)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fruit.scientificName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF757575),
                                fontSize = 13.sp
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select ${fruit.name}",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tech badges row
// ---------------------------------------------------------------------------

@Composable
private fun TechBadgesRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "4 grades per variety",
            style = MaterialTheme.typography.labelSmall.copy(
                color = FreshGreen,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
        DotSeparator()
        Text(
            text = "MobileNetV3",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
        DotSeparator()
        Text(
            text = "TfLite",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun DotSeparator() {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(5.dp)
            .clip(CircleShape)
            .background(Color(0xFF9E9E9E))
    )
}

// ---------------------------------------------------------------------------
// Bottom navigation bar
// ---------------------------------------------------------------------------

@Composable
private fun HomeBottomNavigation(
    selectedRoute: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = FreshGreenDark,
        tonalElevation = 0.dp
    ) {
        // Home
        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = { onItemSelected("home") },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(24.dp))
            },
            label = {
                Text("Home", fontSize = 11.sp,
                    fontWeight = if (selectedRoute == "home") FontWeight.Bold else FontWeight.Normal)
            },
            colors = navItemColors()
        )

        // Capture
        NavigationBarItem(
            selected = selectedRoute == "scan",
            onClick = { onItemSelected("scan") },
            icon = {
                Icon(Icons.Default.CameraEnhance, contentDescription = "Capture", modifier = Modifier.size(24.dp))
            },
            label = {
                Text("Capture", fontSize = 11.sp,
                    fontWeight = if (selectedRoute == "scan") FontWeight.Bold else FontWeight.Normal)
            },
            colors = navItemColors()
        )

        // Result — custom drawable
        NavigationBarItem(
            selected = selectedRoute == "result",
            onClick = { onItemSelected("result") },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.ic_result),
                    contentDescription = "Result",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(
                        if (selectedRoute == "result") Color.White
                        else Color.White.copy(alpha = 0.7f)
                    )
                )
            },
            label = {
                Text("Result", fontSize = 11.sp,
                    fontWeight = if (selectedRoute == "result") FontWeight.Bold else FontWeight.Normal)
            },
            colors = navItemColors()
        )

        // Summary
        NavigationBarItem(
            selected = selectedRoute == "history",
            onClick = { onItemSelected("history") },
            icon = {
                Icon(Icons.Default.BarChart, contentDescription = "Summary", modifier = Modifier.size(24.dp))
            },
            label = {
                Text("Summary", fontSize = 11.sp,
                    fontWeight = if (selectedRoute == "history") FontWeight.Bold else FontWeight.Normal)
            },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = Color.White,
    unselectedIconColor = Color.White.copy(alpha = 0.7f),
    unselectedTextColor = Color.White.copy(alpha = 0.7f),
    indicatorColor = Color.White.copy(alpha = 0.15f)
)
