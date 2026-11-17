package com.example.offsuitpoker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class SlangCategory(val label: String) {
    ALL("ALL"),
    THE_ORIGIN("THE ORIGIN"),
    DRAWS_AND_MATH("DRAWS & MATH"),
    TABLE_ACTION("TABLE ACTION"),
    MINDSET("MINDSET")
}

data class PokerSlangTerm(
    val term: String,
    val category: SlangCategory,
    val shortDefinition: String,
    val probabilityOrTip: String? = null,
    val isAppOrigin: Boolean = false
)

val POKER_SLANG_DICTIONARY = listOf(
    PokerSlangTerm(
        term = "RUNNER RUNNER NUTS",
        category = SlangCategory.THE_ORIGIN,
        shortDefinition = "The namesake of this app! An extraordinary miracle where a player hits the exact two consecutive cards on both the Turn and River to construct the absolute unbeatable best hand on the table.",
        probabilityOrTip = "Probability: ~0.1% to ~4% from the flop depending on the draw. The purest embodiment of poker as an unpredictable probability puzzle.",
        isAppOrigin = true
    ),
    PokerSlangTerm(
        term = "RUNNER / BACKDOOR",
        category = SlangCategory.THE_ORIGIN,
        shortDefinition = "Needing consecutive cards on both the Turn and River streets to complete a hand (e.g. runner-runner flush when holding only 3 suited cards after the flop).",
        probabilityOrTip = "Rule of thumb: A backdoor flush hits ~4.2% of the time, adding subtle equity to speculative hands."
    ),
    PokerSlangTerm(
        term = "THE NUTS",
        category = SlangCategory.THE_ORIGIN,
        shortDefinition = "The absolute, unbeatable best possible hand on a given board. If you hold the nuts at showdown, it is mathematically impossible to lose.",
        probabilityOrTip = "Example: On a board of A-K-Q-J-10 without flush possibilities, holding an Ace gives you the Broadway straight—the absolute nuts."
    ),
    PokerSlangTerm(
        term = "FLUSH DRAW",
        category = SlangCategory.DRAWS_AND_MATH,
        shortDefinition = "Holding 4 cards of the same suit, needing just 1 more card on the Turn or River to complete a 5-card Flush.",
        probabilityOrTip = "Math: 9 outs remaining in the deck. With 2 cards to come, hits ~35% of the time (approx. 2-to-1 against)."
    ),
    PokerSlangTerm(
        term = "GUTSHOT (BELLY BUSTER)",
        category = SlangCategory.DRAWS_AND_MATH,
        shortDefinition = "An inside straight draw needing one specific middle card rank to complete (e.g. holding 7-8 on a 10-J-2 flop, needing a 9).",
        probabilityOrTip = "Math: Exactly 4 outs. Hits ~8.5% on the turn and ~16.5% by the river."
    ),
    PokerSlangTerm(
        term = "OPEN-ENDED STRAIGHT DRAW (OESD)",
        category = SlangCategory.DRAWS_AND_MATH,
        shortDefinition = "Four consecutive cards that can be completed at either end (e.g. holding 8-9 on a 6-7-K flop, needing either a 5 or a 10).",
        probabilityOrTip = "Math: 8 outs. Hits ~17% on the next card and ~31.5% by the river."
    ),
    PokerSlangTerm(
        term = "POT ODDS",
        category = SlangCategory.DRAWS_AND_MATH,
        shortDefinition = "The mathematical ratio between the current pot size and the amount required to call. Used to determine if chasing a draw has positive expected value (+EV).",
        probabilityOrTip = "Formula: Bet to Call / (Total Pot after Call). If your hand equity exceeds this percentage, calling is mathematically profitable."
    ),
    PokerSlangTerm(
        term = "OUTS",
        category = SlangCategory.DRAWS_AND_MATH,
        shortDefinition = "The unseen cards remaining in the deck that will improve your hand to a likely winning hand.",
        probabilityOrTip = "The Rule of 4 and 2: Multiply your outs by 4 on the flop (or 2 on the turn) to approximate your winning percentage."
    ),
    PokerSlangTerm(
        term = "EQUITY",
        category = SlangCategory.DRAWS_AND_MATH,
        shortDefinition = "Your mathematical percentage share of the pot based on your likelihood of winning the hand at showdown.",
        probabilityOrTip = "Unlike real life luck, equity is exact math calculated against an opponent's range."
    ),
    PokerSlangTerm(
        term = "BAD BEAT",
        category = SlangCategory.TABLE_ACTION,
        shortDefinition = "Losing a substantial pot when your hand was an overwhelming statistical favorite (often 90%+) when all the chips went into the middle.",
        probabilityOrTip = "Probability law: A 95% favorite still loses 1 out of 20 times. Variance is part of the game."
    ),
    PokerSlangTerm(
        term = "STRADDLE",
        category = SlangCategory.TABLE_ACTION,
        shortDefinition = "An optional voluntary blind bet posted before cards are dealt (typically 2x the Big Blind by Under the Gun), creating an action-packed pot and gaining last action pre-flop.",
        probabilityOrTip = "Increases stakes and creates dramatic pre-flop multi-way tension."
    ),
    PokerSlangTerm(
        term = "ALL-IN (SHOVE / JAM)",
        category = SlangCategory.TABLE_ACTION,
        shortDefinition = "Wagering every single chip in front of you. Forces opponents to make a critical high-stakes decision for their tournament or stack life.",
        probabilityOrTip = "Maximizes fold equity and denies opponents proper pot odds to draw."
    ),
    PokerSlangTerm(
        term = "CHECK-RAISE",
        category = SlangCategory.TABLE_ACTION,
        shortDefinition = "Checking when first to act, and then raising after an opponent bets. A powerful deceptive move used for value or as a semi-bluff.",
        probabilityOrTip = "Demonstrates supreme board texture control."
    ),
    PokerSlangTerm(
        term = "MUCK",
        category = SlangCategory.TABLE_ACTION,
        shortDefinition = "Discarding your hole cards face down into the dead pile without showing them to opponents, conceding the hand while concealing information.",
        probabilityOrTip = "Keeping your cards secret prevents observant opponents from learning your betting tendencies."
    ),
    PokerSlangTerm(
        term = "SHOWDOWN",
        category = SlangCategory.TABLE_ACTION,
        shortDefinition = "The final phase of a hand where remaining active players expose their cards to compare strengths and award the pot.",
        probabilityOrTip = "The climax of every Texas Hold'em hand."
    ),
    PokerSlangTerm(
        term = "TILT",
        category = SlangCategory.MINDSET,
        shortDefinition = "A state of emotional agitation, frustration, or anger leading a player to abandon mathematical logic and play recklessly.",
        probabilityOrTip = "The best poker players treat bad beats as expected statistical variance and remain stone-cold objective."
    ),
    PokerSlangTerm(
        term = "FISH & SHARK",
        category = SlangCategory.MINDSET,
        shortDefinition = "'Fish' refers to a novice recreational player making mathematically negative expectation bets; 'Shark' refers to a disciplined, analytical player.",
        probabilityOrTip = "\"If you can't spot the sucker at the table in your first half hour, then you are the sucker.\""
    )
)

