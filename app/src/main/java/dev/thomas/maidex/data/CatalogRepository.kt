package dev.thomas.maidex.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dev.thomas.maidex.BuildConfig
import org.json.JSONArray
import java.io.File

class CatalogRepository(private val context: Context) {
    private val databaseFile = File(context.filesDir, "catalog-${BuildConfig.VERSION_CODE}.db")

    fun load(): CatalogSnapshot {
        ensureDatabase()
        val database = SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY,
        )
        return database.use {
            CatalogSnapshot(
                charts = loadCharts(it),
                options = loadOptions(it),
                info = loadInfo(it),
            )
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
        return database.rawQuery(sql, null).use { cursor ->
            buildList(cursor.count) {
                while (cursor.moveToNext()) add(cursor.toSongChart())
            }
        }
    }

    private fun Cursor.toSongChart(): SongChart = SongChart(
        id = getLong(0),
        chartKey = getString(1),
        sourceSongId = getString(2),
        category = getString(3),
        title = getString(4),
        titleRomanized = getString(5),
        titleAliases = getString(6),
        artist = getString(7),
        artistRomanized = getString(8),
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
        noteDesigner = nullableString(19),
        noteDesignerRomanized = getString(20),
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
        unlockInfo = UnlockMetadata.forSong(getString(2)),
    )

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
}

data class CatalogSnapshot(
    val charts: List<SongChart>,
    val options: FilterOptions,
    val info: CatalogInfo,
)
