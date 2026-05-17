package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.icon.SakuwiseIcons
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalLayoutApi::class)
class SakuwiseIconsScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun sakuwiseIcons_all_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FlowRow(
                        modifier = Modifier.padding(SakuwiseSpacing.l),
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        allTestIcons.forEach { (name, icon) ->
                            Column(
                                modifier = Modifier.size(SakuwiseSpacing.xxxxxl),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(SakuwiseSpacing.xxl),
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun sakuwiseIcons_all_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FlowRow(
                        modifier = Modifier.padding(SakuwiseSpacing.l),
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        allTestIcons.forEach { (name, icon) ->
                            Column(
                                modifier = Modifier.size(SakuwiseSpacing.xxxxxl),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(SakuwiseSpacing.xxl),
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val allTestIcons = listOf(
    "Home" to SakuwiseIcons.Home,
    "Plan" to SakuwiseIcons.Plan,
    "Plus" to SakuwiseIcons.Plus,
    "Assets" to SakuwiseIcons.Assets,
    "Me" to SakuwiseIcons.Me,
    "HomeFilled" to SakuwiseIcons.HomeFilled,
    "PlanFilled" to SakuwiseIcons.PlanFilled,
    "AssetsFilled" to SakuwiseIcons.AssetsFilled,
    "MeFilled" to SakuwiseIcons.MeFilled,
    "Back" to SakuwiseIcons.Back,
    "Close" to SakuwiseIcons.Close,
    "More" to SakuwiseIcons.More,
    "Search" to SakuwiseIcons.Search,
    "ChevronRight" to SakuwiseIcons.ChevronRight,
    "ChevronDown" to SakuwiseIcons.ChevronDown,
    "ChevronUp" to SakuwiseIcons.ChevronUp,
    "Edit" to SakuwiseIcons.Edit,
    "Trash" to SakuwiseIcons.Trash,
    "Copy" to SakuwiseIcons.Copy,
    "Check" to SakuwiseIcons.Check,
    "Camera" to SakuwiseIcons.Camera,
    "Calendar" to SakuwiseIcons.Calendar,
    "Filter" to SakuwiseIcons.Filter,
    "Bell" to SakuwiseIcons.Bell,
    "Shield" to SakuwiseIcons.Shield,
    "Eye" to SakuwiseIcons.Eye,
    "EyeOff" to SakuwiseIcons.EyeOff,
    "Expense" to SakuwiseIcons.Expense,
    "Income" to SakuwiseIcons.Income,
    "Transfer" to SakuwiseIcons.Transfer,
    "Cash" to SakuwiseIcons.Cash,
    "Bank" to SakuwiseIcons.Bank,
    "Wallet" to SakuwiseIcons.Wallet,
    "Gold" to SakuwiseIcons.Gold,
    "Land" to SakuwiseIcons.Land,
    "Deposit" to SakuwiseIcons.Deposit,
    "Link" to SakuwiseIcons.Link,
    "Receipt" to SakuwiseIcons.Receipt,
    "ArrowUpRight" to SakuwiseIcons.ArrowUpRight,
    "ArrowDownLeft" to SakuwiseIcons.ArrowDownLeft,
    "Swap" to SakuwiseIcons.Swap,
    "Leaf" to SakuwiseIcons.Leaf,
    "Warning" to SakuwiseIcons.Warning,
    "Info" to SakuwiseIcons.Info,
    "Sparkle" to SakuwiseIcons.Sparkle,
)
