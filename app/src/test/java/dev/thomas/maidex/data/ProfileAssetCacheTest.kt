package dev.thomas.maidex.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.URI

class ProfileAssetCacheTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `official profile decorations are cached and reused offline`() {
        val requestedUrls = mutableListOf<String>()
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                requestedUrls += chain.request().url.toString()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("official-image".toResponseBody("image/png".toMediaType()))
                    .build()
            }
            .build()
        val cache = ProfileAssetCache(temporaryFolder.newFolder(), client)
        val source = PlayerProfile(
            name = "Player",
            officialRating = 16_070,
            region = AccountRegion.INTERNATIONAL,
            titleRarity = "Gold",
            avatarUrl = "https://maimaidx-eng.com/maimai-mobile/img/avatar.png",
            courseRankUrl = "https://maimaidx-eng.com/maimai-mobile/img/course.png",
            classRankUrl = "https://maimaidx-eng.com/maimai-mobile/img/class.png",
        )

        val cached = cache.cache(source)
        val reused = cache.cache(source)
        val assetUrls = listOf(
            cached.avatarUrl,
            cached.courseRankUrl,
            cached.classRankUrl,
            cached.titleBackgroundUrl,
            cached.ratingBaseUrl,
            cached.starIconUrl,
        )

        assertEquals(6, requestedUrls.size)
        assertEquals(cached, reused)
        assetUrls.forEach { assetUrl ->
            assertTrue(assetUrl.startsWith("file:"))
            assertEquals("official-image", File(URI(assetUrl)).readText())
        }
    }

    @Test
    fun `official rating and title assets follow DX NET variants`() {
        assertTrue(DxNetAssets.ratingBaseUrl(16_000).contains("rating_base_rainbow_kiwami.png"))
        assertTrue(DxNetAssets.ratingBaseUrl(15_000).contains("rating_base_rainbow.png"))
        assertTrue(DxNetAssets.ratingBaseUrl(14_500).contains("rating_base_platinum.png"))
        assertTrue(DxNetAssets.ratingBaseUrl(12_000).contains("rating_base_bronze.png"))
        assertTrue(DxNetAssets.titleBackgroundUrl("Gold").contains("trophy_gold.png"))
        assertTrue(DxNetAssets.titleBackgroundUrl("unknown").contains("trophy_normal.png"))
    }
}
