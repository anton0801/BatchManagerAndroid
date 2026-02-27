package com.managerbatching.batchmanager.ui.batch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.model.*
import com.managerbatching.batchmanager.ui.dashboard.toEmoji
import com.managerbatching.batchmanager.ui.theme.*
import com.managerbatching.batchmanager.viewmodel.BatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchCreatorScreen(
    viewModel: BatchViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var eggType by remember { mutableStateOf(EggType.CHICKEN) }
    var quantity by remember { mutableStateOf(10) }
    var temperature by remember { mutableStateOf("37.5") }
    var humidity by remember { mutableStateOf("60") }
    var incubationDays by remember { mutableStateOf("21") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(visible, enter = fadeIn()) {
        Scaffold(
            containerColor = BackgroundYellow,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "New Batch",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextBrown
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, "Cancel", tint = TextBrown)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundYellow)
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Name
                SectionCard {
                    Text("Batch Name", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = TextBrownSoft, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = false },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Spring Hatch 2024", fontFamily = NunitoFont) },
                        isError = nameError,
                        supportingText = if (nameError) {{ Text("Name is required", color = CriticalRed) }} else null,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = PrimaryYellow.copy(0.5f),
                            focusedContainerColor = CreamPanel,
                            unfocusedContainerColor = CreamPanel
                        )
                    )
                }

                // Egg type
                SectionCard {
                    Text("Egg Type", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = TextBrownSoft, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EggType.values().forEach { type ->
                            val selected = eggType == type
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        2.dp,
                                        if (selected) AccentOrange else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .background(if (selected) AccentOrange.copy(0.15f) else CreamPanel)
                                    .clickable { eggType = type }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(type.toEmoji(), fontSize = 22.sp)
                                Text(
                                    type.name.lowercase().replaceFirstChar { it.uppercase() }.take(5),
                                    fontFamily = NunitoFont,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) AccentOrange else TextBrownSoft
                                )
                            }
                        }
                    }
                }

                // Quantity
                SectionCard {
                    Text("Number of Eggs", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = TextBrownSoft, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SpringButton(
                            text = "−",
                            onClick = { if (quantity > 1) quantity-- },
                            color = CardSandy,
                            textColor = TextBrown,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            quantity.toString(),
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = TextBrown,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        SpringButton(
                            text = "+",
                            onClick = { quantity++ },
                            color = PrimaryYellow,
                            textColor = TextBrown,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Incubation params
                SectionCard {
                    Text("Incubation Settings", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = TextBrownSoft, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ParamField("Temperature °C", temperature, Modifier.weight(1f)) { temperature = it }
                        ParamField("Humidity %", humidity, Modifier.weight(1f)) { humidity = it }
                        ParamField("Days", incubationDays, Modifier.weight(1f)) { incubationDays = it }
                    }
                }

                // Notes
                SectionCard {
                    Text("Notes (optional)", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = TextBrownSoft, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Any notes about this batch...", fontFamily = NunitoFont) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = PrimaryYellow.copy(0.5f),
                            focusedContainerColor = CreamPanel,
                            unfocusedContainerColor = CreamPanel
                        )
                    )
                }

                // Buttons
                SpringButton(
                    text = "🥚  Save Batch",
                    onClick = {
                        if (name.isBlank()) { nameError = true; return@SpringButton }
                        val batch = Batch(
                            name = name,
                            eggType = eggType,
                            totalEggs = quantity,
                            incubationParams = IncubationParams(
                                temperature = temperature.toFloatOrNull() ?: 37.5f,
                                humidity = humidity.toFloatOrNull() ?: 60f,
                                incubationDays = incubationDays.toIntOrNull() ?: 21
                            ),
                            notes = notes,
                            status = BatchStatus.PENDING
                        )
                        viewModel.createBatch(batch)
                        onSaved()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryYellow
                )
                SpringButton(
                    text = "Cancel",
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentOrange,
                    textColor = Color.White
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ParamField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    Column(modifier) {
        Text(label, fontFamily = NunitoFont, fontSize = 11.sp, color = TextBrownSoft, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentOrange,
                unfocusedBorderColor = PrimaryYellow.copy(0.5f),
                focusedContainerColor = CreamPanel,
                unfocusedContainerColor = CreamPanel
            ),
            textStyle = LocalTextStyle.current.copy(fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = TextBrown)
        )
    }
}