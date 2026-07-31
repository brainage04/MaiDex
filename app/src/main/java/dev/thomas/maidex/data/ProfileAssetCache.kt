package dev.thomas.maidex.data

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

internal class ProfileAssetCache(
    private val directory: File,
    private val client: OkHttpClient = OkHttpClient(),
    private val allowedHosts: Set<String> = setOf("maimaidx-eng.com", "maimaidx.jp"),
) {
    fun cache(profile: PlayerProfile): PlayerProfile {
        val decorated = DxNetAssets.decorate(profile)
        return decorated.copy(
            avatarUrl = cacheAsset("avatar", decorated.avatarUrl),
            courseRankUrl = cacheAsset("course-rank", decorated.courseRankUrl),
            classRankUrl = cacheAsset("class-rank", decorated.classRankUrl),
            titleBackgroundUrl = cacheAsset("title-background", decorated.titleBackgroundUrl),
            ratingBaseUrl = cacheAsset("rating-base", decorated.ratingBaseUrl),
            starIconUrl = cacheAsset("star-icon", decorated.starIconUrl),
        )
    }

    fun clear() {
        directory.deleteRecursively()
    }

    private fun cacheAsset(key: String, sourceUrl: String): String {
        if (sourceUrl.isBlank() || sourceUrl.startsWith("file:")) return sourceUrl
        val url = sourceUrl.toHttpUrlOrNull() ?: return sourceUrl
        if (url.scheme != "https" || allowedHosts.isNotEmpty() && url.host !in allowedHosts) return sourceUrl

        val extension = url.pathSegments.lastOrNull()
            ?.substringAfterLast('.', "img")
            ?.lowercase()
            ?.takeIf { it in SUPPORTED_EXTENSIONS }
            ?: "img"
        val destination = File(directory, "$key-${sourceUrl.sha256().take(16)}.$extension")
        if (destination.isFile && destination.length() > 0L) return destination.toURI().toString()

        val temporary = File(directory, "${destination.name}.tmp")
        return runCatching {
            directory.mkdirs()
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null || body.contentLength() > MAX_ASSET_BYTES) {
                    return sourceUrl
                }
                temporary.outputStream().use { output ->
                    body.byteStream().use { input ->
                        if (!input.copyBoundedTo(output, MAX_ASSET_BYTES)) {
                            temporary.delete()
                            return sourceUrl
                        }
                    }
                }
            }
            if (!temporary.renameTo(destination)) {
                temporary.copyTo(destination, overwrite = true)
                temporary.delete()
            }
            directory.listFiles()
                ?.filter { it != destination && it.name.startsWith("$key-") }
                ?.forEach(File::delete)
            destination.toURI().toString()
        }.getOrElse {
            temporary.delete()
            sourceUrl
        }
    }

    private fun String.sha256(): String = MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { byte -> "%02x".format(byte) }

    private fun InputStream.copyBoundedTo(output: OutputStream, maximumBytes: Long): Boolean {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val count = read(buffer)
            if (count < 0) return true
            total += count
            if (total > maximumBytes) return false
            output.write(buffer, 0, count)
        }
    }

    private companion object {
        const val MAX_ASSET_BYTES = 5L * 1024L * 1024L
        val SUPPORTED_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "gif")
    }
}
