package io.github.brainage04.maidex.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TARGET_PACKAGE = "io.github.brainage04.maidex"

@RunWith(AndroidJUnit4::class)
class LoadPerformanceBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupAndCatalogLoad() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.COLD,
        iterations = 5,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
        assertTrue(
            "Catalog did not finish loading",
            device.wait(Until.hasObject(By.textContains("songs")), 10_000),
        )
    }

    @Test
    fun searchDrawerAndDialogLoads() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.textContains("songs")), 10_000)
        },
    ) {
        val search = device.findObject(By.text("Search"))
        search.click()
        search.text = "Garakuta"
        device.waitForIdle()
        device.pressBack()

        device.findObject(By.desc("Open navigation menu")).click()
        device.wait(Until.hasObject(By.text("DX NET account")), 5_000)
        device.findObject(By.text("DX NET account")).click()
        device.wait(Until.hasObject(By.text("maimai DX NET")), 5_000)
        device.pressBack()

        device.findObject(By.desc("Open navigation menu")).click()
        assertTrue(
            "Navigation drawer did not reopen",
            device.wait(Until.hasObject(By.text("Dan courses")), 5_000),
        )
        device.findObject(By.text("Dan courses")).click()
        device.wait(Until.hasObject(By.text("Dan type")), 5_000)
    }
}
