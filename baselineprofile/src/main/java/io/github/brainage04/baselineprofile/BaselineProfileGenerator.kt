package io.github.brainage04.maidex.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE_NAME = "io.github.brainage04.maidex"

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startup() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        assertTrue(
            "Catalog did not finish loading",
            device.wait(Until.hasObject(By.textContains("songs")), 10_000),
        )
        device.findObject(By.desc("Open navigation menu")).click()
        assertTrue(
            "Navigation drawer did not open",
            device.wait(Until.hasObject(By.text("DX NET account")), 5_000),
        )
        device.findObject(By.text("DX NET account")).click()
        assertTrue(
            "Account dialog did not open",
            device.wait(Until.hasObject(By.text("maimai DX NET")), 5_000),
        )
        device.pressBack()
        device.findObject(By.desc("Open navigation menu")).click()
        assertTrue(
            "Navigation drawer did not reopen",
            device.wait(Until.hasObject(By.text("Dan courses")), 5_000),
        )
        device.findObject(By.text("Dan courses")).click()
        assertTrue(
            "Dan courses did not open",
            device.wait(Until.hasObject(By.text("Dan type")), 5_000),
        )
    }
}
