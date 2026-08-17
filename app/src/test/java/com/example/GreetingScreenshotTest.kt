package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.LedgerSummary
import com.example.ui.components.HeroAccountCard
import com.example.ui.theme.MoneyLedgerTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val dummySummary = LedgerSummary(
      totalAccountBalancePaise = 1850000L,
      businessMoneyRemainingPaise = 1200000L,
      personalMoneyRemainingPaise = 650000L,
      businessMoneyUsedPersonallyPaise = 150000L,
      bankBalancePaise = 1850000L,
      cashBalancePaise = 0L
    )

    composeTestRule.setContent {
      MoneyLedgerTheme {
        HeroAccountCard(
          summary = dummySummary,
          businessName = "Mom's Boutique",
          onSettleClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/hero_card.png")
  }
}
