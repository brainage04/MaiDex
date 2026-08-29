package io.github.brainage04.maidex.data

import android.content.Context
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import io.github.brainage04.maidex.BuildConfig
import android.util.JsonReader
import android.util.JsonToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class CatalogRepository(private val context: Context) {
    private val databaseFile = File(context.filesDir, "catalog-${BuildConfig.VERSION_CODE}.db")
    private val updatePreferences =
        context.getSharedPreferences("catalog-update", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    fun load(): CatalogSnapshot = synchronized(CATALOG_LOCK) {
        ensureDatabase()
        val database = SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY,
        )
        database.use {
            CatalogSnapshot(
                charts = loadCharts(it),
                options = loadOptions(it),
                info = loadInfo(it),
            )
        }
    }

    fun refreshFromSource(): Boolean {
        synchronized(CATALOG_LOCK) { ensureDatabase() }
        val sourceFile = File(databaseFile.parentFile, "${databaseFile.name}.source.tmp")
        val rebuiltFile = File(databaseFile.parentFile, "${databaseFile.name}.refresh.tmp")
        val request = Request.Builder()
            .url(SOURCE_URL)
            .apply {
                updatePreferences.getString(PREF_SOURCE_ETAG, null)
                    ?.takeIf(String::isNotBlank)
                    ?.let { header("If-None-Match", it) }
            }
            .build()
        try {
            client.newCall(request).execute().use { response ->
                updatePreferences.edit()
                    .putLong(PREF_LAST_CHECKED_AT, System.currentTimeMillis())
                    .apply()
                if (response.code == 304) return false
                check(response.isSuccessful) {
                    "Catalog source returned HTTP ${response.code}"
                }
                val body = checkNotNull(response.body) { "Catalog source returned an empty response" }
                check(body.contentLength() <= MAX_SOURCE_BYTES) { "Catalog source is unexpectedly large" }
                sourceFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        check(input.copyBoundedTo(output, MAX_SOURCE_BYTES)) {
                            "Catalog source is unexpectedly large"
                        }
                    }
                }

                synchronized(CATALOG_LOCK) {
                    rebuildDatabase(sourceFile, rebuiltFile, response.header("Last-Modified"))
                    replaceDatabase(rebuiltFile)
                }
                updatePreferences.edit()
                    .putString(PREF_SOURCE_ETAG, response.header("ETag").orEmpty())
                    .apply()
                return true
            }
        } finally {
            sourceFile.delete()
            rebuiltFile.delete()
        }
    }

    private fun rebuildDatabase(sourceFile: File, destination: File, lastModified: String?) {
        databaseFile.copyTo(destination, overwrite = true)
        val database = SQLiteDatabase.openDatabase(
            destination.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE,
        )
        database.use {
            val songEnrichments = loadSongEnrichments(it)
            val designerEnrichments = loadDesignerEnrichments(it)
            it.beginTransaction()
            try {
                it.delete("charts", null, null)
                it.delete("songs", null, null)
                val result = FileInputStream(sourceFile).use { input ->
                    JsonReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                        importRemoteCatalog(it, reader, songEnrichments, designerEnrichments)
                    }
                }
                check(result.songCount > 0 && result.chartCount > 0) {
                    "Catalog source contained no playable charts"
                }
                result.metadata["source_url"] = SOURCE_URL
                result.metadata["song_count"] = result.songCount.toString()
                result.metadata["chart_count"] = result.chartCount.toString()
                lastModified
                    ?.let(::sourceDate)
                    ?.let { date -> result.metadata.putIfAbsent("catalog_update_time", date) }
                result.metadata.forEach { (key, value) ->
                    it.insertWithOnConflict(
                        "metadata",
                        null,
                        ContentValues(2).apply {
                            put("key", key)
                            put("value", value)
                        },
                        SQLiteDatabase.CONFLICT_REPLACE,
                    )
                }
                it.setTransactionSuccessful()
            } finally {
                it.endTransaction()
            }
            it.rawQuery("PRAGMA quick_check", null).use { cursor ->
                check(cursor.moveToFirst() && cursor.getString(0) == "ok") {
                    "Refreshed catalog database failed integrity validation"
                }
            }
        }
    }

    private fun sourceDate(lastModified: String): String? = runCatching {
        ZonedDateTime.parse(lastModified, DateTimeFormatter.RFC_1123_DATE_TIME)
            .toLocalDate()
            .toString()
    }.getOrNull()

    private fun importRemoteCatalog(
        database: SQLiteDatabase,
        reader: JsonReader,
        songEnrichments: Map<String, SongEnrichment>,
        designerEnrichments: Map<String, String>,
    ): CatalogImportResult {
        val metadata = linkedMapOf<String, String>()
        var songCount = 0
        var chartCount = 0
        reader.beginObject()
        while (reader.hasNext()) {
            when (val name = reader.nextName()) {
                "songs" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val song = readRemoteSong(reader)
                        if (song.sourceId.isBlank()) continue
                        songCount += 1
                        val enrichment = songEnrichments[song.sourceId]
                        database.insertOrThrow(
                            "songs",
                            null,
                            ContentValues(15).apply {
                                put("id", songCount)
                                put("source_id", song.sourceId)
                                put("category", song.category)
                                put("title", song.title.ifBlank { song.sourceId })
                                put(
                                    "title_romanized",
                                    enrichment?.titleRomanized.orEmpty().ifBlank { song.title },
                                )
                                put("title_aliases", enrichment?.titleAliases.orEmpty())
                                put("artist", song.artist)
                                put(
                                    "artist_romanized",
                                    enrichment?.artistRomanized.orEmpty().ifBlank { song.artist },
                                )
                                put("bpm", song.bpm)
                                put("image_url", "$IMAGE_BASE_URL/${song.imageName}")
                                put("version", song.version)
                                put("release_date", song.releaseDate)
                                put("is_new", if (song.isNew) 1 else 0)
                                put("is_locked", if (song.isLocked) 1 else 0)
                                put("comment", song.comment)
                            },
                        )
                        song.sheets.forEach { sheet ->
                            chartCount += 1
                            database.insertOrThrow(
                                "charts",
                                null,
                                ContentValues(23).apply {
                                    put(
                                        "chart_key",
                                        listOf(song.sourceId, sheet.type, sheet.difficulty)
                                            .joinToString("\u001f"),
                                    )
                                    put("song_id", songCount)
                                    put("type", sheet.type)
                                    put("difficulty", sheet.difficulty)
                                    put("level", sheet.level)
                                    put("level_value", sheet.levelValue)
                                    put("internal_level", sheet.internalLevel)
                                    put("chart_constant", sheet.internalLevelValue)
                                    put("note_designer", sheet.noteDesigner)
                                    put(
                                        "note_designer_romanized",
                                        designerEnrichments[sheet.noteDesigner.orEmpty()]
                                            .orEmpty()
                                            .ifBlank { sheet.noteDesigner.orEmpty() },
                                    )
                                    put("tap_count", sheet.noteCounts.tap)
                                    put("hold_count", sheet.noteCounts.hold)
                                    put("slide_count", sheet.noteCounts.slide)
                                    put("touch_count", sheet.noteCounts.touch)
                                    put("break_count", sheet.noteCounts.breakNotes)
                                    put("total_count", sheet.noteCounts.total)
                                    put("region_jp", if (sheet.regions.jp) 1 else 0)
                                    put("region_intl", if (sheet.regions.international) 1 else 0)
                                    put("region_usa", if (sheet.regions.usa) 1 else 0)
                                    put("region_cn", if (sheet.regions.china) 1 else 0)
                                    put("region_overrides", sheet.regionOverrides)
                                    put("is_special", if (sheet.isSpecial) 1 else 0)
                                    put("version", sheet.version)
                                },
                            )
                        }
                    }
                    reader.endArray()
                }
                "updateTime" -> metadata["catalog_update_time"] = reader.nextNullableString().orEmpty()
                "categories", "versions", "types", "difficulties", "regions" ->
                    metadata[name] = readJsonValue(reader).toString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return CatalogImportResult(songCount, chartCount, metadata)
    }

    private fun loadSongEnrichments(database: SQLiteDatabase): Map<String, SongEnrichment> =
        database.rawQuery(
            "SELECT source_id, title_romanized, title_aliases, artist_romanized FROM songs",
            null,
        ).use { cursor ->
            buildMap(cursor.count) {
                while (cursor.moveToNext()) {
                    put(
                        cursor.getString(0),
                        SongEnrichment(
                            titleRomanized = cursor.getString(1),
                            titleAliases = cursor.getString(2),
                            artistRomanized = cursor.getString(3),
                        ),
                    )
                }
            }
        }

    private fun loadDesignerEnrichments(database: SQLiteDatabase): Map<String, String> =
        database.rawQuery(
            """SELECT note_designer, note_designer_romanized FROM charts
                WHERE note_designer IS NOT NULL AND note_designer != ''""",
            null,
        ).use { cursor ->
            buildMap {
                while (cursor.moveToNext()) putIfAbsent(cursor.getString(0), cursor.getString(1))
            }
        }

    private fun replaceDatabase(rebuiltFile: File) {
        try {
            Files.move(
                rebuiltFile.toPath(),
                databaseFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                rebuiltFile.toPath(),
                databaseFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    private fun InputStream.copyBoundedTo(output: java.io.OutputStream, maximumBytes: Long): Boolean {
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

    private fun readRemoteSong(reader: JsonReader): RemoteSong {
        var sourceId = ""
        var category = ""
        var title = ""
        var artist = ""
        var bpm: Int? = null
        var imageName = ""
        var version = ""
        var releaseDate: String? = null
        var isNew = false
        var isLocked = false
        var comment: String? = null
        val sheets = mutableListOf<RemoteSheet>()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "songId" -> sourceId = reader.nextNullableString().orEmpty()
                "category" -> category = reader.nextNullableString().orEmpty()
                "title" -> title = reader.nextNullableString().orEmpty()
                "artist" -> artist = reader.nextNullableString().orEmpty()
                "bpm" -> bpm = reader.nextNullableInt()
                "imageName" -> imageName = reader.nextNullableString().orEmpty()
                "version" -> version = reader.nextNullableString().orEmpty()
                "releaseDate" -> releaseDate = reader.nextNullableString()
                "isNew" -> isNew = reader.nextNullableBoolean()
                "isLocked" -> isLocked = reader.nextNullableBoolean()
                "comment" -> comment = reader.nextNullableString()
                "sheets" -> {
                    reader.beginArray()
                    while (reader.hasNext()) sheets += readRemoteSheet(reader)
                    reader.endArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return RemoteSong(
            sourceId = sourceId,
            category = category,
            title = title,
            artist = artist,
            bpm = bpm,
            imageName = imageName,
            version = version,
            releaseDate = releaseDate,
            isNew = isNew,
            isLocked = isLocked,
            comment = comment,
            sheets = sheets,
        )
    }

    private fun readRemoteSheet(reader: JsonReader): RemoteSheet {
        var type = ""
        var difficulty = ""
        var level: String? = null
        var levelValue: Double? = null
        var internalLevel: String? = null
        var internalLevelValue: Double? = null
        var noteDesigner: String? = null
        var noteCounts = RemoteNoteCounts()
        var regions = RemoteRegions()
        var regionOverrides = "{}"
        var isSpecial = false
        var version: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextNullableString().orEmpty()
                "difficulty" -> difficulty = reader.nextNullableString().orEmpty()
                "level" -> level = reader.nextNullableString()
                "levelValue" -> levelValue = reader.nextNullableDouble()
                "internalLevel" -> internalLevel = reader.nextNullableString()
                "internalLevelValue" -> internalLevelValue = reader.nextNullableDouble()
                "noteDesigner" -> noteDesigner = reader.nextNullableString()
                "noteCounts" -> noteCounts = readRemoteNoteCounts(reader)
                "regions" -> regions = readRemoteRegions(reader)
                "regionOverrides" -> regionOverrides = readJsonValue(reader).toString()
                "isSpecial" -> isSpecial = reader.nextNullableBoolean()
                "version" -> version = reader.nextNullableString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return RemoteSheet(
            type = type,
            difficulty = difficulty,
            level = level,
            levelValue = levelValue,
            internalLevel = internalLevel,
            internalLevelValue = internalLevelValue,
            noteDesigner = noteDesigner,
            noteCounts = noteCounts,
            regions = regions,
            regionOverrides = regionOverrides,
            isSpecial = isSpecial,
            version = version,
        )
    }

    private fun readRemoteNoteCounts(reader: JsonReader): RemoteNoteCounts {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return RemoteNoteCounts()
        }
        var tap: Int? = null
        var hold: Int? = null
        var slide: Int? = null
        var touch: Int? = null
        var breakNotes: Int? = null
        var total: Int? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "tap" -> tap = reader.nextNullableInt()
                "hold" -> hold = reader.nextNullableInt()
                "slide" -> slide = reader.nextNullableInt()
                "touch" -> touch = reader.nextNullableInt()
                "break" -> breakNotes = reader.nextNullableInt()
                "total" -> total = reader.nextNullableInt()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return RemoteNoteCounts(tap, hold, slide, touch, breakNotes, total)
    }

    private fun readRemoteRegions(reader: JsonReader): RemoteRegions {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return RemoteRegions()
        }
        var jp = false
        var international = false
        var usa = false
        var china = false
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "jp" -> jp = reader.nextNullableBoolean()
                "intl" -> international = reader.nextNullableBoolean()
                "usa" -> usa = reader.nextNullableBoolean()
                "cn" -> china = reader.nextNullableBoolean()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return RemoteRegions(jp, international, usa, china)
    }

    private fun readJsonValue(reader: JsonReader): Any = when (reader.peek()) {
        JsonToken.BEGIN_ARRAY -> JSONArray().apply {
            reader.beginArray()
            while (reader.hasNext()) put(readJsonValue(reader))
            reader.endArray()
        }
        JsonToken.BEGIN_OBJECT -> JSONObject().apply {
            reader.beginObject()
            while (reader.hasNext()) put(reader.nextName(), readJsonValue(reader))
            reader.endObject()
        }
        JsonToken.STRING -> reader.nextString()
        JsonToken.NUMBER -> reader.nextString()
        JsonToken.BOOLEAN -> reader.nextBoolean()
        JsonToken.NULL -> {
            reader.nextNull()
            JSONObject.NULL
        }
        else -> error("Unexpected catalog JSON token ${reader.peek()}")
    }

    private fun JsonReader.nextNullableString(): String? = when (peek()) {
        JsonToken.NULL -> {
            nextNull()
            null
        }
        JsonToken.STRING, JsonToken.NUMBER -> nextString()
        else -> {
            skipValue()
            null
        }
    }

    private fun JsonReader.nextNullableInt(): Int? = nextNullableString()?.toIntOrNull()

    private fun JsonReader.nextNullableDouble(): Double? = nextNullableString()?.toDoubleOrNull()

    private fun JsonReader.nextNullableBoolean(): Boolean = when (peek()) {
        JsonToken.BOOLEAN -> nextBoolean()
        JsonToken.STRING, JsonToken.NUMBER -> nextString().toBooleanStrictOrNull() ?: false
        JsonToken.NULL -> {
            nextNull()
            false
        }
        else -> {
            skipValue()
            false
        }
    }
    private fun ensureDatabase() {
        if (databaseFile.exists() && databaseFile.length() > 0L) return
        val temporary = File(databaseFile.parentFile, "${databaseFile.name}.tmp")
        context.assets.open("catalog.db").use { input ->
            temporary.outputStream().use { output -> input.copyTo(output) }
        }
        check(temporary.renameTo(databaseFile)) { "Unable to install catalog database" }
    }

    private fun loadCharts(database: SQLiteDatabase): List<SongChart> {
        val sql = """
            SELECT
                c.id, c.chart_key, s.source_id, s.category, s.title, s.title_romanized,
                s.title_aliases, s.artist, s.artist_romanized, s.bpm, s.image_url,
                s.version, s.release_date, s.comment, c.type, c.difficulty, c.level,
                c.level_value, c.internal_level, c.note_designer,
                c.note_designer_romanized, c.tap_count, c.hold_count, c.slide_count,
                c.touch_count, c.break_count, c.total_count, c.region_jp,
                c.region_intl, c.region_usa, c.region_cn, c.version, c.is_special
            FROM charts c
            JOIN songs s ON s.id = c.song_id
        """.trimIndent()
        val songSearchMetadata = HashMap<String, SongSearchMetadata>(2_048)
        val designerSearchableTexts = HashMap<String, String>(256)
        return database.rawQuery(sql, null).use { cursor ->
            buildList(cursor.count) {
                while (cursor.moveToNext()) {
                    add(cursor.toSongChart(songSearchMetadata, designerSearchableTexts))
                }
            }
        }
    }

    private fun Cursor.toSongChart(
        songSearchMetadata: MutableMap<String, SongSearchMetadata>,
        designerSearchableTexts: MutableMap<String, String>,
    ): SongChart {
        val sourceSongId = getString(2)
        val title = getString(4)
        val titleRomanized = getString(5)
        val titleAliases = getString(6)
        val artist = getString(7)
        val artistRomanized = getString(8)
        val noteDesigner = nullableString(19)
        val noteDesignerRomanized = getString(20)
        val songSearch = songSearchMetadata.getOrPut(sourceSongId) {
            SongSearchMetadata(
                searchableText = normalizeSearch(
                    listOf(
                        title,
                        titleRomanized,
                        titleAliases.replace('\u001e', ' '),
                        artist,
                        artistRomanized,
                    ).joinToString(" "),
                ),
                artistSearchText = normalizeSearch("$artist $artistRomanized"),
                titleSortKey = title.lowercase(java.util.Locale.ROOT),
            )
        }
        val designerSearchableText =
            if (noteDesigner.isNullOrBlank() && noteDesignerRomanized.isBlank()) {
                ""
            } else {
                val designerKey = "${noteDesigner.orEmpty()}\u001f$noteDesignerRomanized"
                designerSearchableTexts.getOrPut(designerKey) {
                    normalizeSearch("${noteDesigner.orEmpty()} $noteDesignerRomanized")
                }
            }

        return SongChart(
            id = getLong(0),
            chartKey = getString(1),
            sourceSongId = sourceSongId,
            category = getString(3),
            title = title,
            titleRomanized = titleRomanized,
            titleAliases = titleAliases,
            artist = artist,
            artistRomanized = artistRomanized,
            bpm = nullableInt(9),
            imageUrl = getString(10),
            songVersion = getString(11),
            releaseDate = nullableString(12),
            comment = nullableString(13),
            type = getString(14),
            difficulty = getString(15),
            level = nullableString(16),
            levelValue = nullableDouble(17),
            constant = nullableDouble(18),
            noteDesigner = noteDesigner,
            noteDesignerRomanized = noteDesignerRomanized,
            noteCounts = NoteCounts(
                tap = nullableInt(21),
                hold = nullableInt(22),
                slide = nullableInt(23),
                touch = nullableInt(24),
                breakNotes = nullableInt(25),
                total = nullableInt(26),
            ),
            regions = Regions(
                jp = getInt(27) == 1,
                international = getInt(28) == 1,
                usa = getInt(29) == 1,
                china = getInt(30) == 1,
            ),
            chartVersion = nullableString(31),
            isSpecial = getInt(32) == 1,
            unlockInfo = UnlockMetadata.forSong(sourceSongId),
            searchableText = songSearch.searchableText + designerSearchableText,
            artistSearchText = songSearch.artistSearchText,
            designerSearchText = designerSearchableText,
            titleSortKey = songSearch.titleSortKey,
        )
    }

    private fun loadOptions(database: SQLiteDatabase): FilterOptions = FilterOptions(
        categories = metadataArray(database, "categories", "category"),
        versions = metadataArray(database, "versions", "version").asReversed(),
        difficulties = metadataArray(database, "difficulties", "difficulty"),
        types = metadataArray(database, "types", "type"),
        regions = metadataArray(database, "regions", "region"),
    )

    private fun loadInfo(database: SQLiteDatabase): CatalogInfo = CatalogInfo(
        updateTime = metadata(database, "catalog_update_time"),
        songCount = metadata(database, "song_count").toInt(),
        chartCount = metadata(database, "chart_count").toInt(),
        sourceUrl = metadata(database, "source_url"),
        lastCheckedAt = updatePreferences.getLong(PREF_LAST_CHECKED_AT, 0L),
    )

    private fun metadataArray(database: SQLiteDatabase, key: String, field: String): List<String> {
        val array = JSONArray(metadata(database, key))
        return buildList(array.length()) {
            repeat(array.length()) { index -> add(array.getJSONObject(index).getString(field)) }
        }
    }

    private fun metadata(database: SQLiteDatabase, key: String): String =
        database.rawQuery("SELECT value FROM metadata WHERE key = ?", arrayOf(key)).use { cursor ->
            check(cursor.moveToFirst()) { "Missing catalog metadata: $key" }
            cursor.getString(0)
        }

    private fun Cursor.nullableString(index: Int): String? = if (isNull(index)) null else getString(index)
    private fun Cursor.nullableInt(index: Int): Int? = if (isNull(index)) null else getInt(index)
    private fun Cursor.nullableDouble(index: Int): Double? = if (isNull(index)) null else getDouble(index)

    private companion object {
        const val SOURCE_URL = "https://dp4p6x0xfi5o9.cloudfront.net/maimai/data.json"
        const val IMAGE_BASE_URL = "https://dp4p6x0xfi5o9.cloudfront.net/maimai/img/cover"
        const val PREF_SOURCE_ETAG = "source_etag"
        const val PREF_LAST_CHECKED_AT = "last_checked_at"

        const val MAX_SOURCE_BYTES = 32L * 1024L * 1024L
        val CATALOG_LOCK = Any()
    }
}
private data class SongSearchMetadata(
    val searchableText: String,
    val artistSearchText: String,
    val titleSortKey: String,
)

private data class SongEnrichment(
    val titleRomanized: String,
    val titleAliases: String,
    val artistRomanized: String,
)

private data class CatalogImportResult(
    val songCount: Int,
    val chartCount: Int,
    val metadata: MutableMap<String, String>,
)

private data class RemoteSong(
    val sourceId: String,
    val category: String,
    val title: String,
    val artist: String,
    val bpm: Int?,
    val imageName: String,
    val version: String,
    val releaseDate: String?,
    val isNew: Boolean,
    val isLocked: Boolean,
    val comment: String?,
    val sheets: List<RemoteSheet>,
)

private data class RemoteSheet(
    val type: String,
    val difficulty: String,
    val level: String?,
    val levelValue: Double?,
    val internalLevel: String?,
    val internalLevelValue: Double?,
    val noteDesigner: String?,
    val noteCounts: RemoteNoteCounts,
    val regions: RemoteRegions,
    val regionOverrides: String,
    val isSpecial: Boolean,
    val version: String?,
)

private data class RemoteNoteCounts(
    val tap: Int? = null,
    val hold: Int? = null,
    val slide: Int? = null,
    val touch: Int? = null,
    val breakNotes: Int? = null,
    val total: Int? = null,
)

private data class RemoteRegions(
    val jp: Boolean = false,
    val international: Boolean = false,
    val usa: Boolean = false,
    val china: Boolean = false,
)

data class CatalogSnapshot(
    val charts: List<SongChart>,
    val options: FilterOptions,
    val info: CatalogInfo,
)
