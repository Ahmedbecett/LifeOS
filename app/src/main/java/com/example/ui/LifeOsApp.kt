package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.LocalizationManager
import com.example.ui.ai.AiAssistantScreen
import com.example.ui.career.CareerScreen
import com.example.ui.dashboard.SmartDashboardScreen
import com.example.ui.finance.FinanceScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.shopping.ShoppingScreen
import com.example.ui.study.StudyScreen
import com.example.ui.tasks.TasksScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoAccent
import com.example.ui.travel.TravelScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOsApp(viewModel: LifeOsViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val strings = LocalizationManager.getStrings(currentLanguage)

    var showLanguageMenu by remember { mutableStateOf(false) }

    // Intercept back button if on sub-screen
    BackHandler(enabled = currentScreen != LifeOsScreen.DASHBOARD) {
        viewModel.navigateTo(LifeOsScreen.DASHBOARD)
    }

    val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LifeOS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyanAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "AI",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CyanAccent
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Quick AI button
                            IconButton(
                                onClick = { viewModel.navigateTo(LifeOsScreen.AI_ASSISTANT) },
                                modifier = Modifier.testTag("top_quick_ai_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Assistant",
                                    tint = CyanAccent
                                )
                            }

                            // Language switcher dropdown button
                            Box {
                                IconButton(
                                    onClick = { showLanguageMenu = true },
                                    modifier = Modifier.testTag("top_language_switcher_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Language",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = showLanguageMenu,
                                    onDismissRequest = { showLanguageMenu = false }
                                ) {
                                    AppLanguage.values().forEach { lang ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = lang.displayName,
                                                    fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (lang == currentLanguage) CyanAccent else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                viewModel.setLanguage(lang)
                                                showLanguageMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Settings Button
                            IconButton(
                                onClick = { viewModel.navigateTo(LifeOsScreen.SETTINGS) },
                                modifier = Modifier.testTag("top_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Hub Quick Navigation Strip for all 9 core hubs
                        val allScreens = listOf(
                            LifeOsScreen.DASHBOARD to strings.navDashboard,
                            LifeOsScreen.AI_ASSISTANT to strings.navAiAssistant,
                            LifeOsScreen.TRAVEL to strings.navTravel,
                            LifeOsScreen.STUDY to strings.navStudy,
                            LifeOsScreen.CAREER to strings.navCareer,
                            LifeOsScreen.FINANCE to strings.navFinance,
                            LifeOsScreen.TASKS to strings.navTasks,
                            LifeOsScreen.SHOPPING to strings.navShopping,
                            LifeOsScreen.SETTINGS to strings.navSettings
                        )

                        val selectedIndex = allScreens.indexOfFirst { it.first == currentScreen }.coerceAtLeast(0)

                        ScrollableTabRow(
                            selectedTabIndex = selectedIndex,
                            edgePadding = 12.dp,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = CyanAccent,
                            divider = {}
                        ) {
                            allScreens.forEachIndexed { index, (screen, title) ->
                                Tab(
                                    selected = currentScreen == screen,
                                    onClick = { viewModel.navigateTo(screen) },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("tab_${screen.name}")
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    val navItems = listOf(
                        Triple(LifeOsScreen.DASHBOARD, strings.navDashboard, Icons.Default.Dashboard),
                        Triple(LifeOsScreen.AI_ASSISTANT, strings.navAiAssistant, Icons.Default.AutoAwesome),
                        Triple(LifeOsScreen.TRAVEL, strings.navTravel, Icons.Default.Flight),
                        Triple(LifeOsScreen.FINANCE, strings.navFinance, Icons.Default.AccountBalanceWallet),
                        Triple(LifeOsScreen.TASKS, strings.navTasks, Icons.Default.Checklist)
                    )

                    navItems.forEach { (screen, label, icon) ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = CyanAccent.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.name}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    LifeOsScreen.DASHBOARD -> SmartDashboardScreen(viewModel = viewModel)
                    LifeOsScreen.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
                    LifeOsScreen.TRAVEL -> TravelScreen(viewModel = viewModel)
                    LifeOsScreen.STUDY -> StudyScreen(viewModel = viewModel)
                    LifeOsScreen.CAREER -> CareerScreen(viewModel = viewModel)
                    LifeOsScreen.FINANCE -> FinanceScreen(viewModel = viewModel)
                    LifeOsScreen.TASKS -> TasksScreen(viewModel = viewModel)
                    LifeOsScreen.SHOPPING -> ShoppingScreen(viewModel = viewModel)
                    LifeOsScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