@Composable
fun PokerSlangDialog(
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SlangCategory.ALL) }

    val filteredTerms = remember(searchQuery, selectedCategory) {
        POKER_SLANG_DICTIONARY.filter { term ->
            val matchesCategory = selectedCategory == SlangCategory.ALL || term.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                term.term.contains(searchQuery, ignoreCase = true) ||
                term.shortDefinition.contains(searchQuery, ignoreCase = true) ||
                (term.probabilityOrTip?.contains(searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE608080A))
                .padding(horizontal = 14.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F0F14))
                    .border(1.dp, Color(0xFF272736), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "POKER LEXICON",
                                color = Color(0xFFD4AF37),
                                fontFamily = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "CULTURE, SLANG & THE NAME ORIGIN",
                                color = Color(0xFF71717A),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E1E28),
                                contentColor = Color(0xFFA1A1AA)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "CLOSE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search slang (e.g. runner, nuts, odds)...", color = Color(0xFF525260), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color(0xFF22222E),
                            focusedTextColor = Color(0xFFEDEDED),
                            unfocusedTextColor = Color(0xFFCCCCCC),
                            cursorColor = Color(0xFFD4AF37)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SlangCategory.values()) { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF272314) else Color(0xFF14141A))
                                    .border(
                                        0.75.dp,
                                        if (isSelected) Color(0xFFD4AF37) else Color(0xFF22222E),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat.label,
                                    color = if (isSelected) Color(0xFFD4AF37) else Color(0xFF71717A),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slang List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredTerms) { item ->
                            SlangTermCard(item = item)
                        }

                        if (filteredTerms.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No slang terms matched \"$searchQuery\"",
                                        color = Color(0xFF525260),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlangTermCard(item: PokerSlangTerm) {
    val borderColor = if (item.isAppOrigin) Color(0xFFD4AF37) else Color(0xFF20202C)
    val bgColor = if (item.isAppOrigin) Color(0xFF16140D) else Color(0xFF121217)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(if (item.isAppOrigin) 1.dp else 0.75.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.term,
                    color = if (item.isAppOrigin) Color(0xFFD4AF37) else Color(0xFFEDEDED),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (item.isAppOrigin) Color(0xFF2E260F) else Color(0xFF1B1B24))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.isAppOrigin) "APP NAMESAKE" else item.category.label,
                        color = if (item.isAppOrigin) Color(0xFFE5C058) else Color(0xFF8E8EA0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.shortDefinition,
                color = Color(0xFFCCCCCC),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )

            if (item.probabilityOrTip != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0A0A0E))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = item.probabilityOrTip,
                        color = if (item.isAppOrigin) Color(0xFFD4AF37) else Color(0xFF38BDF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        lineHeight = 13.5.sp
                    )
                }
            }
        }
    }
}
