package io.github.brainage04.maidex.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogRepositoryTest {
    @Test
    fun catalogSearchTextIncludesSongAndChartMetadata() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val charts = CatalogRepository(context).load().charts
        val chart = charts.single { it.noteDesigner == "BLaCK rOSE dIsEASe pATiENT" }

        assertTrue(chart.searchableText.contains(normalizeSearch(chart.title)))
        assertTrue(chart.searchableText.contains(normalizeSearch(chart.artist)))
        assertTrue(chart.searchableText.contains(normalizeSearch(chart.noteDesigner.orEmpty())))
    }

    @Test
    fun damagedInstalledCatalogFallsBackToTheBundledDatabase() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val installed = java.io.File(
            context.filesDir,
            "catalog-${io.github.brainage04.maidex.BuildConfig.VERSION_CODE}.db",
        )
        installed.writeText("not a SQLite database ".repeat(64))

        val charts = CatalogRepository(context).load().charts

        assertTrue(charts.isNotEmpty())
    }
}
