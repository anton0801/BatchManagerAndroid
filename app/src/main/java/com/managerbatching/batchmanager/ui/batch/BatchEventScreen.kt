package com.managerbatching.batchmanager.ui.batch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.model.*
import com.managerbatching.batchmanager.ui.theme.*
import com.managerbatching.batchmanager.viewmodel.BatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEventScreen(
    batchId: String,
    viewModel: BatchViewModel,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf<EventType?>(null) }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundYellow,
        topBar = {
            TopAppBar(
                title = { Text("Add Event", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, color = TextBrown) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = TextBrown) }
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Select Event Type", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = TextBrownSoft, fontSize = 13.sp)

            EventType.entries.chunked(2).forEachIndexed { rowIdx, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEachIndexed { idx, type ->
                        val selected = selectedType == type
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay((rowIdx * 2 + idx) * 60L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 30 }) + fadeIn(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .shadow(if (selected) 6.dp else 2.dp, RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) AccentOrange.copy(0.15f) else CardSandy)
                                    .border(
                                        if (selected) 2.dp else 0.dp,
                                        if (selected) AccentOrange else Color.Transparent,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedType = type }
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(type.emoji, fontSize = 28.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(type.label, fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = if (selected) AccentOrange else TextBrown, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            if (selectedType != null) {
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    SectionCard(backgroundColor = CreamPanel) {
                        Text("Details", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = TextBrownSoft, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))

                        if (selectedType in listOf(EventType.EGGS_ADDED, EventType.EGGS_DISCARDED)) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Quantity", fontFamily = NunitoFont) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentOrange, unfocusedBorderColor = PrimaryYellow.copy(0.5f),
                                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (optional)", fontFamily = NunitoFont) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentOrange, unfocusedBorderColor = PrimaryYellow.copy(0.5f),
                                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }

                SpringButton(
                    text = "💾  Save Event",
                    onClick = {
                        val event = BatchEvent(
                            type = selectedType!!,
                            description = description,
                            quantity = quantity.toIntOrNull()
                        )
                        viewModel.addEvent(batchId, event)
                        onBack()
                    },
                    color = AccentOrange,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}